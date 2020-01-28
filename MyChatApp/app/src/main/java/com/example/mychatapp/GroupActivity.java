package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupActivity extends AppCompatActivity {

    private boolean anonymous;
    private EditText user_input_text;
    private ListView group_message_list;
    private ArrayList<Group> messages=new ArrayList<Group>();
    private ArrayAdapter<Group> adapter;
    private ImageButton send_message_button;
    private Toolbar toolbar;
    private String group_name,current_userid,current_username;
    private String current_date,current_time;
    private FirebaseAuth user_auth;
    private DatabaseReference user_reference,group_reference,group_messagekey_reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        group_name=getIntent().getExtras().get("group_name").toString();
        user_auth=FirebaseAuth.getInstance();
        current_userid=user_auth.getCurrentUser().getUid();
        user_reference= FirebaseDatabase.getInstance().getReference().child("Users");
        group_reference= FirebaseDatabase.getInstance().getReference().child("Groups").child(group_name);
        initialize();
        GetUserInformation();
        send_message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageToDatabase();
                user_input_text.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        group_reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                    DisplayMessage(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator it=dataSnapshot.getChildren().iterator();
        while(it.hasNext())
        {
            String chat_date=(String)((DataSnapshot)it.next()).getValue();
            String chat_message=(String)((DataSnapshot)it.next()).getValue();
            String chat_name=(String)((DataSnapshot)it.next()).getValue();
            String chat_time=(String)((DataSnapshot)it.next()).getValue();
            String chat_id=(String)((DataSnapshot)it.next()).getValue();
            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            String added=chat_name+": "+chat_message;
            Group group;
            if(chat_id.equals(current_userid))
                group=new Group("",added);
            else
                group=new Group(added,"");
            messages.add(group);
        }
        adapter.notifyDataSetChanged();
    }

    private void SaveMessageToDatabase() {
        String message=user_input_text.getText().toString();
        String message_key=group_reference.push().getKey();
        if(message.length()==0)
            Toast.makeText(this, "Invalid message", Toast.LENGTH_SHORT).show();
        else
        {
            Calendar date=Calendar.getInstance();
            SimpleDateFormat date_format=new SimpleDateFormat("MMM dd, yyyy");
            current_date=date_format.format(date.getTime());
            Calendar time=Calendar.getInstance();
            SimpleDateFormat time_format=new SimpleDateFormat("hh:mm:ss a");
            current_time=time_format.format(time.getTime());
            HashMap<String,Object> group_message_key=new HashMap<>();
            group_reference.updateChildren(group_message_key);
            group_messagekey_reference=group_reference.child(message_key);
            HashMap<String,Object> message_information=new HashMap<>();
            if(anonymous==false)
                message_information.put("name",current_username);
            else
                message_information.put("name","Anonymous");
            message_information.put("message",message);
            message_information.put("date",current_date);
            message_information.put("time",current_time);
            message_information.put("user_id",current_userid);
            group_messagekey_reference.updateChildren(message_information);
        }
    }

    private void GetUserInformation() {
        user_reference.child(current_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    current_username=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.dropdown_group,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.anonymous)
        {
            GoAnonymous();
        }
        if(item.getItemId()==R.id.group_compress)
        {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
        }
        return true;
    }

    private void GoAnonymous()
    {
        anonymous=true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        anonymous=false;
    }

    private void initialize()
    {
        anonymous=false;
        /*group_message_list=findViewById(R.id.group_message_list);
        adapter=new ArrayAdapter<String>(GroupActivity.this,R.layout.message_list,R.id.message_list_id,messages);
        group_message_list.setAdapter(adapter);*/
        // Construct the data source
        messages = new ArrayList<Group>();
// Create the adapter to convert the array to views
        adapter = new GroupAdapter(GroupActivity.this,messages);
// Attach the adapter to a ListView
        group_message_list = findViewById(R.id.group_message_list);
        group_message_list.setAdapter(adapter);
        user_input_text=findViewById(R.id.group_message_input);
        send_message_button=findViewById(R.id.send_message_image);
        toolbar=findViewById(R.id.group_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(group_name);
    }
}
