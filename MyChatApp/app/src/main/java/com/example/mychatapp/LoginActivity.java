package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email_id,password;
    private TextView register,forgot_password,phone_login;
    private Button login_button;
    private FirebaseAuth user_auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
        user_auth=FirebaseAuth.getInstance();
        login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                StartLogin();
            }
        });
        register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GoToRegisterActivity();
            }
        });
        phone_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToPhoneActivity();
            }
        });
    }

    private void GoToPhoneActivity()
    {
        Intent phone_intent=new Intent(LoginActivity.this,PhoneActivity.class);
        startActivity(phone_intent);
    }

    private void StartLogin()
    {
        String s_password=password.getText().toString();
        String s_email=email_id.getText().toString();
        if(s_email==null || s_email.length()==0)
            Toast.makeText(this,"Invalid email",Toast.LENGTH_SHORT).show();
        else if(s_password==null || s_password.length()<6)
            Toast.makeText(this,"Password should have minimum 6 characters",Toast.LENGTH_SHORT).show();
        else
        {
            user_auth.signInWithEmailAndPassword(s_email,s_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        GoToMainActivity();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this,"Unable to login",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void GoToRegisterActivity()
    {
        Intent register_intent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(register_intent);
    }
    private void GoToMainActivity()
    {
        Intent main_intent=new Intent(LoginActivity.this,MainActivity.class);
        main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main_intent);
        finish();
    }
    private void initialize()
    {
        email_id=findViewById(R.id.login_email_id);
        password=findViewById(R.id.login_password);
        register=findViewById(R.id.register);
        forgot_password=findViewById(R.id.forgot_password);
        login_button=findViewById(R.id.login_button);
        phone_login=findViewById(R.id.login_phone);
    }
}
