package com.example.mychatapp;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private RecyclerView contact_list;
    private View contact_view;
    private String current_user_id;
    private FirebaseAuth user_auth;
    private DatabaseReference contact_reference,user_reference;
    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user_reference= FirebaseDatabase.getInstance().getReference().child("Users");
        user_auth=FirebaseAuth.getInstance();
        current_user_id=user_auth.getCurrentUser().getUid();
        contact_reference= FirebaseDatabase.getInstance().getReference().child("Contacts").child(current_user_id);
        contact_view=inflater.inflate(R.layout.fragment_contact, container, false);
        contact_list=contact_view.findViewById(R.id.contact_list);
        contact_list.setLayoutManager(new LinearLayoutManager(getContext()));
        return contact_view;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(contact_reference,Contact.class)
                .build();
        final FirebaseRecyclerAdapter<Contact,ContactViewHolder> contact_adapter=new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contact model) {
                final String user_id=getRef(position).getKey();
                user_reference.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            String profile_name = dataSnapshot.child("name").getValue().toString();
                            holder.user_name.setText(profile_name);
                            if (dataSnapshot.hasChild("image"))
                            {
                                String profile_image = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(profile_image).placeholder(R.drawable.default_image).into(holder.profile_image);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.display_user_layout,parent,false);
                ContactViewHolder viewHolder=new ContactViewHolder(view);
                return viewHolder;
            }
        };
        contact_list.setAdapter(contact_adapter);
        contact_adapter.startListening();
    }
    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView user_name;
        ImageView profile_image;

        public ContactViewHolder(@NonNull View itemView)
        {
            super(itemView);

            user_name = itemView.findViewById(R.id.user_name_display);
            profile_image = itemView.findViewById(R.id.user_image);
        }
    }
}
