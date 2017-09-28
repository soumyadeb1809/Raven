package soumyadeb.raven.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import soumyadeb.raven.R;
import soumyadeb.raven.adapters.MainPagerAdapter;
import soumyadeb.raven.firebase.FirebaseAssignment;
import soumyadeb.raven.utility.Tools;

public class MainActivity extends AppCompatActivity {
    private static final long ONE_MEGABYTE = 1024 * 1024;
    //Firebase Instances:
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    DatabaseReference mUserDB;

    //UI Instances:
    private Toolbar toolbar;
    private ViewPager mViewPager;
    private  MainPagerAdapter mPagerAdapter;
    private TabLayout mTabLayout;

    private String uId = null;
    MenuItem settingsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=(Toolbar)findViewById(R.id.main_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("images").child("thumbnails");
        mUserDB = FirebaseDatabase.getInstance().getReference().child(FirebaseAssignment.NAME_USERS_DB);

        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);

        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(MainPagerAdapter.ITEM_CHATS, true);





    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }
        else {
            uId = currentUser.getUid();
            //Drawable thumbnail =null;
            mUserDB.child(uId).child("online").setValue(true);
            StorageReference thumbFilepath = mStorage.child(uId+".jpg");
            thumbFilepath.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Drawable image = new BitmapDrawable(getResources(), Tools.getCroppedBitmap(bitmapImage));
                    settingsMenu.setIcon(image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

            //settingsMenu.setIcon(thumbnail);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        settingsMenu = menu.findItem(R.id.main_profile);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_action_logout)
        {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.main_account_settings)
        {
           startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        if(item.getItemId() == R.id.main_profile)
        {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        if(item.getItemId() == R.id.main_all_users)
        {
            startActivity(new Intent(MainActivity.this, AllUsersActivity.class));
        }

        return true;
    }

    private void sendToStart(){
        startActivity(new Intent(MainActivity.this, StartActivity.class));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null) {
            mUserDB.child(mAuth.getCurrentUser().getUid().toString()).child("online").setValue(false);
        }
    }
}
