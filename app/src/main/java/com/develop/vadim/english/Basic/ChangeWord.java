package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

    private BroadcastReceiver updateHasBeenDoneBroadcastReceiver;

    private Word changingWord;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        updateHasBeenDoneBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Close Activity", "Bye!");
                onBackPressed();
            }
        };



        final Handler removingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                sendBroadcast(new Intent(MainActivity.BROADCAST_ACTION));
            }
        };

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
                                startActivity(new Intent(ChangeWord.this, WordCheckActivity.class));
                            }
                        })
                        .positiveClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                changingWord.removeWordFromService(removingWordHandler);
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
    protected void onResume() {
        super.onResume();

        registerReceiver(updateHasBeenDoneBroadcastReceiver, new IntentFilter(MainActivity.BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(updateHasBeenDoneBroadcastReceiver);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            unregisterReceiver(updateHasBeenDoneBroadcastReceiver);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
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
