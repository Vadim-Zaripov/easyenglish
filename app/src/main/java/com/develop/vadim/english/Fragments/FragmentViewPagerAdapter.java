package com.develop.vadim.english.Fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
