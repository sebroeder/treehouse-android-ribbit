package de.sebastianroeder.ribbit;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class EditFriendsActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();
    protected ParseUser mCurrentUser;
    protected ParseRelation<ParseUser> mFriendRelation;
    protected List<ParseUser> mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_friends);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.orderByAscending(ParseConstants.KEY_USERNAME);
        userQuery.setLimit(1000);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e == null) {
                    mUsers = parseUsers;
                    List<String> userNames = new ArrayList<String>(mUsers.size());

                    // TODO: do not include currently logged in user in list
                    for (ParseUser user : mUsers) {
                        userNames.add(user.getUsername());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            EditFriendsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            userNames);
                    setListAdapter(adapter);
                    
                    addFriendCheckmarks();
                } else {
                    Log.e(TAG, e.getMessage());

                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setTitle(R.string.generic_error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .create()
                            .show();
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseUser clickedUser = mUsers.get(position);
        if (l.isItemChecked(position)) {
            mFriendRelation.add(clickedUser);
        } else {
            mFriendRelation.remove(clickedUser);
        }

        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());

                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setTitle(R.string.generic_error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .create()
                            .show();
                }
            }
        });
    }

    private void addFriendCheckmarks() {
        mFriendRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    ListView listView = getListView();
                    for (ParseUser user : mUsers) {
                        for (ParseUser friend : friends) {
                            if (user.getObjectId().equals(friend.getObjectId())) {
                                listView.setItemChecked(mUsers.indexOf(user), true);
                                break;
                            }
                        }
                    }

                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

}
