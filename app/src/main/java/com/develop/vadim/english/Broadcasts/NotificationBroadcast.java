package com.develop.vadim.english.Broadcasts;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Services.WordCheckService;

import java.util.Calendar;

public class NotificationBroadcast extends BroadcastReceiver {

    public static final String TAG = "ServiceAutoStart";

    public static final String notificationId = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {

        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Вызов уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(when)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setContentTitle("Lett")
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Log.d(TAG, "MSG");
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        //notificationManagerCompat.notify(200, builder.build());

        //Установка временного сервиса
        Intent serviceIntent = new Intent(context, WordCheckService.class);
        context.startService(serviceIntent);


    }
}
