package com.develop.vadim.english.Basic;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.develop.vadim.english.Basic.tutorial.TutorialViewPagerAdapter;
import com.develop.vadim.english.R;
import com.develop.vadim.english.utils.Utils;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class TutorialActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        TextView continueTextView = findViewById(R.id.continueTextView);
        continueTextView.setOnTouchListener(Utils.loginTouchListener);
        continueTextView.setOnClickListener(view -> {
           getSharedPreferences(getString(R.string.tutorial_key), MODE_PRIVATE).edit().putBoolean(getString(R.string.tutorial_key), true).apply();
           finish();
        });

        ViewPager tutorialViewPager = findViewById(R.id.tutorialViewPager);
        TutorialViewPagerAdapter tutorialViewPagerAdapter = new TutorialViewPagerAdapter(getSupportFragmentManager(), 0);
        tutorialViewPager.setAdapter(tutorialViewPagerAdapter);
        tutorialViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == tutorialViewPagerAdapter.getCount() - 1) {
                    continueTextView.animate().alpha(1f).setDuration(200).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            continueTextView.setVisibility(View.VISIBLE);
                            continueTextView.setClickable(true);
                            continueTextView.setAlpha(0f);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            continueTextView.setAlpha(1f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }
                else {
                    continueTextView.animate().alpha(0f).setDuration(200).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            continueTextView.setClickable(false);
                            continueTextView.setAlpha(1f);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            continueTextView.setAlpha(0f);
                            continueTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)  { }
        });

        DotsIndicator dotsIndicator = findViewById(R.id.tutorialDotsIndicator);
        dotsIndicator.setViewPager(tutorialViewPager);
    }
}
