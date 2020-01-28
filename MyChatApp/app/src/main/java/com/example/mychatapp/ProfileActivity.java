package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button save_changes;
    private EditText user_name,status,phone_number;
    private ImageView image;
    private String current_user_id,image_url;
    private FirebaseAuth user_auth;
    private DatabaseReference root;
    private StorageReference user_profile_image_reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initialize();

        user_auth=FirebaseAuth.getInstance();
        user_profile_image_reference= FirebaseStorage.getInstance().getReference().child("Profile");
        current_user_id=user_auth.getCurrentUser().getUid();
        root= FirebaseDatabase.getInstance().getReference();

        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveChanges();
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery_intent=new Intent();
                gallery_intent.setAction(Intent.ACTION_PICK);
                gallery_intent.setType("image/*");
                //Intent chooser = Intent.createChooser(gallery_intent, "Complete Action using..");
                //gallery_intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(gallery_intent,1);
                //startActivityForResult(Intent.createChooser(gallery_intent, "Select Picture"), 1);
            }
        });
        GetUserInformation();
    }
    private void initialize()
    {
        save_changes=findViewById(R.id.save_changes);
        user_name=findViewById(R.id.user_name);
        status=findViewById(R.id.status);
        phone_number=findViewById(R.id.phone_number);
        image=findViewById(R.id.profile_photo);
    }
    private void SaveChanges() {
        String set_username=user_name.getText().toString();
        String set_status=status.getText().toString();
        String set_phone_number=phone_number.getText().toString();
        if(set_username==null || set_username.length()==0)
            Toast.makeText(this, "Invalid User name", Toast.LENGTH_SHORT).show();
        else if(set_status==null || set_status.length()==0)
            Toast.makeText(this, "Status empty", Toast.LENGTH_SHORT).show();
        else if(set_phone_number==null || set_phone_number.length()==0)
            Toast.makeText(this, "Invalid Phone number", Toast.LENGTH_SHORT).show();
        else
        {
            HashMap<String,String> profile=new HashMap<>();
            if(image_url!=null)
                profile.put("image",image_url);
            profile.put("uid",current_user_id);
            profile.put("name",set_username);
            profile.put("status",set_status);
            profile.put("phone_no",set_phone_number);
            root.child("Users").child(current_user_id).setValue(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        GoToMainActivity();
                    }
                    else
                        Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void GetUserInformation()
    {
        root.child("Users").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String get_user_name=null,get_status=null,get_image=null,get_phone_no=null;
                    if(dataSnapshot.hasChild("name"))
                    get_user_name=dataSnapshot.child("name").getValue().toString();
                    if(dataSnapshot.hasChild("status"))
                    get_status=dataSnapshot.child("status").getValue().toString();
                    if(dataSnapshot.hasChild("image"))
                    get_image=dataSnapshot.child("image").getValue().toString();
                    if(dataSnapshot.hasChild("phone_no"))
                    get_phone_no=dataSnapshot.child("phone_no").getValue().toString();
                    if(get_user_name!=null)
                    user_name.setText(get_user_name);
                    if(get_status!=null)
                    status.setText(get_status);
                    if(get_phone_no!=null)
                    phone_number.setText(get_phone_no);
                    if(get_image!=null)
                    {
                        Picasso.get().load(get_image).into(image);
                        image_url=get_image;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GoToMainActivity();
    }

    private void GoToMainActivity()
    {
        Intent main_intent=new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(main_intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1 && resultCode==RESULT_OK && data!=null)
        {
            //Uri image_uri= data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(ProfileActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final StorageReference file_path=user_profile_image_reference.child(current_user_id+".png");
                file_path.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return file_path.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                            String download_url= task.getResult().toString();
                            root.child("Users").child(current_user_id).child("image").setValue(download_url)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful())
                                                Toast.makeText(ProfileActivity.this, "Unable to upload", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else
                            Toast.makeText(ProfileActivity.this, "Unable to upload", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(ProfileActivity.this, "Unable to upload", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
