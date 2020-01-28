package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView search_friend_recycle_list;
    private DatabaseReference user_reference;
    private Toolbar search_toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        user_reference= FirebaseDatabase.getInstance().getReference().child("Users");
        initialize();
        search_toolbar.setTitle("Search Friend");
        search_friend_recycle_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(user_reference, Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, SearchViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, SearchViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SearchViewHolder holder, final int position, @NonNull Contact model)
                    {
                        holder.user_name.setText(model.getName());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.default_image).into(holder.user_image);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                String user_id_visit = getRef(position).getKey();
                                Intent show_profile = new Intent(SearchActivity.this, ShowProfile.class);
                                show_profile.putExtra("user_id_visit",user_id_visit);
                                startActivity(show_profile);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.display_user_layout, viewGroup, false);
                        return new SearchViewHolder(view);
                    }
                };
        search_friend_recycle_list.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GoToMainActivity();
    }
    private void GoToMainActivity()
    {
        Intent main_intent=new Intent(SearchActivity.this,MainActivity.class);
        startActivity(main_intent);
        finish();
    }
    private void initialize() {
        search_toolbar=findViewById(R.id.search_toolbar);
        search_friend_recycle_list=findViewById(R.id.search_friend_recycle);
    }
    public static class SearchViewHolder extends RecyclerView.ViewHolder
    {
        TextView user_name;
        ImageView user_image;

        public SearchViewHolder(@NonNull View itemView)
        {
            super(itemView);
            user_name = itemView.findViewById(R.id.user_name_display);
            user_image = itemView.findViewById(R.id.user_image);
        }
    }
}
