package com.develop.vadim.english.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.develop.vadim.english.Basic.ChangeWord;
import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Basic.Word;
import com.develop.vadim.english.Basic.WordCheckActivity;
import com.develop.vadim.english.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.util.ArrayList;
import java.util.Date;

public class WordCheckFragment extends Fragment {

    private EditText userAnswerEditText;
    private TextView userQuestionTextView;
    private LinearLayout helpImageButtonsLinearLayout;
    private TextView rightAnswerTextView;
    private TextView userAnswerTextView;
    private TextView headerTextView;
    private TextView forgetWordTextView;
    private ImageView editWordImageView;
    private ImageView deleteWordImageView;
    private ImageView continueImageView;

    private View[] checkingViews;

    private final long animationDuration = 400;

    private DatabaseReference databaseReference;

    private ArrayList<Word> checkingWordsList = new ArrayList<>();
    private ArrayList<String> categoriesList = new ArrayList<>();

    private int width;

    private Handler removingWordHandler;
    private int stage = 0;

    private boolean widthFlag = true;
    private boolean isGoneToChange = false;

    private View viewLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        checkingWordsList = ((MainActivity)getActivity()).wordsCheckWordsArrayList;
        checkingWordsList.add(new Word(1));
        checkingWordsList.add(new Word(2));

        categoriesList = ((MainActivity)getActivity()).categoryNames;

        viewLayout = inflater.inflate(R.layout.check_word_layout, container, false);
        return viewLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = MainActivity.reference.child("words");

        rightAnswerTextView = view.findViewById(R.id.rightAnswerTextView);
        userAnswerEditText = view.findViewById(R.id.checkWordEditText);
        userQuestionTextView = view.findViewById(R.id.wordCheckTextView);
        userAnswerTextView = view.findViewById(R.id.userAnswerTextView);
        headerTextView = view.findViewById(R.id.headerTextView);
        editWordImageView = view.findViewById(R.id.editImageView);
        deleteWordImageView = view.findViewById(R.id.deleteWordImageView);
        continueImageView = view.findViewById(R.id.continueImageView);
        helpImageButtonsLinearLayout = view.findViewById(R.id.linearLayout3);
        forgetWordTextView = view.findViewById(R.id.forgetTextView);

        checkingViews = new View[] {
                rightAnswerTextView,
                userAnswerEditText,
                userQuestionTextView,
                userAnswerTextView,
                headerTextView,
                editWordImageView,
                deleteWordImageView,
                continueImageView,
                helpImageButtonsLinearLayout,
                forgetWordTextView
        };

