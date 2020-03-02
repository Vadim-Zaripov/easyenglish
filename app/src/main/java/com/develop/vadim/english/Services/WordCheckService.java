package com.develop.vadim.english.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class WordCheckService extends Service {


    public final static String SERVICE_TAG = "WordService";

    NotificationManager notificationManager;

    ExecutorService executorService;

    public static final String wordsFromServiceIntentKey = "WORDSFROMSERVICE";

    private SharedPreferences sharedPreferences;

    private DatabaseReference databaseReference;
    private long databaseReferenceChildrenCount;

    @Override
    public void onCreate() {
        super.onCreate();

        databaseReference = MainActivity.reference.child("words");
        sharedPreferences = getSharedPreferences("Shared preferences for Words Service", MODE_PRIVATE);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d(SERVICE_TAG, "Service has been started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent pendingIntent = intent.getParcelableExtra(MainActivity.PARCELABLE_EXTRA);
        new Thread(new WordCheckRunnable(pendingIntent)).start();


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(SERVICE_TAG, "Service has been destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class WordCheckRunnable implements Runnable {
        PendingIntent pendingIntent;
        Intent intent;
        List<Word> neededWordsList = new ArrayList<>();

        WordCheckRunnable(PendingIntent pendingIntent) {
            this.pendingIntent = pendingIntent;
            intent = new Intent(MainActivity.BROADCAST_ACTION);
        }

        @Override
        public void run() {
            neededWordsList.clear();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    WordCheckService.this.databaseReferenceChildrenCount = dataSnapshot.getChildrenCount();

                    for(int childrenOnReference = 0; childrenOnReference < databaseReferenceChildrenCount; childrenOnReference++) {
                        final long childrenOnReferenceCurrentIndex = childrenOnReference;
                        if(isNeedChecking(childrenOnReferenceCurrentIndex)) {
                            Word word = new Word(childrenOnReferenceCurrentIndex);
                            word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenOnReferenceCurrentIndex)).child(Word.englishDatabaseKey).getValue()).toString());
                            word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenOnReferenceCurrentIndex)).child(Word.russianDatabaseKey).getValue()).toString());

                            neededWordsList.add(word);
                        }
                    }

                    saveData();

                    Log.d(MainActivity.MAIN_ACTIVITY_TAG, MainActivity.BROADCAST_ACTION);

                    intent.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_FINISH);
                    LocalBroadcastManager.getInstance(WordCheckService.this).sendBroadcast(intent);

                    stopSelf();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private synchronized void saveData() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(neededWordsList);
            editor.putString(getString(R.string.service_saved_indexes_key), json);
            editor.apply();
        }

        private boolean isNeedChecking(long index) {
            return true;
        }
    }


}