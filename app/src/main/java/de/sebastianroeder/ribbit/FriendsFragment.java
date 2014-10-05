package de.sebastianroeder.ribbit;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends ListFragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setProgressBarIndeterminateVisibility(true);
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseRelation<ParseUser> friendRelation =
                currentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
        ParseQuery<ParseUser> query = friendRelation.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME)
                .findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                Context context = getListView().getContext();

                if (e == null) {
                    List<String> friendNames = new ArrayList<String>(friends.size());
                    for (ParseUser friend : friends) {
                        friendNames.add(friend.getUsername());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            context,
                            android.R.layout.simple_list_item_1,
                            friendNames);

                    setListAdapter(adapter);
                } else {
                    Log.e(TAG, e.getMessage());

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.generic_error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .create()
                            .show();
                }
            }
        });

    }
}
