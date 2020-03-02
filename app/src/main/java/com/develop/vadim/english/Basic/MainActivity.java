package com.develop.vadim.english.Basic;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.develop.vadim.english.Broadcasts.WordCheckBroadcast;
import com.develop.vadim.english.Fragments.FragmentViewPagerAdapter;
import com.develop.vadim.english.Fragments.WordCheckFragment;
import com.develop.vadim.english.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    FragmentViewPagerAdapter fragmentViewPagerAdapter;
    ViewPager viewPager;

    public static final String MAIN_ACTIVITY_TAG = "MainActivity";

    public static long index;

    public static String ind;

    public static final String PARCELABLE_EXTRA = "Parcelable";

    public String NOW_DATE;
    public static String NEXT_DATE;
    public String WEEK_DATE, MONTH_DATE, THREE_MONTH_DATE, SIX_MONTH_DATE;
    public DateFormat format;
    public Date now;

    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

    public final static int STATUS_START = 100;
    public final static int STATUS_FINISH = 200;

    public final static String PARAM_TIME = "time";
    public final static String PARAM_TASK = "task";
    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "ru.lett.xenous.action.BROADCAST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDefaultFiles();
        initDate();
        createNotificationChannel();
        setUpService();

        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.mainViewPagerId);
        viewPager.setAdapter(fragmentViewPagerAdapter);

        callCheck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(MainActivity.MAIN_ACTIVITY_TAG
                , "Start Result");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        callCheck();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void callCheck() {
        SharedPreferences sharedPreferences = getSharedPreferences("Main Activity SP", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean b = sharedPreferences.getBoolean(getString(R.string.word_check_flag), false);
        if(b) {
            callWordsCheck();

            editor.putBoolean(getString(R.string.word_check_flag), false);
            editor.apply();
        }
    }

    public void setUpService() {
        Intent intent = new Intent(this, WordCheckBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long rightNowTime = System.currentTimeMillis();

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, rightNowTime, 60000, pendingIntent);
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(WordCheckBroadcast.notificationId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    private void initDefaultFiles() {
        user = FirebaseAuth.getInstance().getCurrentUser();
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        reference.keepSynced(true);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                index = dataSnapshot.getChildrenCount();
                ind = String.valueOf(index);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void callWordsCheck() {
        WordCheckFragment wordCheckFragment = new WordCheckFragment();
        wordCheckFragment.show(getFragmentManager(), "WordCheckFragment Tag");
    }

    private void initDate() {
        format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        NOW_DATE = format.format(calendar.getTime());
        try {now = format.parse(NOW_DATE);} catch (ParseException e) {}
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        NEXT_DATE = format.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        WEEK_DATE = format.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        calendar.add(Calendar.MONTH, 1);
        MONTH_DATE = format.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 2);
        THREE_MONTH_DATE = format.format(calendar.getTime());
        calendar.add(Calendar.MONTH, 3);
        SIX_MONTH_DATE = format.format(calendar.getTime());
    }
}
