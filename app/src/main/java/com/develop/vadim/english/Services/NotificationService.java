package com.develop.vadim.english.Services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Basic.Word;
import com.develop.vadim.english.Broadcasts.NotificationBroadcastReceiver;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationService extends Service {
    public NotificationService() { }

    @Override
    public void onCreate() {

        Log.w("NotificationService", "Set alarm to tomorrow");

        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(NotificationService.this);
            startAlarm();
        }

        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAlarm() {

        Toast.makeText(this, "Alarm set to tomorrow", Toast.LENGTH_LONG).show();
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;


        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        myIntent = new Intent(this, NotificationBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.cancel(pendingIntent);
        manager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis()+Word.CHECK_INTERVAL.get(Word.LEVEL_DAY),
                pendingIntent
        );

        //THIS IS WHERE YOU SET NOTIFICATION TIME FOR CASES WHEN THE NOTIFICATION NEEDS TO BE RESCHEDULED
//        myIntent = new Intent(this, NotificationBroadcastReceiver.class);
//        pendingIntent = PendingIntent.getBroadcast(this,0,myIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(Context c) {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NotificationBroadcastReceiver.NOTIFICATION_ID_KEY, name, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            NotificationManager notificationManager = c.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

}
