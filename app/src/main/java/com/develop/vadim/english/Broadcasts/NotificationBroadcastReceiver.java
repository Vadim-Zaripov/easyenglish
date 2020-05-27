package com.develop.vadim.english.Broadcasts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Basic.Word;
import com.develop.vadim.english.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 211;

    public static final String TAG = "Lett Notify";
    public static final String NOTIFICATION_ID_KEY = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {
        {
            AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent myIntent;
            PendingIntent pendingIntent;

            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);

            myIntent = new Intent(context, NotificationBroadcastReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context,0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(pendingIntent);
            manager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis() + Word.CHECK_INTERVAL.get(Word.LEVEL_DAY),
                    pendingIntent
            );
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Вызов уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_ID_KEY)
                .setSmallIcon(R.drawable.app_icon_small)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200, builder.build());
    }
}
