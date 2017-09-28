package soumyadeb.raven.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import soumyadeb.raven.R;

public class SettingsActivity extends AppCompatActivity {

    // UI Instances:
    private static final int MAX_LENGTH = 20;
    private TextView mDisplayName, mStatus;
    private CircleImageView mImage;
    private ImageView mSettingsBack;
    private Button mChangeImgButton, mChangeStatusBtn;
    private ProgressDialog mProgress;
    Toolbar toolbar;

    // Firebase Instances:
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private StorageReference mImageStorage;

    private String name, image, status, thumb_image;

    //Image data:

    private static final int MAX_THUMB_HEIGHT = 100;
    private static final int MAX_THUMB_WIDTH = 100;
    private static final int MAX_THUMB_QUALITY = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize UI instances:
        mDisplayName = (TextView) findViewById(R.id.text_display_name);
        mStatus = (TextView) findViewById(R.id.text_status);
        mImage = (CircleImageView) findViewById(R.id.img_profile);
        mChangeImgButton = (Button) findViewById(R.id.setting_change_image);
        mChangeStatusBtn = (Button) findViewById(R.id.setting_change_status);
        mSettingsBack = (ImageView)findViewById(R.id.back_settings);
        mProgress = new ProgressDialog(this);

        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        // Get current user from FirebaseAuth:
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser.getUid();

        // Populate the UI with the current data stored in the database:
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid);
        mDatabase.keepSynced(true);

        // File path: root/images/
        mImageStorage = FirebaseStorage.getInstance().getReference().child("images");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                if(image.equals("default")){
                    mImage.setImageResource(R.drawable.ic_circle_profile_img);
                }
                else {

                    // Load Profile Image
                    Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_circle_profile_img).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Do nothing
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_circle_profile_img)
                                    .into(mImage);
                        }
                    });

                    // Load Background Image
                    Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mSettingsBack, new Callback() {
                                @Override
                                public void onSuccess() {
                                    // Do nothing
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(getApplicationContext()).load(image).into(mSettingsBack);
                                }
                            });
                }

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                if(databaseError.getCode() != DatabaseError.PERMISSION_DENIED)
                    Toast.makeText(SettingsActivity.this,"Error occurred. Please try again.",Toast.LENGTH_LONG).show();
            }
        });

        // Button onClick handlers:
            // Change Status button
        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, StatusActivity.class));
            }
        });

            // Change Image button
        mChangeImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });
    }

    // After user selects an image from gallery:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected image:
                Uri resultUri = result.getUri();

                // Create an instance of file from Uri:
                File imageFilePath = new File(resultUri.getPath());

                String currentUserId = currentUser.getUid();

                // Compress the selected image ans store it as Bitmap:
                Bitmap thumbBitmap = new Compressor(this).setMaxHeight(MAX_THUMB_HEIGHT).
                                            setMaxWidth(MAX_THUMB_WIDTH).setQuality(MAX_THUMB_QUALITY).
                                            compressToBitmap(imageFilePath);

                // Create ByteArrayOutputStream to upload Bitmap to Firebase storage:
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, MAX_THUMB_QUALITY, baos);

                // convert the Bitmap data to byte for uploading to Firebase storage:
                final byte[] thumbByte = baos.toByteArray();


                mProgress.setMessage("Uploading...");
                mProgress.show();
                /** Upload image to Firebase Storage:
                    * Filepath for full size image
                    * File path: root/images/profile_images/
                 */
                final StorageReference filepath = mImageStorage.child("profile_images").child(currentUserId+".jpg");

                /** Filepath for thumbnails
                    * Filepath: root/images/thumbnails/
                 */
                final StorageReference thumbFilepath = mImageStorage.child("thumbnails").child(currentUserId+".jpg");

                // Upload the full-sized image first:
                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                        // Upload thumbnail after full-sized image upload is successfull:
                        UploadTask uploadTask = thumbFilepath.putBytes(thumbByte);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String thumbDownloadUrl = taskSnapshot.getDownloadUrl().toString();

                                // Create Hashmap for updated image and thumbnail URLs:
                                Map imageUpdate = new HashMap<String, String>();
                                imageUpdate.put("image",downloadUrl);
                                imageUpdate.put("thumb_image",thumbDownloadUrl);

                                // Update the database:
                                mDatabase.updateChildren(imageUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                            Toast.makeText(SettingsActivity.this, "Profile image updated successfully", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(SettingsActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                });

                                mProgress.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() { // Failure handler for thumbnail Upload Task
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mProgress.dismiss();
                                Toast.makeText(SettingsActivity.this, "Thumbnail upload failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {  // Failure handler for image upload
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgress.dismiss();
                        Toast.makeText(SettingsActivity.this, "Image upload failed. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    // Method to generate a random string of size MAX_LENGTH:
    private static String random()
    {
        final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(MAX_LENGTH);
        for(int i=0;i<MAX_LENGTH;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
