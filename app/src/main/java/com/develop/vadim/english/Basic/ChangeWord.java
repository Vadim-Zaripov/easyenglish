package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.develop.vadim.english.R;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

public class ChangeWord extends AppCompatActivity {

    private EditText originalWordEditText;
    private EditText translatedWordEditText;
    private ImageView deleteWordImageView;
    private ImageView saveChangesImageView;

    private Word changingWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        changingWord = getIntent().getParcelableExtra(getString(R.string.changeWord));

        saveChangesImageView = findViewById(R.id.saveChangesImageView);
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
                                changingWord.removeWordFromService();
                                onBackPressed();
                                iosDialog.dismiss();
                            }
                        })
                        .build()
                        .show();
            }
        });

        saveChangesImageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                saveChanges();
            }
        });

        originalWordEditText.setText(changingWord.getWordInEnglish());
        translatedWordEditText.setText(changingWord.getWordInRussian());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
