package com.develop.vadim.english.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Broadcasts.NotificationBroadcast;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.develop.vadim.english.Broadcasts.NotificationBroadcast.notificationId;

public class WordCheckService extends Service {
    public final static String SERVICE_TAG = "WordService";

    public static final String wordsFromServiceIntentKey = "WORDSFROMSERVICE";

    private SharedPreferences sharedPreferences;

    private DatabaseReference databaseReference;
    private long databaseReferenceChildrenCount;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Polly", Toast.LENGTH_LONG).show();

        Log.d(SERVICE_TAG, "Service has been started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Polly", Toast.LENGTH_LONG).show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("words");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            boolean isNeedToSendNotification = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long wordsCount = dataSnapshot.getChildrenCount();
                for(int wordsCounter = 0;wordsCounter < (int) wordsCount; wordsCounter++) {
                    Log.d(SERVICE_TAG, dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.levelDatabaseKey).getValue().toString());
                    Word word = new Word(wordsCounter);
                    word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                    word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                    word.setWordCategory(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue()).toString());
                    word.setLevel((long) Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.levelDatabaseKey)).getValue());
                    word.setDate( Long.parseLong(
                            Objects.requireNonNull(
                                    dataSnapshot
                                            .child(String.valueOf(wordsCounter))
                                            .child(Word.dateKey)
                                            .getValue()
                            ).toString()));

                    if(word.getLevel() == Word.LEVEL_ARCHIVED) {
                        //TODO: Replace with normal checking
                        isNeedToSendNotification  = true;

                        break;
                    }
                }

                if(isNeedToSendNotification) {
                    createNotificationChannel();

                    long when = System.currentTimeMillis();

                    Intent notificationIntent = new Intent(WordCheckService.this, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                    //Вызов уведомления
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(WordCheckService.this, notificationId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setWhen(when)
                            .setAutoCancel(false)
                            .setContentTitle("Lett")
                            .setContentText("Пора повторить слова!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    Log.d(SERVICE_TAG, "MSG");
                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

                    notificationManagerCompat.notify(200, builder.build());
                }
                else {
                    Log.d(SERVICE_TAG, "NO WORDS");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(SERVICE_TAG, "ERROR");
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(SERVICE_TAG, "Service has been destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NotificationBroadcast.notificationId, name, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}