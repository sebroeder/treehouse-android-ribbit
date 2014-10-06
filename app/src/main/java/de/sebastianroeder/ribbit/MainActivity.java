package de.sebastianroeder.ribbit;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

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
            navigateToLoginActivity();
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
                navigateToEditFriendsActivity();
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
                                        break;
                                    case RibbitConstants.OPTION_CHOOSE_EXISTING_PICTURE:
                                        break;
                                    case RibbitConstants.OPTION_CHOOSE_EXISTING_VIDEO:
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

    private void navigateToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void navigateToEditFriendsActivity() {
        Intent editFriendsIntent = new Intent(this, EditFriendsActivity.class);
        startActivity(editFriendsIntent);
    }

}
