package de.sebastianroeder.ribbit;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpened(getIntent());
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            startLoginActivity();
        } else {
            Log.i(RibbitConstants.DEBUG_TAG,
                    String.format("User %s is logged in", currentUser.getUsername()));
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case RibbitConstants.PAGE_INBOX:
                        return new InboxFragment();
                    case RibbitConstants.PAGE_FRIENDS:
                        return new FriendsFragment();
                    default:
                        throw new IllegalArgumentException("No fragment at position " + position);
                }
            }

            @Override
            public int getCount() {
                return RibbitConstants.PAGE_COUNT;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                Locale l = Locale.getDefault();
                switch (position) {
                    case RibbitConstants.PAGE_INBOX:
                        return getString(R.string.title_section_inbox).toUpperCase(l);
                    case RibbitConstants.PAGE_FRIENDS:
                        return getString(R.string.title_section_friends).toUpperCase(l);
                    default:
                        throw new IllegalArgumentException("No title for section " + position);
                }
            }
        });

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                             .setText(mViewPager.getAdapter().getPageTitle(i))
                             .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_edit_friends:
                startEditFriendsActivity();
                break;
            case R.id.action_logout:
                String username = ParseUser.getCurrentUser().getUsername();
                ParseUser.logOut();
                Log.i(RibbitConstants.DEBUG_TAG, String.format("User %s is logged out", username));
                startLoginActivity();
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Camera Options")
                       .setItems(R.array.camera_options, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int option) {
                               switch (option) {
                                   case RibbitConstants.OPTION_TAKE_PICTURE:
                                       startTakePictureActivity();
                                       break;
                                   case RibbitConstants.OPTION_TAKE_VIDEO:
                                       startTakeVideoActivity();
                                       break;
                                   case RibbitConstants.OPTION_CHOOSE_EXISTING_PICTURE:
                                       startChooseExistingPictureActivity();
                                       break;
                                   case RibbitConstants.OPTION_CHOOSE_EXISTING_VIDEO:
                                       startChooseExistingVideoActivity();
                                       break;
                               }
                           }
                       })
                       .create()
                       .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case RibbitConstants.REQUEST_TAKE_VIDEO:
                    broadcastNewMediaFile(data);
                    break;
                case RibbitConstants.REQUEST_CHOOSE_EXISTING_PICTURE:
                    // TODO: do something with the picture URI
                    break;
                case RibbitConstants.REQUEST_CHOOSE_EXISTING_VIDEO:
                    // TODO: check that video size is not more than 10 MB
                    // TODO: do something with the video URI
                    break;
            }
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_message_general_error, Toast.LENGTH_LONG).show();
        }
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                   .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void startEditFriendsActivity() {
        Intent editFriendsIntent = new Intent(this, EditFriendsActivity.class);
        startActivity(editFriendsIntent);
    }

    private void startTakePictureActivity() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri pictureUri = getOutputMediaFileUri(RibbitConstants.MEDIA_TYPE_IMAGE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivity(takePictureIntent);
        } catch (RibbitStorageStateException e) {
            Log.e(RibbitConstants.DEBUG_TAG, e.getMessage());

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.generic_error_title)
                   .setMessage(e.getMessage())
                   .setPositiveButton(android.R.string.ok, null)
                   .create()
                   .show();
        }
    }

    private void startTakeVideoActivity() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        try {
            Uri videoUri = getOutputMediaFileUri(RibbitConstants.MEDIA_TYPE_VIDEO);
            // File size limit for parse.com free plan is 10 MB
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                           .putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
                           .putExtra(MediaStore.EXTRA_VIDEO_QUALITY, RibbitConstants.VIDEO_QUALITY_LOW);
            startActivityForResult(takeVideoIntent, RibbitConstants.REQUEST_TAKE_VIDEO);
        } catch (RibbitStorageStateException e) {
            Log.e(RibbitConstants.DEBUG_TAG, e.getMessage());

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.generic_error_title)
                   .setMessage(e.getMessage())
                   .setPositiveButton(android.R.string.ok, null)
                   .create()
                   .show();
        }
    }

    private void startChooseExistingPictureActivity() {
        Intent chooseExistingPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseExistingPictureIntent.setType("image/*");
        startActivityForResult(chooseExistingPictureIntent, RibbitConstants.REQUEST_CHOOSE_EXISTING_PICTURE);
    }

    private void startChooseExistingVideoActivity() {
        Intent chooseExistingVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseExistingVideoIntent.setType("video/*");
        Toast.makeText(this, R.string.warning_video_file_size, Toast.LENGTH_LONG).show();
        startActivityForResult(chooseExistingVideoIntent, RibbitConstants.REQUEST_CHOOSE_EXISTING_VIDEO);
    }

    private Uri getOutputMediaFileUri(int mediaType) throws RibbitStorageStateException {

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new RibbitStorageStateException(
                    getString(R.string.error_message_external_storage));
        }

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.app_name));

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            String message = String.format("%s %s.",
                    getString(R.string.error_message_create_directory),
                    mediaStorageDir.getAbsolutePath());
            throw new RibbitStorageStateException(message);
        }

        String dateFormat = "yyyyMMdd_HHmmss";
        String timestamp = new SimpleDateFormat(dateFormat).format(new Date());
        String path = mediaStorageDir.getPath() + File.separator;
        String base_name;
        String suffix;

        switch (mediaType) {
            case RibbitConstants.MEDIA_TYPE_IMAGE:
                base_name = "IMG_";
                suffix = ".jpg";
                break;
            case RibbitConstants.MEDIA_TYPE_VIDEO:
                base_name = "VID_";
                suffix = ".mp4";
                break;
            default:
                throw new IllegalArgumentException(
                        getString(R.string.error_message_unknown_media_type));
        }

        File mediaFile = new File(path + base_name + timestamp + suffix);
        Uri mediaFileUri = Uri.fromFile(mediaFile);
        Log.i(RibbitConstants.DEBUG_TAG, "Save media file: " + mediaFileUri.toString());

        return mediaFileUri;
    }

    private void broadcastNewMediaFile(Intent data) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(data.getData());
        sendBroadcast(mediaScanIntent);
    }

}
