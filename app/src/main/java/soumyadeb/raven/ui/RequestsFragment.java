package soumyadeb.raven.ui;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import soumyadeb.raven.R;
import soumyadeb.raven.adapters.UsersAdapter;
import soumyadeb.raven.models.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    // UI Instances:
    private RecyclerView mUsersList;
    private ProgressDialog mProgress;

    // Firebase Instances:
    private DatabaseReference mFriendsDB;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private ArrayList<User> userList;
    private ArrayList<String>keyList;
    private UsersAdapter adapter;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_requests, container, false);


        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Loading...");

        mProgress.show();

        keyList = new ArrayList<>();
        userList = new ArrayList<>();
        adapter = new UsersAdapter(getActivity(), userList, R.layout.user_item_friend_req);

        mAuth = FirebaseAuth.getInstance();
        String currentUser =  mAuth.getCurrentUser().getUid().toString();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendsDB = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(currentUser);
        mFriendsDB.keepSynced(true);
        mDatabase.keepSynced(true);

        mUsersList = (RecyclerView)mView.findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsersList.setAdapter(adapter);


        mFriendsDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String key = dataSnapshot.getKey();
                if(dataSnapshot.child("request_state").getValue().equals("received")) {
                    mDatabase.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.setKey(key);
                            keyList.add(key);
                            userList.add(user);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mProgress.dismiss();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Do nothing
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals("received")) {
                    String key = dataSnapshot.getKey();
                    int index = keyList.indexOf(key);
                    userList.remove(index);
                    adapter.notifyDataSetChanged();
                }
                // Do nothing
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Do nothing
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        });



        if(userList.size() == 0)
            mProgress.dismiss();

        return mView;
    }

}
