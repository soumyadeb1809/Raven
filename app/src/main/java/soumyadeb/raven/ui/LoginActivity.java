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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import soumyadeb.raven.R;
import soumyadeb.raven.firebase.FirebaseAssignment;
import soumyadeb.raven.utility.Tools;

public class LoginActivity extends AppCompatActivity {

    // UI Instances:
    private Toolbar toolbar;
    private TextInputLayout mEmail,mPassword;
    private Button mLoginBtn;
    private ProgressDialog mLoginProgress;

    // Firebase Instances:
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Data members:

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*INITIALIZE UI INSTANCES*/
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = (TextInputLayout)findViewById(R.id.login_email);
        mPassword = (TextInputLayout)findViewById(R.id.login_password);
        mLoginBtn = (Button)findViewById(R.id.login_btn);

        mLoginProgress = new ProgressDialog(this);

        /*INITIALIZE FIREBASE INSTANCES*/
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                mLoginProgress.setMessage("Verifying credentials...");
                mLoginProgress.setCanceledOnTouchOutside(false);
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mLoginProgress.show();
                    loginUser(v, email, password);
                }

                else {
                    Snackbar.make(v,"All fields are mandatory",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginUser(final View v, String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mLoginProgress.dismiss();
                if(task.isSuccessful())
                {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String currentUser = mAuth.getCurrentUser().getUid().toString();

                    mDatabase.child(FirebaseAssignment.NAME_USERS_DB).child(currentUser)
                            .child(FirebaseAssignment.KEY_DEVICE_TOKEN).setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Tools.showAlert(LoginActivity.this,"Error","Something went wrong, please try again.");
                        }
                    });


                }
                else {
                    Tools.showAlert(LoginActivity.this,"Error","Login failed. Please check your credentials and try again.");
                }
            }
        });
    }
}
