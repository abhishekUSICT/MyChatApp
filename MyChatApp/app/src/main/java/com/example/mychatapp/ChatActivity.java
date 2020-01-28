package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageSenderID,myUri="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String checker="";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        initialize();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void initialize()
    {
        ChatToolBar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        getSupportActionBar().setTitle(messageReceiverName);
        SendMessageButton=findViewById(R.id.send_message);
        MessageInputText=findViewById(R.id.input_message);
        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=findViewById(R.id.message_list);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.dropdown_chat,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.compress)
        {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.send_file)
        {
            CharSequence options[]=new CharSequence[]
                    {
                            "Image","Pdf file","Word file"
                    };
            AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Select File");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(i==0)
                    {
                        checker="image";
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent,"Select image"),438);
                    }
                    else if(i==1)
                    {
                        checker="pdf";
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        startActivityForResult(intent.createChooser(intent,"Select pdf file"),438);
                    }
                    else if(i==2)
                    {
                        checker="docx";
                        Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/msword");
                        startActivityForResult(intent.createChooser(intent,"Select Word file"),438);
                    }
                }
            });
            builder.show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data.getData()!=null)
        {
            fileUri=data.getData();
            if(checker.equals("image"))
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath=storageReference.child(messagePushID+".jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl=task.getResult();
                            myUri=downloadUrl.toString();
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUri);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            //messageTextBody.put("time", saveCurrentTime);
                            //messageTextBody.put("date", saveCurrentDate);
                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (!task.isSuccessful())
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else if(!checker.equals("image"))
            {
                final StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Documents");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath=storageReference.child(messagePushID+"."+checker);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", downloadUrl);
                                    messageTextBody.put("name", fileUri.getLastPathSegment());
                                    messageTextBody.put("type", checker);
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("to", messageReceiverID);
                                    messageTextBody.put("messageID", messagePushID);
                                    //messageTextBody.put("time", saveCurrentTime);
                                    //messageTextBody.put("date", saveCurrentDate);
                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
                                    RootRef.updateChildren(messageBodyDetails);
                                }
                        }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChatActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendMessage()
    {
        String messageText=MessageInputText.getText().toString();
        if(messageText.length()==0)
        {
            Toast.makeText(this, "Type your message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            //messageTextBody.put("time", saveCurrentTime);
            //messageTextBody.put("date", saveCurrentDate);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (!task.isSuccessful())
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    MessageInputText.setText("");
                }
            });
        }
    }
}
