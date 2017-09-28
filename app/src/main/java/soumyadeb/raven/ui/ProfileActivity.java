package soumyadeb.raven.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import soumyadeb.raven.R;
import soumyadeb.raven.firebase.FirebaseAssignment;
import soumyadeb.raven.models.NotificationData;
import soumyadeb.raven.utility.Tools;


public class ProfileActivity extends AppCompatActivity {

    // UI Instances:
    private TextView mDisplayName;
    private TextView mStatus;
    private TextView mTotalFriends;
    private ImageView mProfileImage;
    private Button mSendReq;
    private Button mDeclineReq;
    private ProgressDialog mProgress;
    Toolbar toolbar;



    // Firebase Instances:
    private DatabaseReference mUserDB;
    private DatabaseReference mFriendReqDB;
    private DatabaseReference mFriendsDB;
    private DatabaseReference mNotificationDB;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    // Data members:
    private String mCurrentUserId;
    private String mProfileUserId;
    private int mCurrentReqState;
    public static String REQ_SENT = "sent";
    public static String REQ_RECEIVED = "received";

    // Friends states:
    public static final int STATE_NOT_FRIENDS = 0;
    public static final int STATE_REQ_SENT = 1;
    public static final int STATE_REQ_RECEIVED = 2;
    public static final int STATE_FRIENDS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize UI instances:
        mDisplayName = (TextView)findViewById(R.id.text_display_name);
        mStatus = (TextView)findViewById(R.id.text_status);
        mTotalFriends = (TextView)findViewById(R.id.text_total_frnds);
        mSendReq = (Button)findViewById(R.id.send_frnd_req);
        mProfileImage = (ImageView)findViewById(R.id.img_profile);
        mSendReq = (Button)findViewById(R.id.send_frnd_req);
        mDeclineReq = (Button)findViewById(R.id.decline_frnd_req);

        // Get current user's ID:
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid().toString();

        //Initialize and start Progress Dialog:
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.show();

