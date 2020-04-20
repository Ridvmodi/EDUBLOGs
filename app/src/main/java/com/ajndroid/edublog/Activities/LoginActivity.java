package com.ajndroid.edublog.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ajndroid.edublog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;


public class LoginActivity extends AppCompatActivity {

    private EditText userMobNo,userOtp;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;
    private ImageView loginPhoto;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String otpSent = "";

    private Boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMobNo = findViewById(R.id.login_mob_no);
        userOtp = findViewById(R.id.login_otp);
        btnLogin = findViewById(R.id.loginBtn);
        loginProgress = findViewById(R.id.login_progress);
        mAuth = FirebaseAuth.getInstance();
        HomeActivity = new Intent(this,com.ajndroid.edublog.Activities.Home.class);
        loginPhoto = findViewById(R.id.login_photo);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    userMobNo.setError("Enter correct mob no");
                    userMobNo.requestFocus();
                    return;
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                isOtpSent = true;
                otpSent = s;
                userOtp.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);

            }
        };



        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                String mobNo = userMobNo.getText().toString();
                String otp = "";

                if (mobNo.isEmpty()) {
                    showMessage("Please Verify All Field");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                } else if(mobNo.length() < 10) {
                    userMobNo.setError("Please enter correct mob no");
                } else {

                    if (mobNo.length() == 10) {
                        mobNo = "+91" + mobNo;
                    }

                    if (isOtpSent) {
                        otp = userOtp.getText().toString();
                        if (otp.isEmpty()) {
                            userOtp.setError("Required");
                            return;
                        }

                    signIn(otp, otpSent);

                    } else {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobNo, 60, TimeUnit.SECONDS, LoginActivity.this, mCallbacks);
                    }

                }
            }
        });
    }

    private void signIn(String otpEntered, String otpSent) {

        PhoneAuthCredential authCredential = PhoneAuthProvider.getCredential(otpSent, otpEntered);

        mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    loginProgress.setVisibility(View.INVISIBLE);
                    btnLogin.setVisibility(View.VISIBLE);
                    updateUI();
                }
                else {
                    showMessage(task.getException().getMessage());
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    private void updateUI() {
        startActivity(HomeActivity);
        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            //user is already connected  so we need to redirect him to home page
            updateUI();
        }
    }
}
