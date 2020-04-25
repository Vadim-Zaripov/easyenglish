package com.develop.vadim.english.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WordCheckFragment extends Fragment {

    private EditText userAnswerEditText;
    private TextView userQuestionTextView;
    private LinearLayout helpImageButtonsLinearLayout;
    private TextView rightAnswerTextView;
    private TextView userAnswerTextView;
    private TextView headerTextView;
    private TextView forgetWordTextView;
    private TextView checkAnswerTextView;
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
        checkAnswerTextView = view.findViewById(R.id.checkAnswerTextView);

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
                forgetWordTextView,
                checkAnswerTextView
        };

        if(checkingWordsList.size() == 0) {
            endChecking();
            setUpNoWordsStatus(false);
        }
        else {
            setUpLesson();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(isGoneToChange) {
            continueChecking();

            isGoneToChange = false;
        }
    }

    private void setUpLesson() {
        if(stage == checkingWordsList.size()) {
            endChecking();
            setUpNoWordsStatus(true);

            return;
        }

        appearAnimation();

        userQuestionTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        helpImageButtonsLinearLayout.setVisibility(View.INVISIBLE);
        userAnswerTextView.setVisibility(View.INVISIBLE);


        userAnswerEditText.setTextColor(getResources().getColor(R.color.colorWhite));
        userAnswerEditText.setText("");

        userQuestionTextView.setText(checkingWordsList.get(stage).getWordInRussian());

        continueImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer = userAnswerEditText.getText().toString().trim().replace("\n", "").toLowerCase();

                if(!answer.equals("")) {
                    if(answer.equals(checkingWordsList.get(stage).getWordInEnglish().toLowerCase())) {
                        stage += 1;
                        wordCheckingDisappearAnimation(true);

                        //new AnalyzeUserAnswerThread(stage, true);
                    }
                    else {
                        wordCheckingDisappearAnimation(false);

                        //new AnalyzeUserAnswerThread(stage, false);
                    }
                }
                else {
                    Toast.makeText(getContext(), "Заполните поле перевода", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgetWordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordCheckingDisappearAnimation(false);

                //new Thread(new AnalyzeUserAnswerThread(stage, false));
            }
        });
    }

    private void wordCheckingDisappearAnimation(boolean isAnswerRight) {
        if(isAnswerRight) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(200);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    continueImageView.setClickable(false);
                    forgetWordTextView.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    continueImageView.setClickable(true);

                    userQuestionTextView.setVisibility(View.INVISIBLE);
                    userAnswerEditText.setVisibility(View.INVISIBLE);
                    forgetWordTextView.setVisibility(View.INVISIBLE);

                    setUpLesson();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    userAnswerEditText.startAnimation(alphaAnimation);
                    userQuestionTextView.startAnimation(alphaAnimation);
                    forgetWordTextView.startAnimation(alphaAnimation);
                }
            }, 300);

            userAnswerEditText.setTextColor(getResources().getColor(R.color.rightWordGreen));
        }
        else {
            userAnswerEditText.setTextColor(getResources().getColor(R.color.wrongWordRed));
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(200);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    userAnswerEditText.setVisibility(View.INVISIBLE);
                    headerTextView.setVisibility(View.INVISIBLE);
                    userQuestionTextView.setVisibility(View.INVISIBLE);
                    forgetWordTextView.setVisibility(View.INVISIBLE);
                    checkAnswerTextView.setVisibility(View.INVISIBLE);

                    callMistakeAnalyze();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkAnswerTextView.startAnimation(alphaAnimation);
                    userQuestionTextView.startAnimation(alphaAnimation);
                    headerTextView.startAnimation(alphaAnimation);
                    userAnswerEditText.startAnimation(alphaAnimation);
                    forgetWordTextView.startAnimation(alphaAnimation);
                }
            }, 300);
        }
    }

    private void appearAnimation() {
        Animation appearAnimation = new AlphaAnimation(0f, 1f);
        appearAnimation.setDuration(200);

        userQuestionTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        userQuestionTextView.setVisibility(View.VISIBLE);
        userAnswerEditText.setVisibility(View.VISIBLE);
        headerTextView.setVisibility(View.VISIBLE);
        forgetWordTextView.setVisibility(View.VISIBLE);
        checkAnswerTextView.setVisibility(View.VISIBLE);

        forgetWordTextView.startAnimation(appearAnimation);
        headerTextView.startAnimation(appearAnimation);
        userAnswerEditText.startAnimation(appearAnimation);
        checkAnswerTextView.startAnimation(appearAnimation);
    }

    private void endChecking() {
        for(View checkingView : checkingViews) {
            checkingView.setClickable(false);
            checkingView.animate()
                    .alphaBy(1)
                    .alpha(0)
                    .start();

            checkingView.setClickable(false);
        }
    }

    private void callConfetti() {
        ((MainActivity)getActivity()).callConfetti();
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

        checkAnswerTextView.startAnimation(appearAnimation);
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

        if(userAnswerEditText.getText().toString().trim().replace("\n", "").equals("")) {
            userQuestionTextView.setText("-");
        }
        else {
            userQuestionTextView.setText(userAnswerEditText.getText());
        }

        rightAnswerTextView.setVisibility(View.VISIBLE);
        userAnswerTextView.setVisibility(View.VISIBLE);
        rightAnswerTextView.setText(checkingWordsList.get(stage).getWordInEnglish());
        rightAnswerTextView.setTextColor(getResources().getColor(R.color.rightWordGreen));

        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ToDo: IOS DIALOG
                new IOSDialog.Builder(view.getContext())
                        .message(getString(R.string.deleteWordMessage))
                        .positiveButtonText("Да")
                        .negativeButtonText("Нет")
                        .positiveClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                //checkingWordsList.get(stage).removeWordFromService(removingWordHandler);
                                continueChecking();

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
                    changeWordIntent.putStringArrayListExtra(getString(R.string.categoriesToChangeWordActivity), getCategoriesArrayListForWordChanging());
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
        checkAnswerTextView.startAnimation(disappearAnimation);
        helpImageButtonsLinearLayout.startAnimation(disappearAnimation);

        disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

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

    private ArrayList<String> getCategoriesArrayListForWordChanging() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Без категории");
        categories.addAll(categoriesList);

        return categories;
    }

    private void setUpNoWordsStatus(boolean isCheckingEndedByUser) {
        TextView noWordsToCheckTextView = viewLayout.findViewById(R.id.noWordsToCheckTextView);

        noWordsToCheckTextView.animate().alphaBy(0).alpha(1).setDuration(300).start();
        noWordsToCheckTextView.setVisibility(View.VISIBLE);

        if(isCheckingEndedByUser) {
            callConfetti();
        }
        else {
            //TODO: set text with next date of checking words
        }
    }

    private class AnalyzeUserAnswerThread implements Runnable {
        int index;
        boolean isAnswerRight;
        long value;

        Word checkingWord;

        AnalyzeUserAnswerThread(int index, boolean isAnswerRight) {
            this.index = index;
            this.isAnswerRight = isAnswerRight;
            checkingWord = checkingWordsList.get(index);

            if(isAnswerRight) {
                value = checkingWord.getLevel() + 1L;
            }
            else {
                value = Word.LEVEL_DAY;
            }
        }

        @Override
        public void run() {
            databaseReference.child(String.valueOf(checkingWord.getIndex())).child(Word.levelDatabaseKey).setValue(value).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();

                    Toast.makeText(getContext(), "Произошла ошибка. Проверьте подключение к сети", Toast.LENGTH_LONG).show();
                }
            });


            Calendar cal = new GregorianCalendar();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            databaseReference
                    .child(String.valueOf(checkingWord.getIndex()))
                    .child(Word.dateKey)
                    .setValue(
                            cal.getTimeInMillis() + Word.CHECK_INTERVAL.get((int)value)
                    );
        }
    }
}
