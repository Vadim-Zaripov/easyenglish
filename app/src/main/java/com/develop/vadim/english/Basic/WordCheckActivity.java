package com.develop.vadim.english.Basic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.util.ArrayList;
import java.util.Date;

public class WordCheckActivity extends AppCompatActivity {
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

    private final long animationDuration = 400;

    private DatabaseReference databaseReference;

    private ArrayList<Word> checkingWordsList;
    private ArrayList<String> categoriesList = new ArrayList<>();

    private int width;

    private Handler removingWordHandler;

    private boolean widthFlag = true;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_word);

        databaseReference = MainActivity.reference.child("words");
        rightAnswerTextView = findViewById(R.id.rightAnswerTextView);
        userAnswerEditText = findViewById(R.id.checkWordEditText);
        userQuestionTextView = findViewById(R.id.wordCheckTextView);
        userAnswerTextView = findViewById(R.id.userAnswerTextView);
        headerTextView = findViewById(R.id.headerTextView);
        editWordImageView = findViewById(R.id.editImageView);
        deleteWordImageView = findViewById(R.id.deleteWordImageView);
        continueImageView = findViewById(R.id.continueImageView);
        helpImageButtonsLinearLayout = findViewById(R.id.linearLayout3);
        forgetWordTextView = findViewById(R.id.forgetTextView);

        categoriesList = getIntent().getStringArrayListExtra(getString(R.string.categoriesKey));
        checkingWordsList = getIntent().getParcelableArrayListExtra(getString(R.string.wordsToCheckingKey));

        setUpLesson(0);
    }

    private void setUpLesson(final int stage) {
        if(checkingWordsList == null || checkingWordsList.size() == 0) {
            onBackPressed();

            return;
        }

        Animation appearAnimation = new AlphaAnimation(0f, 1f);
        appearAnimation.setDuration(200);

        userQuestionTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        userQuestionTextView.setVisibility(View.VISIBLE);
        userAnswerEditText.setVisibility(View.VISIBLE);
        headerTextView.setVisibility(View.VISIBLE);
        forgetWordTextView.setVisibility(View.VISIBLE);

        helpImageButtonsLinearLayout.setVisibility(View.INVISIBLE);
        userAnswerTextView.setVisibility(View.INVISIBLE);

        forgetWordTextView.startAnimation(appearAnimation);
        headerTextView.startAnimation(appearAnimation);
        userAnswerEditText.startAnimation(appearAnimation);
        userAnswerEditText.setTextColor(getResources().getColor(R.color.colorWhite));
        userAnswerEditText.setText("");

        forgetWordTextView.setClickable(true);

        forgetWordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDisappearAnimation(stage);
            }
        });

        if(stage != checkingWordsList.size()) {
            userQuestionTextView.setText(checkingWordsList.get(stage).getWordInRussian());
            continueImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userAnswerEditText.getText().toString().equals("")) {
                        Toast.makeText(v.getContext(), "Заполните поле для ввода", Toast.LENGTH_LONG).show();
                    }
                    else {
                        startDisappearAnimation(stage);
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Отлично!", Toast.LENGTH_SHORT).show();

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.konfetti), MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(getString(R.string.konfettiKey), true).apply();

            finish();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void startDisappearAnimation(final int stage) {
        if(userAnswerEditText.getText().toString().trim().toLowerCase().equals(checkingWordsList.get(stage).getWordInEnglish())) {
            new Thread(new AnalyzeUserAnswerThread(stage, true)).start();

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

                    setUpLesson(stage + 1);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            userAnswerEditText.startAnimation(alphaAnimation);
            userQuestionTextView.startAnimation(alphaAnimation);
            forgetWordTextView.startAnimation(alphaAnimation);


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

                    callUserHelp(stage);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            userQuestionTextView.startAnimation(alphaAnimation);
            headerTextView.startAnimation(alphaAnimation);
            userAnswerEditText.startAnimation(alphaAnimation);
            forgetWordTextView.startAnimation(alphaAnimation);

            new Thread(new AnalyzeUserAnswerThread(stage, false)).start();
        }
    }


    @SuppressLint("HandlerLeak")
    private void callUserHelp(final int stage) {
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

                Toast.makeText(WordCheckActivity.this, "Слово успешно удалено", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(getString(R.string.changingWord), checkingWordsList.get(stage));
                intent.putExtra(getString(R.string.removeWordKey), true);

                sendBroadcast(intent);

                continueChecking(stage);
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

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                continueImageView.setClickable(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                continueImageView.setClickable(false);

            }
        });
        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();

        userQuestionTextView.setText(userAnswerEditText.getText());
        rightAnswerTextView.setVisibility(View.VISIBLE);
        userAnswerTextView.setVisibility(View.VISIBLE);
        rightAnswerTextView.setText(checkingWordsList.get(stage).getWordInEnglish());
        rightAnswerTextView.setTextColor(getResources().getColor(R.color.rightWordGreen));

        editWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeWordIntent = new Intent(v.getContext(), ChangeWord.class);
                changeWordIntent.putStringArrayListExtra(getString(R.string.categoriesToChangeWordActivity), getCategoriesToCheckWordActivityList());
                changeWordIntent.putExtra(getString(R.string.changeWord), checkingWordsList.get(stage));

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(WordCheckActivity.this);

                startActivity(changeWordIntent, activityOptions.toBundle());
            }
        });

        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IOSDialog.Builder(v.getContext())
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

        userAnswerEditText.setVisibility(View.INVISIBLE);
        continueImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               continueChecking(stage);
            }
        });
    }

    private void continueChecking(final int stage) {
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

                setUpLesson(stage + 1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();

    }

    private ArrayList<String> getCategoriesToCheckWordActivityList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Без категории");
        arrayList.addAll(categoriesList);
        arrayList.add("Добавить");

        return arrayList;
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

                    Toast.makeText(getApplicationContext(), "Произошла ошибка. Проверьте подключение к сети", Toast.LENGTH_LONG).show();

                    onBackPressed();
                }
            });

            databaseReference
                    .child(String.valueOf(checkingWord.getIndex()))
                    .child(Word.dateKey)
                    .setValue(
                            new Date().getTime() + Word.CHECK_INTERVAL.get((int)value)
                    );
        }
    }
}
