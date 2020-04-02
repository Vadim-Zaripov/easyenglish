package com.develop.vadim.english.Basic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.develop.vadim.english.Broadcasts.WordCheckBroadcast;
import com.develop.vadim.english.Fragments.FragmentViewPagerAdapter;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Services.WordCheckService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FragmentViewPagerAdapter fragmentViewPagerAdapter;
    ViewPager viewPager;

    public static final String MAIN_ACTIVITY_TAG = "MainActivity";

    public static long index;

    public static String ind;

    public static final String PARCELABLE_EXTRA = "Parcelable";

    private SharedPreferences wordsCheckSharedPreferences;

    public String NOW_DATE;
    public static String NEXT_DATE;
    public String WEEK_DATE, MONTH_DATE, THREE_MONTH_DATE, SIX_MONTH_DATE;
    public DateFormat format;
    public Date now;
    public Calendar calendar = Calendar.getInstance();
    public GregorianCalendar gregorianCalendar = new GregorianCalendar();

    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

    public final static int STATUS_START = 100;
    public final static int STATUS_FINISH = 200;

    public final static String PARAM_STATUS = "Status";

    public final static String BROADCAST_ACTION = "ru.lett.xenous.action.BROADCAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDefaultFiles();
        initDate();
        createNotificationChannel();

        wordsCheckSharedPreferences = getSharedPreferences(getPackageName() + ".wordsCheckFlag", MODE_PRIVATE);

        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.mainViewPagerId);
        viewPager.setAdapter(fragmentViewPagerAdapter);

        DotsIndicator dotsIndicator = findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        FirebaseDynamicLinks
                .getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink;
                        if(pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            String[] splitedLink = deepLink.toString().split("/");

                            String category = splitedLink[splitedLink.length - 1];
                            String sharingUserUid = splitedLink[splitedLink.length - 2];

                            new Thread(new StartAddingCategoryFromLink(category, sharingUserUid)).start();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "FAILED");
                    }
                });

        callCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        callCheck();
    }

    @Override
    public void onBackPressed() { }


    private void callCheck() {
            if(wordsCheckSharedPreferences.getInt(getPackageName() + ".wordsCheckFlag", -1) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            Intent intent = new Intent(this, WordCheckService.class);
            startService(intent);

            BroadcastReceiver wordsCheckBroadCastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch(intent.getIntExtra(PARAM_STATUS, 0)) {
                        case STATUS_START:
                            Toast.makeText(context, "Загружаем слова", Toast.LENGTH_LONG).show();
                            break;
                        case STATUS_FINISH:
                            callWordsCheck();
                            break;
                    }
                }
            };

            IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
            registerReceiver(wordsCheckBroadCastReceiver, intentFilter);

            wordsCheckSharedPreferences.edit().putInt(getPackageName() + ".wordsCheckFlag", Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
        }
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(WordCheckBroadcast.notificationId, name, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void initDefaultFiles() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    //       FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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
        Intent wordCheckIntent = new Intent(this, WordCheckActivity.class);
        wordCheckIntent.putExtra(getString(R.string.parcelableWordKey), false);

        startActivity(wordCheckIntent);
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

    private class StartAddingCategoryFromLink implements Runnable {
        private String category;
        private String sharingUserUid;
        private List<Word> sharingWordList = new ArrayList<>();

        private StartAddingCategoryFromLink(String category, String sharingUserUid) {
            this.category = category;
            this.sharingUserUid = sharingUserUid;
        }

        @Override
        public void run() {
            final DatabaseReference sharingUserReference  = FirebaseDatabase.getInstance().getReference().child("users").child(sharingUserUid).child("words");
            sharingUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                        if(Objects.equals(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue(), category)) {
                            Log.d("HUY", "bib");
                            Word newWord = new Word(wordsCounter);
                            newWord.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                            newWord.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                            newWord.setWordCategory(category);

                            sharingWordList.add(newWord);
                        }
                    }

                    for(int wordsCounter = 0; wordsCounter < sharingWordList.size(); wordsCounter++) {
                        Log.d("BIBKA", String.valueOf(sharingWordList.size()));
                        sharingWordList.get(wordsCounter).sentWordToService();
                    }

                    FirebaseDatabase.getInstance().getReference().child("users").child(sharingUserUid).child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            FirebaseDatabase.getInstance().getReference().child("users").child(sharingUserUid).child("categories").child(String.valueOf(dataSnapshot.getChildrenCount())).setValue(category);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