        // Get mProfileUserId from Intent:
        mProfileUserId = getIntent().getStringExtra("userId");
        if(mProfileUserId != null){
            //Get data from the database:
            
            mRootRef = FirebaseDatabase.getInstance().getReference();
            // Path: /users/mProfileUserId:
            mUserDB = mRootRef.child(FirebaseAssignment.NAME_USERS_DB).child(mProfileUserId);
            mUserDB.keepSynced(true);

            // Path: /friend_requests :
            mFriendReqDB = mRootRef.child(FirebaseAssignment.NAME_FRIEND_REQ_DB);
            mFriendReqDB.keepSynced(true);

            // Path: /friends :
            mFriendsDB = mRootRef.child(FirebaseAssignment.NAME_FRIENDS_DB);
            mFriendsDB.keepSynced(true);

            // Path: /notifications :
            mNotificationDB = mRootRef.child(FirebaseAssignment.NAME_NOTIFICATION_DB);
            mNotificationDB.keepSynced(true);

            // Get current request state:
            mFriendReqDB.child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mProgress.dismiss();
                    mSendReq.setVisibility(View.VISIBLE);

                    mFriendsDB.child(mProfileUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int totalFriends = (int)dataSnapshot.getChildrenCount();
                            mTotalFriends.setText("Total Friends : "+totalFriends);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            mProgress.dismiss();
                            Toast.makeText(ProfileActivity.this,"Error occurred. Please try again.",Toast.LENGTH_LONG).show();
                        }
                    });


                    if(dataSnapshot.hasChild(mProfileUserId)){

                        String requestState = dataSnapshot.child(mProfileUserId).child("request_state").getValue().toString();
                        if(requestState.equals(REQ_SENT)){
                            mCurrentReqState = STATE_REQ_SENT;
                            mSendReq.setText("Cancel friend request");
                        }
                        else if(requestState.equals(REQ_RECEIVED)){
                            mCurrentReqState = STATE_REQ_RECEIVED;
                            mSendReq.setText("Accept friend request");
                            mDeclineReq.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        mFriendsDB.child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(mProfileUserId)){
                                    mCurrentReqState = STATE_FRIENDS;
                                    mSendReq.setText("Remove friend");
                                }
                                else {
                                    mCurrentReqState = STATE_NOT_FRIENDS;
                                    mSendReq.setText("Send friend request");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                mProgress.dismiss();
                                Toast.makeText(ProfileActivity.this,"Error occurred. Please try again.",Toast.LENGTH_LONG).show();
                            }
                        }

                        );

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mProgress.dismiss();
                    if(databaseError.getCode() != DatabaseError.PERMISSION_DENIED)
                        Toast.makeText(ProfileActivity.this,"Error occurred. Please try again.",Toast.LENGTH_LONG).show();
                }
            });




            // Get data of the profile from the database:
            mUserDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    updateData(name, status, image, getApplicationContext());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if(databaseError.getCode() != DatabaseError.PERMISSION_DENIED)
                        Toast.makeText(ProfileActivity.this,"Error occurred. Please try again.",Toast.LENGTH_LONG).show();
                }
            });

        }

        /** SENDING FRIEND REQUESTS:
         *  In friend_requests section of database, data is stored
         *  in following format:
         *
         *  The user who is sending the req = 'user1'
         *  user1_key{
         *      .user2_key{
         *          request_state: "sent"
         *      }
         *  }
         *
         *  The user who is receiving the req = 'user2'
         *  user2_key{
         *      .user1_key{
         *          request_state: "received"
         *      }
         *  }
         *
         *  Send Request button onClick handler
         */

        mSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //---- Handle the behavior of the send request button---
                
               if(mCurrentReqState == STATE_REQ_RECEIVED){
                   acceptReq(v);
               }
               else {
                   sendCancelReq(v);
               }
                
            }
        });
        
        mDeclineReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeReq(v, "declined");
            }
        });


    }




    private void sendCancelReq(final View v) {
        mSendReq.setEnabled(false);

        //----------------------------------SEND FRIEND REQUEST----------------------------------------------
        switch (mCurrentReqState) {
            case STATE_NOT_FRIENDS:
                // First update data for user1:
                Map requestMap = new HashMap();

                requestMap.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                        +mProfileUserId+"/"+mCurrentUserId+"/"+"request_state","received");

                requestMap.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                        +mCurrentUserId+"/"+mProfileUserId+"/"+"request_state","sent");

                mRootRef.updateChildren(requestMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        NotificationData notificationData = new NotificationData(mCurrentUserId, "request");

                        mNotificationDB.child(mProfileUserId).push().setValue(notificationData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, "Friend request sent.", Toast.LENGTH_SHORT).show();
                                        mSendReq.setText("Cancel friend request");
                                        mCurrentReqState = STATE_REQ_SENT;
                                        mSendReq.setEnabled(true);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                reqSendingError(v);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        reqSendingError(v);
                    }
                });

                break;


                //----------------------------------CANCEL FRIEND REQUEST----------------------------------------------
                // First delete the data(userId) from current user, then delete from the other user's database:
            case STATE_REQ_SENT:
                removeReq(v, "cancelled");
                break;

            case STATE_FRIENDS:
                removeFriend(v);
        }

    }


    private void removeReq(View v, final String type) {

        Map removeReq = new HashMap();

        removeReq.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                +mCurrentUserId+"/"+mProfileUserId, null);
        removeReq.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                +mProfileUserId+"/"+mCurrentUserId, null);

        mRootRef.updateChildren(removeReq).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                mSendReq.setEnabled(true);
                mSendReq.setText("Send friend request");
                mCurrentReqState = STATE_NOT_FRIENDS;
                if(type.equals("declined")) {
                    mDeclineReq.setVisibility(View.GONE);
                }
                if(!type.equals("accepted")) {
                    Toast.makeText(ProfileActivity.this, "Friend request " + type, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Tools.showFailureError(ProfileActivity.this);
            }
        });


    }

    private void acceptReq(View v) {

        Map acceptRq = new HashMap();

        acceptRq.put(FirebaseAssignment.NAME_FRIENDS_DB+"/"+mCurrentUserId+"/"
                +mProfileUserId+"/timestamp", Tools.getCurrentTimeStamp());

        acceptRq.put(FirebaseAssignment.NAME_FRIENDS_DB+"/"+mProfileUserId+"/"
                +mCurrentUserId+"/timestamp", Tools.getCurrentTimeStamp());

        acceptRq.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                +mCurrentUserId+"/"+mProfileUserId, null);

        acceptRq.put(FirebaseAssignment.NAME_FRIEND_REQ_DB+"/"
                +mProfileUserId+"/"+mCurrentUserId, null);

        mRootRef.updateChildren(acceptRq).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                mSendReq.setEnabled(true);
                mCurrentReqState = STATE_FRIENDS;
                mSendReq.setText("Remove friend");
                mDeclineReq.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Friend request accepted", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mSendReq.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Error occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeFriend(View v) {
        mFriendsDB.child(mCurrentUserId).child(mProfileUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendsDB.child(mProfileUserId).child(mCurrentUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mSendReq.setEnabled(true);
                        mSendReq.setText("Send friend request");
                        mCurrentReqState = STATE_NOT_FRIENDS;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mSendReq.setEnabled(true);
                        Tools.showFailureError(ProfileActivity.this);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mSendReq.setEnabled(true);
                Tools.showFailureError(ProfileActivity.this);
            }
        });
    }


    // Method to update the UI:
    private void updateData(String name, String status, final String image, final Context context) {
        mDisplayName.setText(name);
        mStatus.setText(status);
        if(image.equals("default")){
            mProfileImage.setImageResource(R.drawable.ic_profile_img);
        }
        else {
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_profile_img).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {
                    // Do nothing
                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).placeholder(R.drawable.ic_profile_img).into(mProfileImage);
                }
            });
        }
    }

    private void reqSendingError(View v){
        mSendReq.setEnabled(true);
        Toast.makeText(ProfileActivity.this, "Friend not request sent. Please try again", Toast.LENGTH_SHORT).show();
    }

}
