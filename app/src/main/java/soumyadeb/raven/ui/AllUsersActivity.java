package soumyadeb.raven.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import soumyadeb.raven.R;
import soumyadeb.raven.models.User;
import soumyadeb.raven.viewholders.UserViewHolder;

public class AllUsersActivity extends AppCompatActivity {

    // UI Instances:
    private Toolbar toolbar;
    private RecyclerView mUsersList;
    private ProgressDialog mProgress;

    // Firebase Instances:
    private DatabaseReference mUserDB;
    private FirebaseAuth mAuth;


    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Search");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.show();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.keepSynced(true);

        mUsersList = (RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!= null){
            mUserDB = FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getUid().toString());
        }



    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!= null){
            mUserDB.child("online").setValue(true);
        }

        FirebaseRecyclerAdapter<User, UserViewHolder> adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.user_item,
                UserViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User model, int position) {
                viewHolder.setName(model.getName());
                Toast.makeText(AllUsersActivity.this,""+model.getName(),Toast.LENGTH_LONG);
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image(), "false");

                final String userId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("userId",userId);
                        startActivity(profileIntent);
                    }
                });
                mProgress.dismiss();
            }
        };

        mUsersList.setAdapter(adapter);

    }


    @Override
    protected void onStop() {
        super.onStop();
        mUserDB.child("online").setValue(false);
    }
}


