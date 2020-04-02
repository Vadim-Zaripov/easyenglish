package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.develop.vadim.english.R;
import com.google.android.material.textfield.TextInputEditText;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

public class ChangeWord extends AppCompatActivity {

    private EditText originalWordEditText;
    private EditText translatedWordEditText;
    private ImageView deleteWordImageView;

    private Word changingWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        changingWord = getIntent().getParcelableExtra(getString(R.string.changeWord));

        originalWordEditText = findViewById(R.id.editTextRussian);
        translatedWordEditText = findViewById(R.id.editTextEnglish);
        deleteWordImageView = findViewById(R.id.deleteWordImageView);
        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IOSDialog.Builder(v.getContext())
                        .negativeButtonText("Нет")
                        .positiveButtonText("Да")
                        .message(getString(R.string.deleteWordMessage))
                        .negativeClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                iosDialog.dismiss();
                            }
                        })
                        .positiveClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                iosDialog.dismiss();
                            }
                        })
                        .build()
                        .show();
            }
        });

        originalWordEditText.setText(changingWord.getWordInEnglish());
        translatedWordEditText.setText(changingWord.getWordInRussian());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        saveChanges();
    }

    private void saveChanges() {
        if(originalWordEditText.getText().toString().equals(changingWord.getWordInEnglish())) {
            MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.englishDatabaseKey).setValue(originalWordEditText.getText().toString()) ;
        }

        if(translatedWordEditText.getText().toString().equals(changingWord.getWordInEnglish())) {
            MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.russianDatabaseKey).setValue(translatedWordEditText.getText().toString());
        }
    }
}
