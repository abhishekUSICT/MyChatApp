package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText email_id,password;
    private TextView already_have_an_account;
    private Button register_button;
    private FirebaseAuth user_auth;
    private DatabaseReference root_reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
        user_auth=FirebaseAuth.getInstance();
        root_reference=FirebaseDatabase.getInstance().getReference();
        register_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                RegisterUser();
            }
        });
        already_have_an_account.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GoToLoginActivity();
            }
        });
    }
    private void initialize()
    {
        email_id=findViewById(R.id.register_email_id);
        password=findViewById(R.id.register_password);
        already_have_an_account=findViewById(R.id.already_have_an_account);
        register_button=findViewById(R.id.register_button);
    }
    private void RegisterUser()
    {
        String s_password=password.getText().toString();
        String s_email=email_id.getText().toString();
        if(s_email==null || s_email.length()==0)
        Toast.makeText(this,"Invalid email",Toast.LENGTH_SHORT).show();
        else if(s_password==null || s_password.length()<6)
        Toast.makeText(this,"Password should have minimum 6 characters",Toast.LENGTH_SHORT).show();
        else
        {
            user_auth.createUserWithEmailAndPassword(s_email,s_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String current_user_id=user_auth.getCurrentUser().getUid();
                        root_reference.child("Users").child(current_user_id).setValue("");
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        GoToMainActivity();
                    }
                    else
                        Toast.makeText(RegisterActivity.this,"Unable to register",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void GoToLoginActivity()
    {
        Intent login_intent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(login_intent);
    }
    private void GoToMainActivity()
    {
        Intent main_intent=new Intent(RegisterActivity.this,MainActivity.class);
        main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main_intent);
        finish();
    }
}
