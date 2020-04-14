package com.develop.vadim.english.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentViewPagerAdapter extends FragmentPagerAdapter {

    public FragmentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private CategoriesFragment wordsUserCheckFragment = new CategoriesFragment();
    private AddNewWordFragment addNewWordFragment = new AddNewWordFragment();
    private WordsArchiveFragment wordsArchiveFragment = new WordsArchiveFragment();


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CategoriesFragment();
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
