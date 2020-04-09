package com.develop.vadim.english.Fragments;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.develop.vadim.english.Services.WordCheckService;

public class FragmentViewPagerAdapter extends FragmentPagerAdapter {

    public FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private WordsUserCheckFragment wordsUserCheckFragment = new WordsUserCheckFragment();
    private AddNewWordFragment addNewWordFragment = new AddNewWordFragment();
    private WordsArchiveFragment wordsArchiveFragment = new WordsArchiveFragment();


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new WordsUserCheckFragment();
            case 1:
                return new AddNewWordFragment();
            case 2:
                return new WordsArchiveFragment();
        }

        return null;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return FragmentPagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
