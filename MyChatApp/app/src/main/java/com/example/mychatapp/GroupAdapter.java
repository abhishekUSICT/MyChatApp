package com.example.mychatapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
public class GroupAdapter extends ArrayAdapter<Group> {
    public GroupAdapter(Context context, ArrayList<Group> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Group user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_grouplist, parent, false);
        }
        TextView tvName = convertView.findViewById(R.id.receiver_group);
        TextView tvHome = convertView.findViewById(R.id.sender_group);
        tvName.setText(user.name);
        tvHome.setText(user.hometown);
        return convertView;
    }
}