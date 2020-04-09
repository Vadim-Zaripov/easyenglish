package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
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
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WordCheckActivity extends AppCompatActivity {
    private EditText userAnswerEditText;
    private TextView userQuestionTextView;
    private LinearLayout helpImageButtonsLinearLayout;
    private TextView rightAnswerTextView;
    private TextView userAnswerTextView;
    private TextView headerTextView;
    private ImageView editWordImageView;
    private ImageView deleteWordImageView;
    private ImageView continueImageView;

    private final long animationDuration = 400;

    private DatabaseReference databaseReference;

    private ArrayList<Word> checkingWordsList;

    private Handler removingWordHandler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_word);

        databaseReference = MainActivity.reference;
        rightAnswerTextView = findViewById(R.id.rightAnswerTextView);
        userAnswerEditText = findViewById(R.id.checkWordEditText);
        userQuestionTextView = findViewById(R.id.wordCheckTextView);
        userAnswerTextView = findViewById(R.id.userAnswerTextView);
        headerTextView = findViewById(R.id.headerTextView);
        editWordImageView = findViewById(R.id.editImageView);
        deleteWordImageView = findViewById(R.id.deleteWordImageView);
        continueImageView = findViewById(R.id.continueImageView);
        helpImageButtonsLinearLayout = findViewById(R.id.linearLayout3);

        if(getIntent().getBooleanExtra(getString(R.string.word_check_flag), false)) {
            checkingWordsList = getIntent().getParcelableArrayListExtra(getString(R.string.parcelableWordKey));
        }
        else {
            checkingWordsList = loadData();

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.archivedWordsSharedPreferences), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.clear().apply();
        }

        setUpLesson(0);
    }

    private void setUpLesson(final int stage) {

        helpImageButtonsLinearLayout.animate().alphaBy(1).alpha(0).setDuration(animationDuration).start();

        userAnswerEditText.setVisibility(View.VISIBLE);
        userAnswerTextView.setVisibility(View.INVISIBLE);
        rightAnswerTextView.setVisibility(View.INVISIBLE);

        if(checkingWordsList == null || checkingWordsList.size() == 0) {
            finish();

            return;
        }

        if(stage != checkingWordsList.size()) {
            userQuestionTextView.setText(checkingWordsList.get(stage).getWordInRussian());
            continueImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userAnswerEditText.getText().toString().equals(checkingWordsList.get(stage).getWordInEnglish())) {
                        //new Thread(new AnalyzeUserAnswerThread(stage, true)).start();
                    }
                    else {
                        userAnswerEditText.setTextColor(getResources().getColor(R.color.wrongWordRed));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                                alphaAnimation.setDuration(400);
                                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) { }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        callUserHelp(stage);
                                        userAnswerEditText.setVisibility(View.INVISIBLE);
                                        //userQuestionTextView.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) { }
                                });

                                userAnswerEditText.startAnimation(alphaAnimation);
                                userQuestionTextView.startAnimation(alphaAnimation);
                            }
                        }, 450);

                        //new Thread(new AnalyzeUserAnswerThread(stage, false)).start();
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Отлично!", Toast.LENGTH_SHORT).show();

            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(WordCheckActivity.this);

            startActivity(new Intent(WordCheckActivity.this, MainActivity.class), activityOptions.toBundle());
        }

        //editData();
    }

    @SuppressLint("HandlerLeak")
    private void callUserHelp(final int stage) {
        userQuestionTextView.setVisibility(View.VISIBLE);
        userQuestionTextView.setTextColor(getResources().getColor(R.color.wrongWordRed));
        userQuestionTextView.animate().alphaBy(0).alpha(1).setDuration(600).start();

        removingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                setUpLesson(stage + 1);
            }
        };

        helpImageButtonsLinearLayout.setVisibility(View.VISIBLE);
        helpImageButtonsLinearLayout.animate().alphaBy(0).alpha(1).setDuration(animationDuration).start();
        helpImageButtonsLinearLayout.setClickable(false);

        headerTextView.animate().alphaBy(1).alpha(0).setDuration(animationDuration).start();

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
        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();

        userQuestionTextView.setText(userAnswerEditText.getText());
        rightAnswerTextView.setVisibility(View.VISIBLE);
        userAnswerTextView.setVisibility(View.VISIBLE);
        rightAnswerTextView.setText("Говно");
        rightAnswerTextView.setTextColor(getResources().getColor(R.color.rightWordGreen));

        editWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changeWordIntent = new Intent(v.getContext(), ChangeWord.class);
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
                setUpLesson(stage + 1);
            }
        });
    }

    private ArrayList<Word> loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("Shared preferences for Words Service", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.service_saved_indexes_key), null);
        Type type = new TypeToken<List<Word>>() {}.getType();
        if(json == null) {
            Log.d("JSON", "NULL");

            return new ArrayList<>();
        }

        return gson.fromJson(json, type);
    }

    private synchronized void editData() {
        SharedPreferences.Editor editor = getSharedPreferences("Shared preferences for Words Service", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(checkingWordsList.remove(0));
        editor.putString(getString(R.string.service_saved_indexes_key), json);
        editor.apply();
    }

    private class AnalyzeUserAnswerThread implements Runnable {
        int index;
        boolean isAnswerRight;

        AnalyzeUserAnswerThread(int index, boolean isAnswerRight) {
            this.index = index;
            this.isAnswerRight = isAnswerRight;
        }

        @Override
        public void run() {
            Word checkingWord = checkingWordsList.get(index);
            if(isAnswerRight) {
                MainActivity.reference.child("words").child(String.valueOf(checkingWord.getIndex())).child(Word.levelDatabaseKey).setValue(checkingWord.getLevel() + 1);
            }
            else {
                MainActivity.reference.child("words").child(String.valueOf(checkingWord.getIndex())).child(Word.levelDatabaseKey).setValue(0);
            }
        }
    }
}
