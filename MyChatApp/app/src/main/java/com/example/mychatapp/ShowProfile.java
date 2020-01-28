package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ShowProfile extends AppCompatActivity {

    private DatabaseReference user_reference,chat_request_reference,contacts_reference;
    private FirebaseAuth user_auth;
    private TextView user_profile_name,user_profile_status;
    private ImageView user_profile_image;
    private Button send_message_request_button,decline_message_request_button;
    private String receiver_userid,sender_userid,current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);
        user_auth=FirebaseAuth.getInstance();
        sender_userid=user_auth.getCurrentUser().getUid();
        user_reference= FirebaseDatabase.getInstance().getReference().child("Users");
        chat_request_reference=FirebaseDatabase.getInstance().getReference().child("Requests");
        contacts_reference=FirebaseDatabase.getInstance().getReference().child("Contacts");
        current_state="new";
        initialize();
        GetUserInformation();
    }

    private void GetUserInformation() {
        user_reference.child(receiver_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String user_name=dataSnapshot.child("name").getValue().toString();
                String user_status=dataSnapshot.child("status").getValue().toString();
                user_profile_name.setText(user_name);
                user_profile_status.setText(user_status);
                if(dataSnapshot.hasChild("image"))
                {
                    String user_image=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(user_image).placeholder(R.drawable.default_image).into(user_profile_image);
                }
                ManageRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageRequests() {
        chat_request_reference.child(sender_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiver_userid))
                {
                    String type=dataSnapshot.child(receiver_userid).child("type").getValue().toString();
                    if(type.equals("sent"))
                    {
                        current_state="sent";
                        send_message_request_button.setText("Cancel Request");
                    }
                    else if(type.equals("received"))
                    {
                        current_state="received";
                        send_message_request_button.setText("Accept Request");
                        decline_message_request_button.setVisibility(View.VISIBLE);
                        decline_message_request_button.setEnabled(true);
                        decline_message_request_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelRequest();
                            }
                        });
                    }
                }
                else
                {
                    contacts_reference.child(sender_userid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiver_userid))
                            {
                                current_state="friends";
                                send_message_request_button.setText("Remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(!sender_userid.equals(receiver_userid))
        {
            send_message_request_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    send_message_request_button.setEnabled(false);
                    if(current_state.equals("new"))
                    {
                        SendChatRequest();
                    }
                    else if(current_state.equals("sent"))
                    {
                        CancelRequest();
                    }
                    else if(current_state.equals("received"))
                    {
                        AcceptRequest();
                    }
                    else if(current_state.equals("friends"))
                    {
                        RemoveContact();
                    }
                }
            });
        }
        else
        send_message_request_button.setVisibility(View.INVISIBLE);
    }

    private void RemoveContact() {
        contacts_reference.child(sender_userid).child(receiver_userid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contacts_reference.child(receiver_userid).child(sender_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                send_message_request_button.setEnabled(true);
                                                current_state = "new";
                                                send_message_request_button.setText("Send Message");
                                                decline_message_request_button.setVisibility(View.INVISIBLE);
                                                decline_message_request_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptRequest() {
        contacts_reference.child(sender_userid).child(receiver_userid).child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contacts_reference.child(receiver_userid).child(sender_userid).child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                chat_request_reference.child(sender_userid).child(receiver_userid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chat_request_reference.child(receiver_userid).child(sender_userid)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        send_message_request_button.setEnabled(true);
                                                                                        current_state="friends";
                                                                                        send_message_request_button.setText("Remove Contact");
                                                                                        decline_message_request_button.setVisibility(View.INVISIBLE);
                                                                                        decline_message_request_button.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelRequest() {
        chat_request_reference.child(sender_userid).child(receiver_userid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chat_request_reference.child(receiver_userid).child(sender_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                send_message_request_button.setEnabled(true);
                                                current_state = "new";
                                                send_message_request_button.setText("Send Message");
                                                decline_message_request_button.setVisibility(View.INVISIBLE);
                                                decline_message_request_button.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {

        chat_request_reference.child(sender_userid).child(receiver_userid).child("type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            chat_request_reference.child(receiver_userid).child(sender_userid)
                                    .child("type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                send_message_request_button.setEnabled(true);
                                                current_state="sent";
                                                send_message_request_button.setText("Cancel Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void initialize() {
        receiver_userid=getIntent().getExtras().get("user_id_visit").toString();
        user_profile_image=findViewById(R.id.visit_profile_image);
        send_message_request_button=findViewById(R.id.send_message_request_button);
        decline_message_request_button=findViewById(R.id.decline_message_request_button);
        user_profile_name=findViewById(R.id.visit_user_name);
        user_profile_status=findViewById(R.id.visit_profile_status);
    }
}
