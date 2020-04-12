package com.develop.vadim.english.Basic;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.develop.vadim.english.Broadcasts.NotificationBroadcast;
import com.develop.vadim.english.Fragments.AddNewWordFragment;
import com.develop.vadim.english.Fragments.FragmentViewPagerAdapter;
import com.develop.vadim.english.Fragments.WordsArchiveFragment;
import com.develop.vadim.english.Fragments.WordsUserCheckFragment;
import com.develop.vadim.english.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.DoubleBounce;
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
import com.google.gson.Gson;
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

    private FragmentViewPagerAdapter fragmentViewPagerAdapter;
    private ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    public SpinKitView spinKitView;

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
    private DatabaseReference categoryReference = reference.child("categories");

    public final static int STATUS_START = 100;
    public final static int STATUS_FINISH = 200;

    public final static String PARAM_STATUS = "Status";

    public final static String BROADCAST_ACTION = "ru.lett.xenous.action.BROADCAST";
    public final static String BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION = "ru.lett.xenous.action.UPDATE";

    private WordsUserCheckFragment wordsUserCheckFragment;
    private AddNewWordFragment addNewWordFragment;
    private WordsArchiveFragment wordsArchiveFragment;

    public ArrayList<String> categoryNames = new ArrayList<>();
    public ArrayList<Word> wordArrayList = new ArrayList<>();
    private ArrayList<Word> archivedWordsArrayList = new ArrayList<>();

    private boolean isCategoriesLoaded = false;

    private Handler loadingHandler;

    public static final int CATEGORIES_LOAD_END = 0;
    public static final int WORDS_LOAD_END = 1;
    public static final int WORDS_ANALYNG_WND = 3;
    public static final int CHECKING_WORDS_LOAD_END = 4;

    private SharedPreferences archivedWordsSharedPreferences;

    private BroadcastReceiver updateDataBroadcastReceiver;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDefaultFiles();
        initDate();
        createNotificationChannel();
        createNotification();

        updateDataBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Word word = intent.getParcelableExtra(getString(R.string.changingWord));

                if(intent.getBooleanExtra(getString(R.string.addNewCategory), false)) {
                    Log.d("BOB", "BIB");
                    categoryNames.add(word.getWordCategory());
                }

                wordArrayList.set((int) word.getIndex(), word);

                sendBroadcast(new Intent(MainActivity.BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION));
            }
        };

        archivedWordsSharedPreferences = getSharedPreferences(getString(R.string.archivedWordsSharedPreferences), MODE_PRIVATE);
        wordsCheckSharedPreferences = getSharedPreferences(getPackageName() + ".wordsCheckFlag", MODE_PRIVATE);

        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager());

        wordsUserCheckFragment = (WordsUserCheckFragment) fragmentViewPagerAdapter.getItem(0);
        addNewWordFragment = (AddNewWordFragment) fragmentViewPagerAdapter.getItem(1);
        wordsArchiveFragment = (WordsArchiveFragment) fragmentViewPagerAdapter.getItem(2);

        viewPager = findViewById(R.id.mainViewPagerId);
        dotsIndicator = findViewById(R.id.dots_indicator);
        spinKitView = findViewById(R.id.spinKit);
        spinKitView.setIndeterminateDrawable(new DoubleBounce());

        loadingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(MAIN_ACTIVITY_TAG, "HandleMessage" );
                switch(msg.what) {
                    case WORDS_LOAD_END:
                        Log.d(MAIN_ACTIVITY_TAG, "All words has been loaded successfully");

                        break;
                    case CATEGORIES_LOAD_END:
                        Log.d(MAIN_ACTIVITY_TAG, "Categories has been loaded successfully");

                        Animation animation = new AlphaAnimation(1f, 0f);
                        animation.setDuration(300);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                spinKitView.setVisibility(View.INVISIBLE);
                                viewPager.setAdapter(fragmentViewPagerAdapter);
                                dotsIndicator.setViewPager(viewPager);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) { }
                        });

                        spinKitView.startAnimation(animation);

                        break;
                    case WORDS_ANALYNG_WND:
                        Log.d(MAIN_ACTIVITY_TAG, "Words has been analized successfully");
                        callCheck();

                        break;
                    case CHECKING_WORDS_LOAD_END:
                        Log.d(MAIN_ACTIVITY_TAG, "Words has been sorted");
                        callWordsCheck();

                        break;
                }

            }
        };

        Log.d(MAIN_ACTIVITY_TAG, "OnCreate");
        updateData(loadingHandler);

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

                            String category = splitedLink[splitedLink.length - 2];
                            String sharingUserUid = splitedLink[splitedLink.length - 1];

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


        //here we check if out update broadcast receiver is registered
        try {
            unregisterReceiver(updateDataBroadcastReceiver);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }


        callCheck();
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onPause() {
        super.onPause();

        registerReceiver(updateDataBroadcastReceiver, new IntentFilter(MainActivity.BROADCAST_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void callCheck() {
        if(wordsCheckSharedPreferences.getInt(getPackageName() + ".wordsCheckFlag", -1) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            new Thread(new LoadWordsToCheckingThread(loadingHandler)).start();

            wordsCheckSharedPreferences.edit().putInt(getPackageName() + ".wordsCheckFlag", Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
        }



    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "LettReminderChannel";
            String description = "Notification channel for Lett Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(NotificationBroadcast.notificationId, name, importance);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(MainActivity.this, NotificationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(ALARM_SERVICE);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
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
        wordCheckIntent.putExtra(getString(R.string.word_check_flag), 1);

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

    public ArrayList<String> getCategoryNamesList() {
        return categoryNames;
    }

    public ArrayList<Word> getWordArrayList() {
        return wordArrayList;
    }

    public ArrayList<Word> getArchivedWordsArrayList() {
        return archivedWordsArrayList;
    }

    public void setArchivedWordsArrayList(ArrayList<Word> archivedWordsArrayList) {
        this.archivedWordsArrayList = archivedWordsArrayList;
    }

    public void updateData(Handler handler) {
        new Thread(new InitDataThread(handler)).start();
    }

    private void callErrorToast() {
        Toast.makeText(this, "Произошла неизвестная ошибка", Toast.LENGTH_LONG).show();
    }

    private class InitDataThread implements Runnable {

        Handler handler;
        public InitDataThread(Handler handler) {
            this.handler = handler;
            wordArrayList = new ArrayList<>();
            categoryNames = new ArrayList<>();
        }

        @Override
        public void run() {

            //Load words
            reference.child("words").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                        Word word = new Word(wordsCounter);
                        word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                        word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                        word.setWordCategory(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue()).toString());
                        word.setDate((long) Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.dateKey).getValue()));
                        word.setLevel((long) Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.levelDatabaseKey)).getValue());
                        wordArrayList.add(word);
                    }

                    handler.sendEmptyMessage(MainActivity.WORDS_LOAD_END);

                    categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(int categoryReferenceChildrenCounter = 0; categoryReferenceChildrenCounter < dataSnapshot.getChildrenCount(); categoryReferenceChildrenCounter++) {
                                categoryNames.add(String.valueOf(dataSnapshot.child(String.valueOf(categoryReferenceChildrenCounter)).getValue()));
                            }

                            handler.sendEmptyMessage(CATEGORIES_LOAD_END);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                    //Filter words
                    for(Word word : wordArrayList) {
                        //TODO: Add logic to filtrate words, which have been learnt and have been moved to archive
                        //But now I have made archived page as a all words page
                        archivedWordsArrayList.add(word);
                    }

                    handler.sendEmptyMessage(MainActivity.WORDS_ANALYNG_WND);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            //Load categories
            //

        }
    }

    private class StartAddingCategoryFromLink implements Runnable {
        String category;
        String sharingUserUid;
        List<Word> sharingWordList = new ArrayList<>();
        boolean isCategoryReal = false;

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
                    int newWordsCounter = 0;

                    for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                        if(Objects.equals(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue(), category)) {
                            Log.d("HUY", "bib");
                            Word newWord = new Word(dataSnapshot.getChildrenCount() + newWordsCounter);
                            newWord.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                            newWord.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                            newWord.setWordCategory(category);

                            newWordsCounter += 1;
                            sharingWordList.add(newWord);
                        }
                    }

                    for(int wordsCounter = 0; wordsCounter < sharingWordList.size(); wordsCounter++) {
                        Word word = sharingWordList.get(wordsCounter);
                        Log.d("BIBKA", String.valueOf(word.getIndex()));
                        word.sentWordToService();
                    }

                    FirebaseDatabase.getInstance().getReference().child("users").child(sharingUserUid).child("categories").addListenerForSingleValueEvent(new ValueEventListener() {


                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(int categoryCounter = 0; categoryCounter < dataSnapshot.getChildrenCount(); categoryCounter++) {
                                if(Objects.requireNonNull(dataSnapshot.child(String.valueOf(categoryCounter)).getValue()).toString().equals(category)) {
                                    isCategoryReal = true;
                                    break;
                                }
                            }

                            if(isCategoryReal) {
                                Toast.makeText(getApplicationContext(), getString(R.string.isCategoryTheSame), Toast.LENGTH_LONG).show();
                            }
                            else {
                                FirebaseDatabase.getInstance().getReference().child("users").child(sharingUserUid).child("categories").child(String.valueOf(dataSnapshot.getChildrenCount())).setValue(category);
                                Toast.makeText(getApplicationContext(), getString(R.string.newWords), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    private class FilterWordsThread implements Runnable {
        Handler handler;

        FilterWordsThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            for(Word word : wordArrayList) {
                //TODO: Add logic to filtrate words, which have been learnt and have been moved to archive
                //But now I have made archived page as a all words page
                archivedWordsArrayList.add(word);
            }

            handler.sendEmptyMessage(MainActivity.WORDS_ANALYNG_WND);
        }
    }

    private class LoadWordsToCheckingThread implements Runnable {

        Handler handler;
        ArrayList<Word> checkingWordsList = new ArrayList<>();

        LoadWordsToCheckingThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            //TODO: This thread should choose words from all words and call check
            for(Word word : wordArrayList) {
                checkingWordsList.add(word);
            }

            handler.sendEmptyMessage(CHECKING_WORDS_LOAD_END);
            saveData();
        }

        synchronized void saveData() {
            SharedPreferences.Editor editor = archivedWordsSharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(archivedWordsArrayList);
            editor.putString(getString(R.string.service_saved_indexes_key), json);
            editor.apply();
        }
    }
}
