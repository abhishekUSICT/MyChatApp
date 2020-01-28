package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {

    private Toolbar phone_toolbar;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callback;
    private PhoneAuthProvider.ForceResendingToken resend_token;
    private EditText phone_number_edit,phone_otp_edit;
    private TextView phone_number_text,phone_otp_text;
    private Button send_otp,verify_user;
    private String verification_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        mAuth=FirebaseAuth.getInstance();
        initialize();
        send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone_number=phone_number_edit.getText().toString();
                if(phone_number.length()==0)
                    Toast.makeText(PhoneActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                else
                {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone_number,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneActivity.this,               // Activity (for callback binding)
                            callback);
                }

            }
        });
        verify_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_otp.setVisibility(View.INVISIBLE);
                phone_number_text.setVisibility(View.INVISIBLE);
                phone_number_edit.setVisibility(View.INVISIBLE);
                String verification_code=phone_otp_edit.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification_id,verification_code);
                signInWithPhoneAuthCredential(credential);
            }
        });
        callback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                send_otp.setVisibility(View.VISIBLE);
                phone_number_text.setVisibility(View.VISIBLE);
                phone_number_edit.setVisibility(View.VISIBLE);
                phone_otp_text.setVisibility(View.INVISIBLE);
                phone_otp_edit.setVisibility(View.INVISIBLE);
                verify_user.setVisibility(View.INVISIBLE);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Toast.makeText(PhoneActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Toast.makeText(PhoneActivity.this, "SMS quota exceeded", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(PhoneActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
                // Show a message and update the UI
                // ...
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                verification_id = verificationId;
                resend_token = token;
                Toast.makeText(PhoneActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
                send_otp.setVisibility(View.INVISIBLE);
                phone_number_text.setVisibility(View.INVISIBLE);
                phone_number_edit.setVisibility(View.INVISIBLE);
                phone_otp_text.setVisibility(View.VISIBLE);
                phone_otp_edit.setVisibility(View.VISIBLE);
                verify_user.setVisibility(View.VISIBLE);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            GoToMainActivity();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(PhoneActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(PhoneActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void GoToMainActivity()
    {
        Intent main_intent=new Intent(PhoneActivity.this,MainActivity.class);
        startActivity(main_intent);
        finish();
    }

    private void initialize()
    {
        phone_toolbar=findViewById(R.id.phone_login_toolbar);
        setSupportActionBar(phone_toolbar);
        getSupportActionBar().setTitle("Login Using Phone");
        phone_number_edit=findViewById(R.id.phone_number_edit);
        phone_otp_edit=findViewById(R.id.phone_otp_edit);
        phone_number_text=findViewById(R.id.phone_number_text);
        phone_otp_text=findViewById(R.id.phone_otp_text);
        send_otp=findViewById(R.id.send_otp);
        verify_user=findViewById(R.id.verify_user);
    }
}
