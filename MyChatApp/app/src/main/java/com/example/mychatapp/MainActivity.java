package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tablayout;
    private DatabaseReference root_reference;
    private FragmentAdapter fragmentadapter;
    private ViewPager viewpager;
    private FirebaseUser user;
    private FirebaseAuth user_auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_auth=FirebaseAuth.getInstance();
        user=user_auth.getCurrentUser();
        root_reference= FirebaseDatabase.getInstance().getReference();
        toolbar=findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MyChatApp");
        toolbar.setTitleTextColor(0xFFFFFFFF);
        viewpager=findViewById(R.id.main_viewpager);
        fragmentadapter=new FragmentAdapter(getSupportFragmentManager());
        viewpager.setAdapter(fragmentadapter);
        tablayout=findViewById(R.id.tablayout);
        tablayout.setupWithViewPager(viewpager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(user==null)
        {
            StartLogin();
        }
        else
        {
            CheckUserExistence();
        }
    }
    private void CheckUserExistence()
    {
        String current_user_id;
        if(user_auth.getCurrentUser()!=null)
        current_user_id=user_auth.getCurrentUser().getUid();
        else
            return;
        root_reference.child("Users").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("name").exists())
                {
                    GoToProfileActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /*@Override
    protected void onStop() {
        super.onStop();
        user_auth.signOut();
        user=null;
    }*/

    private void StartLogin()
    {
        Intent login_intent=new Intent(MainActivity.this,LoginActivity.class);
        login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login_intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.dropdown_list,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.new_group)
        {
            CreateGroup();
        }
        else if(item.getItemId()==R.id.search_user)
        {
            GoToSearchActivity();
        }
        else if(item.getItemId()==R.id.profile)
        {
            GoToProfileActivity();
        }
        else if(item.getItemId()==R.id.logout)
        {
            user_auth.signOut();
            StartLogin();
        }
        return true;
    }

    private void CreateGroup()
    {
        AlertDialog.Builder b=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        b.setTitle("Group Name : ");
        final EditText group_name=new EditText(MainActivity.this);
        b.setView(group_name);
        b.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String group=group_name.getText().toString();
                if(group==null || group.length()==0)
                    Toast.makeText(MainActivity.this, "Invalid Group Name", Toast.LENGTH_SHORT).show();
                else
                CreateNewGroup(group);
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        b.show();
    }

    private void CreateNewGroup(String group) {
        root_reference.child("Groups").child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                    Toast.makeText(MainActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void GoToProfileActivity()
    {
        Intent profile_intent=new Intent(MainActivity.this,ProfileActivity.class);
        profile_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profile_intent);
        finish();
    }
    private void GoToSearchActivity()
    {
        Intent search_intent=new Intent(MainActivity.this,SearchActivity.class);
        search_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(search_intent);
        finish();
    }
}
