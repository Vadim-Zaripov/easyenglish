package com.develop.vadim.english.Broadcasts;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Services.WordCheckService;

import java.util.Calendar;

public class NotificationBroadcast extends BroadcastReceiver {

    public static final String TAG = "ServiceAutoStart";

    public static final String notificationId = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }

        Toast.makeText(context, "V", Toast.LENGTH_LONG).show();

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
        notificationManagerCompat.notify(200, builder.build());

        setUpRepeating(context);
    }

    private void setUpRepeating(Context c) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        Intent intent = new Intent(c, NotificationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(Context c) {
        {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NotificationBroadcast.notificationId, name, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
