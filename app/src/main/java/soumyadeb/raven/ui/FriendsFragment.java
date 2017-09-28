package soumyadeb.raven.ui;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import soumyadeb.raven.R;
import soumyadeb.raven.models.Friend;
import soumyadeb.raven.models.User;
import soumyadeb.raven.viewholders.UserViewHolder;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    // UI Instances:
    private RecyclerView mUsersList;
    private ProgressDialog mProgress;

    // Firebase Instances:
    private DatabaseReference mFriendsDB;
    private DatabaseReference mUserDB;
    private FirebaseAuth mAuth;

    private ArrayList<User> userList;
    private ArrayList<String>keyList;
    //private UsersAdapter adapter;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_friends, container, false);
        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Loading...");

        mProgress.show();

        userList = new ArrayList<>();
        keyList = new ArrayList<>();
        //adapter = new UsersAdapter(getActivity(), userList, R.layout.user_item_friends);

        mAuth = FirebaseAuth.getInstance();
        String currentUser =  mAuth.getCurrentUser().getUid().toString();

        mUserDB = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendsDB = FirebaseDatabase.getInstance().getReference().child("friends_data").child(currentUser);
        mFriendsDB.keepSynced(true);
        mUserDB.keepSynced(true);

        mUsersList = (RecyclerView)mView.findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));
        //mUsersList.setAdapter(adapter);



        return mView;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend, UserViewHolder> adapter = new FirebaseRecyclerAdapter<Friend, UserViewHolder>(
                Friend.class,
                R.layout.user_item_friends,
                UserViewHolder.class,
                mFriendsDB
        ) {
            @Override
            protected void populateViewHolder(final UserViewHolder viewHolder, Friend model, int position) {
                final String key = getRef(position).getKey();

                mUserDB.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot != null) {
                            String name = dataSnapshot.child("name").getValue().toString();

                            viewHolder.setName(name);

                            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                            if(dataSnapshot.child("online").getValue()!= null) {
                                String online = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setThumbImage(thumb_image, online);
                            }
                            else {
                                viewHolder.setThumbImage(thumb_image, "false");
                            }
                            String status = dataSnapshot.child("status").getValue().toString();
                            viewHolder.setStatus(status);

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                    profileIntent.putExtra("userId", key);
                                    startActivity(profileIntent);

                                }
                            });
                        }
                        else {
                            mProgress.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if(databaseError.getCode()!= DatabaseError.PERMISSION_DENIED){
                            Toast.makeText(getContext(), "Error occurred, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                mProgress.dismiss();

            }

        };

        mUsersList.setAdapter(adapter);
        if(mUsersList.getChildCount() == 0){
            mProgress.dismiss();
        }
    }

}
