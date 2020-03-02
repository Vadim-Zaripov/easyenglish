package com.develop.vadim.english.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.develop.vadim.english.Fragments.AddNewWordFragment;
import com.develop.vadim.english.Fragments.WordsArchiveFragment;
import com.develop.vadim.english.Fragments.WordsUserCheckFragment;

public class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

    public FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WordsUserCheckFragment();
            case 1:
                return new AddNewWordFragment();
            case 2:
                return new WordsArchiveFragment();
            default:
                return new AddNewWordFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
