package com.develop.vadim.english.Broadcasts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Services.NotificationService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 211;

    public static final String TAG = "Lett Notify";
    public static final String NOTIFICATION_ID_KEY = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);



        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Вызов уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_ID_KEY)
                .setSmallIcon(R.drawable.logolett2)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200, builder.build());
    }
}
