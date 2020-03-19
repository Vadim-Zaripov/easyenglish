package com.develop.vadim.english.Fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordCheckDialogFragment extends DialogFragment {

    private Button exitButton;

    public final static String INDEXES_BUNDLE_KEY = "BUNDLE_INDEXES";

    private List<Word> checkingWordsList;

    private TextView taskTextView;
    private EditText answerEditText;
    private TextView repeatingWordTextView;

    private boolean wordCheckFlag;

    public int score = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Title!");

        wordCheckFlag = getArguments().getBoolean(getString(R.string.word_check_flag), false);
        if(wordCheckFlag) {
            this.checkingWordsList = getArguments().getParcelableArrayList(getString(R.string.parcelableWordKey));
        }
        else {
            checkingWordsList = loadData();
        }

        return inflater.inflate(R.layout.activity_word_check, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskTextView = view.findViewById(R.id.wordCheckTaskTextView);
        answerEditText = view.findViewById(R.id.outlinedWordCheckingEditText);
        repeatingWordTextView = view.findViewById(R.id.repeatingWordTextView);
        exitButton = view.findViewById(R.id.exitButton);
        generateTask(0);
    }

    private void generateTask(int stage) {
        if(stage == checkingWordsList.size()) {
            Log.d("BAAABKA", "BIIB");
            dismiss();
            return;
        }

        final byte FROM_RUSSIAN_TO_ENGLISH_KEY = 0;
        final byte FROM_ENGLISH_TO_RUSSIAN_KEY = 1;

        switch(new Random().nextInt(1)) {
            case FROM_RUSSIAN_TO_ENGLISH_KEY:
                fromRussianToEnglishCheck(stage);
                break;
            case FROM_ENGLISH_TO_RUSSIAN_KEY:
                fromEnglishToRussianCheck(stage);
                break;
        }
    }

    private List<Word> loadData() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("Shared preferences for Words Service", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.service_saved_indexes_key), null);
        Type type = new TypeToken<List<Word>>() {}.getType();
        if(json == null) {
            Log.d("JSON", "NULL");
            return new ArrayList<>();
        }

        return gson.fromJson(json, type);
    }

    private void fromRussianToEnglishCheck(final int stage) {
        taskTextView.setText(getString(R.string.from_russian_to_english_task));
        repeatingWordTextView.setText(checkingWordsList.get(stage).getWordInRussian());

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answerEditText.getText().toString().equals(checkingWordsList.get(stage).getWordInEnglish())) {
                    score += 1;
                    Toast.makeText(v.getContext(), "Правильно!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(v.getContext(), "Не правильно!", Toast.LENGTH_SHORT).show();
                }
                generateTask(stage + 1);
                answerEditText.setText("");
            }
        });

    }

    private void foo() {}

    private void fromEnglishToRussianCheck(final int stage) {
        final int finalIndex = stage;
        taskTextView.setText(getString(R.string.from_russian_to_english_task));
        repeatingWordTextView.setText(checkingWordsList.get(stage).getWordInEnglish());

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(answerEditText.getText().toString().equals(checkingWordsList.get(finalIndex).getWordInRussian())) {
                    score += 1;
                    Toast.makeText(v.getContext(), "Правильно!", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(v.getContext(), "Не правильно!", Toast.LENGTH_SHORT).show();
                }
                generateTask(stage+1);
                answerEditText.setText("");
            }
        });
    }
}
