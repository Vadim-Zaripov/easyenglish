package com.develop.vadim.english.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.develop.vadim.english.R;
import com.develop.vadim.english.Services.WordCheckService;

public class WordCheckBroadcast extends BroadcastReceiver {

    public static final String TAG = "ServiceAutoStart";

    public static final String notificationId = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WordCheckService.class));

        SharedPreferences sharedPreferences = context.getSharedPreferences("Main Activity SP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.word_check_flag), true);
        editor.apply();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Lett")
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        Log.d(TAG, "MSG");

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(200, builder.build());
    }
}
