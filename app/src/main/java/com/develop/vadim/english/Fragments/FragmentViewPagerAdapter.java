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
    private WordCheckFragment wordCheckFragment = new WordCheckFragment();


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
            case 3:
                return wordCheckFragment;
        }

        return null;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return FragmentPagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
