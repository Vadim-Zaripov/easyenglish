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
            case 0:
                return new TutorialFragment(R.drawable.tutorial_1);
            case 1:
                return new TutorialFragment(R.drawable.tutorial_2);
            case 2:
                return new TutorialFragment(R.drawable.tutorial_3);
            case 3:
                return new TutorialFragment(R.drawable.tutorial_4);
            case 4:
                return new TutorialFragment(R.drawable.tutorial_5);
            case 5:
                return new TutorialFragment(R.drawable.tutorial_6);
        }

        return null;
    }

    @Override
    public int getCount() {
        return 6;
    }
}
