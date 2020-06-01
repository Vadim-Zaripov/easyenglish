package com.develop.vadim.english.Basic.tutorial;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.develop.vadim.english.R;

public class TutorialViewPagerAdapter extends FragmentPagerAdapter {
    public TutorialViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            // TODO: Add more screenshots and cases for them
            case 0:
                return new TutorialFragment(R.drawable.app_background);
            case 1:
                return new TutorialFragment(R.drawable.app_background);
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
