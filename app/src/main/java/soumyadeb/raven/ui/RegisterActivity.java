package soumyadeb.raven.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import soumyadeb.raven.R;
import soumyadeb.raven.firebase.FirebaseAssignment;
import soumyadeb.raven.models.User;
import soumyadeb.raven.utility.Tools;

public class RegisterActivity extends AppCompatActivity {

    // UI Instances:
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar toolbar;
    private ProgressDialog mRegProgress;

    // Firebase Instances:
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Data Members:
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        mRegProgress = new ProgressDialog(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDisplayName = (TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn = (Button)findViewById(R.id.reg_create_btn);

        mAuth = FirebaseAuth.getInstance();

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                mRegProgress.setMessage("Registering your account...");
                mRegProgress.setCanceledOnTouchOutside(false);
                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    if(password.length()<6){
                        mPassword.getEditText().setError("Password must be 6 characters long");
                        Snackbar.make(v, "Password must be 6 characters long", Snackbar.LENGTH_LONG).show();
                    }
                    else {
                        mRegProgress.show();
                        registerUser(displayName, email, password, v);
                    }
                }
                else {
                    Snackbar.make(v, "All fields are mandatory", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void registerUser(final String displayName, String email, String password, final View view) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                String uId = currentUser.getUid();

                /* Set database reference to 'users' */
                mDatabase = FirebaseDatabase.getInstance().getReference()
                        .child(FirebaseAssignment.NAME_USERS_DB).child(uId);

                /*
                HashMap<String, String> userData = new HashMap<String, String>();
                userData.put("name",displayName);
                userData.put("status","Hi there! I'm using Slime.");
                userData.put("image","default");
                userData.put("thumb_image","default");
                */

                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                User user = new User(displayName, "Hi there! I'm using Slime.", "default", "default", deviceToken, false);

                mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mRegProgress.dismiss();
                            startActivity(new Intent(RegisterActivity.this,MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mRegProgress.dismiss();
                Tools.showAlert(RegisterActivity.this,"Error","Registration failed. Please try again.");
            }
        });
    }
}
