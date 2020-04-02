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

import java.util.Calendar;

public class WordCheckBroadcast extends BroadcastReceiver {

    public static final String TAG = "ServiceAutoStart";

    public static final String notificationId = "let'sCheckWordsNotify";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("Time Picker", Context.MODE_PRIVATE);

        //Вызов уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Lett")
                .setContentText("Пора повторить слова!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Log.d(TAG, "MSG");
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(200, builder.build());

        //Установка временного сервиса
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, WordCheckBroadcast.class), 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar wakeUpTimeCalendar = Calendar.getInstance();
        wakeUpTimeCalendar.set(Calendar.HOUR_OF_DAY, sharedPreferences.getInt(context.getString(R.string.hourOfDay), 12));
        wakeUpTimeCalendar.set(Calendar.MINUTE, sharedPreferences.getInt(context.getString(R.string.minute), 0));
        wakeUpTimeCalendar.set(Calendar.SECOND, 0);

        Log.d(TAG, String.valueOf(wakeUpTimeCalendar.getTimeInMillis()));
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeCalendar.getTimeInMillis(), pendingIntent);
    }
}