        setUpLesson();

    }

    private void setUpLesson() {
        if(stage == checkingWordsList.size()) {
            endChecking();
            setUpNoWordsStatus();

            return;
        }

        appearAnimation();

        helpImageButtonsLinearLayout.setVisibility(View.INVISIBLE);
        userAnswerTextView.setVisibility(View.INVISIBLE);

        continueImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer = userAnswerEditText.getText().toString().trim().replace("\n", "");
                wordCheckingDisappearAnimation();

                if(answer.equals("")) {
                    if (answer.equals(checkingWordsList.get(stage).getWordInEnglish())) {
                        stage += 1;
                        setUpLesson();
                    }
                    else {
                        callMistakeAnalyze();
                    }
                } else {
                    Toast.makeText(getContext(), "Заполните поле перевода", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgetWordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordCheckingDisappearAnimation();
                callMistakeAnalyze();
            }
        });
    }

    private void wordCheckingDisappearAnimation() {

    }

    private void appearAnimation() {
        Animation appearAnimation = new AlphaAnimation(0f, 1f);
        appearAnimation.setDuration(200);

        userQuestionTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        userQuestionTextView.setVisibility(View.VISIBLE);
        userAnswerEditText.setVisibility(View.VISIBLE);
        headerTextView.setVisibility(View.VISIBLE);
        forgetWordTextView.setVisibility(View.VISIBLE);

        forgetWordTextView.startAnimation(appearAnimation);
        headerTextView.startAnimation(appearAnimation);
        userAnswerEditText.startAnimation(appearAnimation);
    }

    private void endChecking() {
        for(int viewCounter = 0; viewCounter < checkingViews.length; viewCounter++) {
            checkingViews[viewCounter].setClickable(false);
            checkingViews[viewCounter].animate()
                    .alphaBy(1)
                    .alpha(0)
                    .start();
        }
    }

    @SuppressLint("HandlerLeak")
    private void callMistakeAnalyze() {
        Animation appearAnimation = new AlphaAnimation(0f, 1f);
        appearAnimation.setDuration(200);
        appearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                userQuestionTextView.setVisibility(View.VISIBLE);
                helpImageButtonsLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        userQuestionTextView.startAnimation(appearAnimation);
        helpImageButtonsLinearLayout.startAnimation(appearAnimation);
        rightAnswerTextView.startAnimation(appearAnimation);
        userQuestionTextView.startAnimation(appearAnimation);


        userQuestionTextView.setTextColor(getResources().getColor(R.color.wrongWordRed));

        removingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Toast.makeText(getContext(), "Слово успешно удалено", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(getString(R.string.changingWord), checkingWordsList.get(stage));
                intent.putExtra(getString(R.string.removeWordKey), true);

                getActivity().sendBroadcast(intent);
            }
        };

        if(widthFlag) {
            width = continueImageView.getMeasuredWidth();

            widthFlag = false;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(continueImageView.getMeasuredWidth(), continueImageView.getMeasuredHeight());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = continueImageView.getLayoutParams();
                layoutParams.width = val;
                continueImageView.setLayoutParams(layoutParams);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                continueImageView.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                continueImageView.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();

        userQuestionTextView.setText(userAnswerEditText.getText());
        rightAnswerTextView.setVisibility(View.VISIBLE);
        userAnswerTextView.setVisibility(View.VISIBLE);
        rightAnswerTextView.setText(checkingWordsList.get(stage).getWordInEnglish());
        rightAnswerTextView.setTextColor(getResources().getColor(R.color.rightWordGreen));

        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IOSDialog.Builder(view.getContext())
                        .message(getString(R.string.deleteWordMessage))
                        .positiveButtonText("Да")
                        .negativeButtonText("Нет")
                        .positiveClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                checkingWordsList.get(stage).removeWordFromService(removingWordHandler);
                                iosDialog.dismiss();
                            }
                        })
                        .negativeClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                iosDialog.dismiss();
                            }
                        })
                        .build()
                        .show();
            }
        });

        editWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changeWordIntent = new Intent(view.getContext(), ChangeWord.class);
                changeWordIntent.putStringArrayListExtra(getString(R.string.categoriesToChangeWordActivity), categoriesList);
                changeWordIntent.putExtra(getString(R.string.changeWord), checkingWordsList.get(stage));

                isGoneToChange = true;

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity());

                startActivity(changeWordIntent, activityOptions.toBundle());
            }
        });

        continueImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueChecking();
            }
        });

    }

    private void continueChecking() {
        Animation animationAppear = new AlphaAnimation(0f, 1f);
        animationAppear.setDuration(animationDuration);
        Animation disappearAnimation = new AlphaAnimation(1f, 0f);
        disappearAnimation.setDuration(animationDuration / 2);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(continueImageView.getMeasuredWidth(), width);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = continueImageView.getLayoutParams();
                layoutParams.width = val;
                continueImageView.setLayoutParams(layoutParams);
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                continueImageView.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                continueImageView.setClickable(true);
            }
        });

        userAnswerTextView.startAnimation(disappearAnimation);
        rightAnswerTextView.startAnimation(disappearAnimation);
        helpImageButtonsLinearLayout.startAnimation(disappearAnimation);
        disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userAnswerTextView.setVisibility(View.INVISIBLE);
                rightAnswerTextView.setVisibility(View.INVISIBLE);
                helpImageButtonsLinearLayout.setVisibility(View.INVISIBLE);

                stage += 1;
                setUpLesson();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();

    }

    private void setUpNoWordsStatus() {
        TextView noWordsToCheckTextView = viewLayout.findViewById(R.id.noWordsToCheckTextView);

        noWordsToCheckTextView.animate().alphaBy(0).alpha(1).setDuration(300).start();
        noWordsToCheckTextView.setVisibility(View.VISIBLE);
    }
}
