package com.example.mychatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private View group_view;
    private ListView list_view;
    private DatabaseReference group_reference;
    private ArrayList<String> groups=new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        group_view=inflater.inflate(R.layout.fragment_group, container, false);
        group_reference= FirebaseDatabase.getInstance().getReference().child("Groups");
        initialize();
        RetrieveAndDisplayGroups();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                String group_name=adapterView.getItemAtPosition(pos).toString();
                Intent group_intent=new Intent(getContext(),GroupActivity.class);
                group_intent.putExtra("group_name",group_name);
                startActivity(group_intent);
            } // getContext() is used because this is a fragment, in case of activity it was MainActivity.this
        });
        return group_view;
    }

    private void RetrieveAndDisplayGroups() {
        group_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                Iterator it=dataSnapshot.getChildren().iterator();
                while(it.hasNext())
                {
                    groups.add(((DataSnapshot)it.next()).getKey());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialize()
    {
        list_view=group_view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),R.layout.group_list,R.id.group_list_id,groups);
        list_view.setAdapter(adapter);
    }
}
