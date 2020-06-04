package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

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
        tutorialViewPager.setAdapter(new TutorialViewPagerAdapter(getSupportFragmentManager(), 0));
        tutorialViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == tutorialViewPager.getChildCount() - 1) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                    alphaAnimation.setDuration(200);

                    continueTextView.setClickable(true);
                    continueTextView.setVisibility(View.VISIBLE);
                    continueTextView.startAnimation(alphaAnimation);
                }
                else {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                    alphaAnimation.setDuration(200);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            continueTextView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });

                    continueTextView.setClickable(false);
                    continueTextView.setVisibility(View.VISIBLE);
                    continueTextView.startAnimation(alphaAnimation);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)  { }
        });

        DotsIndicator dotsIndicator = findViewById(R.id.tutorialDotsIndicator);
        dotsIndicator.setViewPager(tutorialViewPager);
    }
}
