package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position==0)
        return new ChatFragment();
        else if(position==1)
        return new GroupFragment();
        else if(position==2)
        return new ContactFragment();
        else
        return null;
    }

    @Override
    public int getCount() {
        return 3; // 3 fragments are present
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0)
        return "Chats";
        else if(position==1)
        return "Groups";
        else if(position==2)
        return "Contacts";
        else
        return null;
    }
}
