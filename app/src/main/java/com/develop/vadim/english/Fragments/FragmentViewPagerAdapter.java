package com.develop.vadim.english.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.develop.vadim.english.Services.WordCheckService;

public class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

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
                return wordsUserCheckFragment;
            case 1:
                return addNewWordFragment;
            case 2:
                return wordsArchiveFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
