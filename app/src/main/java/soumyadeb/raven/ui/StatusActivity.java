package soumyadeb.raven.ui;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import soumyadeb.raven.R;

public class StatusActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextInputLayout mNewStatus;
    private Button mSaveChangesBtn;

    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Change Status");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNewStatus = (TextInputLayout)findViewById(R.id.new_status);
        mSaveChangesBtn = (Button)findViewById(R.id.status_save_changes);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Updating your status...");
        mProgress.setCanceledOnTouchOutside(false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uId);

        mSaveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = mNewStatus.getEditText().getText().toString();
                if(!TextUtils.isEmpty(newStatus)){
                    mProgress.show();
                    mDatabase.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgress.dismiss();
                            if(task.isSuccessful()) {
                                Toast.makeText(StatusActivity.this, "Status updated successfully", Toast.LENGTH_LONG).show();
                                finish();
                            }
                            else {
                                Toast.makeText(StatusActivity.this,"Failed to update your status. Please try again.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    Snackbar.make(v, "All fields are mandatory",Snackbar.LENGTH_LONG).show();
                    mNewStatus.getEditText().setError("You can't leave this empty");
                }
            }
        });

    }
}
