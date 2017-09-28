package soumyadeb.raven;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import soumyadeb.raven.firebase.FirebaseAssignment;

/**
 * Created by Soumya Deb on 11-07-2017.
 */

public class SlimeChat extends Application {
    FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();

        // OFFLINE FIREBASE:
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            final DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference()
                    .child(FirebaseAssignment.NAME_USERS_DB).child(mAuth.getCurrentUser().getUid().toString());

            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserRef.child("online").onDisconnect().setValue(false);
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        // OFFLINE PICASSO:
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso pic = builder.build();
        pic.setIndicatorsEnabled(true);
        pic.setLoggingEnabled(true);
        Picasso.setSingletonInstance(pic);
    }

}
