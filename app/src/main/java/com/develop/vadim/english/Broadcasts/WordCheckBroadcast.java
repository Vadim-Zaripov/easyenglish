package com.develop.vadim.english.Broadcasts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lett")
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        Log.d(TAG, "MSG");

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(200, builder.build());



        //Set up alarm service
        Intent alarmServiceIntent = new Intent(context, WordCheckBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long rightNowTime = System.currentTimeMillis();

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, rightNowTime + 60000, pendingIntent);

    }
}
