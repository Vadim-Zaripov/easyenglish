package com.develop.vadim.english.Basic;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.develop.vadim.english.Broadcasts.NotificationBroadcastReceiver;
import com.develop.vadim.english.Fragments.AddNewWordFragment;
import com.develop.vadim.english.Fragments.FragmentViewPagerAdapter;
import com.develop.vadim.english.Fragments.WordsArchiveFragment;
import com.develop.vadim.english.Fragments.CategoriesFragment;
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
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MainActivity extends AppCompatActivity {

    private FragmentViewPagerAdapter fragmentViewPagerAdapter;
    private ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    public SpinKitView spinKitView;

    public static final String MAIN_ACTIVITY_TAG = "MainActivity";

    public static long index;

    public static String ind;

    public static String NEXT_DATE;

    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
    private DatabaseReference categoryReference = reference.child("categories");

    public final static String BROADCAST_ACTION = "ru.lett.xenous.action.BROADCAST";
    public final static String BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION = "ru.lett.xenous.action.UPDATE";

    public ArrayList<String> categoryNames = new ArrayList<>();
    public ArrayList<Word> wordArrayList = new ArrayList<>();
    public ArrayList<Word> wordsCheckWordsArrayList = new ArrayList<>();
    private ArrayList<Word> archivedWordsArrayList = new ArrayList<>();

    private boolean isCategoriesLoaded = false;

    private Handler loadingHandler;

    public static final int CATEGORIES_FRAGMENT_KEY = 2;
    public static final int ADD_NEW_WORD_FRAGMENT_KEY = 0;
    public static final int WORDS_ARCHIVE_FRAGMENT_KEY = 3;

    public static final int CATEGORIES_LOAD_END = 0;
    public static final int WORDS_LOAD_END = 1;
    public static final int WORDS_ANALYNG_WND = 3;
    public static final int CHECKING_WORDS_LOAD_END = 4;

    private BroadcastReceiver updateDataBroadcastReceiver;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.logOutImageView).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        });

        initDefaultFiles();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(MainActivity.this);
            startAlarm();
        }

        updateDataBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Word word = intent.getParcelableExtra(getString(R.string.changingWord));

                if(intent.getBooleanExtra(getString(R.string.removeWordKey), false)) {
                    if(intent.getBooleanExtra(getString(R.string.addNewCategory), false)) {
                        categoryNames.add(word.getWordCategory());
                    }

                    wordArrayList.set((int) word.getIndex(), word);
                }
                else {
                    wordArrayList.remove(word.getIndex());
                }

                callFragmentContentUpdate(CATEGORIES_FRAGMENT_KEY);
                callFragmentContentUpdate(WORDS_ARCHIVE_FRAGMENT_KEY);

                sendBroadcast(new Intent(MainActivity.BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION));
            }
        };


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
                            public void onAnimationStart(Animation animation) { }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                spinKitView.setVisibility(View.INVISIBLE);
                                viewPager.setAdapter(fragmentViewPagerAdapter);
                                viewPager.setOffscreenPageLimit(3);
                                viewPager.setCurrentItem(1);

                                dotsIndicator.setViewPager(viewPager);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) { }
                        });

                        spinKitView.startAnimation(animation);

                        break;
                    case WORDS_ANALYNG_WND:
                        Log.d(MAIN_ACTIVITY_TAG, "Words has been analized successfully");

                        Log.d("BIBKA", "BOB");

                        FirebaseDynamicLinks
                                .getInstance()
                                .getDynamicLink(getIntent())
                                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                                    @Override
                                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                        Uri deepLink;
                                        if(pendingDynamicLinkData != null) {
                                            deepLink = pendingDynamicLinkData.getLink();
                                            String category = deepLink.getQueryParameter("category");
                                            String userUid = deepLink.getQueryParameter("user");

                                            new Thread(new StartAddingCategoryFromLink(category, userUid)).start();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", "FAILED");
                                    }
                                });

                        break;
                    case CHECKING_WORDS_LOAD_END:
                        Log.d(MAIN_ACTIVITY_TAG, "Words has been sorted");

                        break;
                }

            }
        };

        fragmentViewPagerAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.mainViewPagerId);
        dotsIndicator = findViewById(R.id.dots_indicator);
        spinKitView = findViewById(R.id.spinKit);
        spinKitView.setIndeterminateDrawable(new DoubleBounce());


        Log.d(MAIN_ACTIVITY_TAG, "OnCreate");
        updateData(loadingHandler);

        registerReceiver(updateDataBroadcastReceiver, new IntentFilter(MainActivity.BROADCAST_ACTION));
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void callConfetti() {
        KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
        konfettiView.build()
                .addColors(Color.MAGENTA, Color.YELLOW, getResources().getColor(R.color.lightPurple))
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.CIRCLE, Shape.RECT)
                .addSizes(new Size(10, 5))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300,  1500L);
    }

    public void callFragmentContentUpdate(int position) {
        CategoriesFragment categoriesFragment = (CategoriesFragment) fragmentViewPagerAdapter.getItem(CATEGORIES_FRAGMENT_KEY);
        AddNewWordFragment addNewWordFragment = (AddNewWordFragment) fragmentViewPagerAdapter.getItem(ADD_NEW_WORD_FRAGMENT_KEY);
        WordsArchiveFragment wordsArchiveFragment = (WordsArchiveFragment) fragmentViewPagerAdapter.getItem(WORDS_ARCHIVE_FRAGMENT_KEY);

        switch(position) {
            case CATEGORIES_FRAGMENT_KEY:
                categoriesFragment.onDataChange();

                break;
            case ADD_NEW_WORD_FRAGMENT_KEY:
                addNewWordFragment.onDataChange();

                break;
            case WORDS_ARCHIVE_FRAGMENT_KEY:
                wordsArchiveFragment.onDataChange();

                break;
        }
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

    private void startAlarm() {
        AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;
        PendingIntent pendingIntent;

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        if( new Date().getTime() <= calendar.getTimeInMillis() ) {
            myIntent = new Intent(MainActivity.this, NotificationBroadcastReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this,0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(pendingIntent);
            manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void initDefaultFiles() {
        user = FirebaseAuth.getInstance().getCurrentUser();
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
                if(databaseError.getCode() == DatabaseError.DISCONNECTED ||
                        databaseError.getCode() == DatabaseError.NETWORK_ERROR) {
                    // ToDo: IOS DIALOG
                    new IOSDialog.Builder(getApplicationContext())
                            .message(getString(R.string.no_internet_error))
                            .build()
                            .show();
                }
            }
        });
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

    public void updateData(Handler handler) {
        new Thread(new InitDataThread(handler)).start();
    }

    private class InitDataThread implements Runnable {

        Handler handler;
        InitDataThread(Handler handler) {
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
                    final ArrayList<String> wordsCategories = new ArrayList<>();
                    for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                        Word word = new Word(wordsCounter);
                        word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                        word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                        word.setWordCategory(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue()).toString());
                        word.setLevel((long) Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.levelDatabaseKey)).getValue());
                        word.setDate( Long.parseLong(
                                Objects.requireNonNull(
                                        dataSnapshot
                                                .child(String.valueOf(wordsCounter))
                                                .child(Word.dateKey)
                                                .getValue()
                                ).toString())
                        );

                        if(!wordsCategories.contains(word.getWordCategory())) {
                            wordsCategories.add(word.getWordCategory());
                        }

                        wordArrayList.add(word);
                    }

                    handler.sendEmptyMessage(MainActivity.WORDS_LOAD_END);

                    categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(int categoryReferenceChildrenCounter = 0; categoryReferenceChildrenCounter < dataSnapshot.getChildrenCount(); categoryReferenceChildrenCounter++) {
                                String category = String.valueOf(dataSnapshot.child(String.valueOf(categoryReferenceChildrenCounter)).getValue());

                                if(wordsCategories.contains(category)) {
                                    categoryNames.add(String.valueOf(dataSnapshot.child(String.valueOf(categoryReferenceChildrenCounter)).getValue()));
                                }
                                else {
                                    categoryReference.child(String.valueOf(categoryReferenceChildrenCounter)).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).getValue());
                                    categoryReference.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).removeValue();
                                }
                            }

                            handler.sendEmptyMessage(CATEGORIES_LOAD_END);

                            for(Word word : wordArrayList) {
                                Date date = new Date();
                                long currentTime = date.getTime();

                                Log.w(MAIN_ACTIVITY_TAG, currentTime + " is greater than " + word.getDate() + ": " + (currentTime > word.getDate()));


                                if(word.getLevel() == Word.LEVEL_ARCHIVED) {
                                    archivedWordsArrayList.add(word);

                                    continue;
                                }

                                if(word.getLevel() == Word.LEVEL_ADDED) {
                                    continue;
                                }

                                if(currentTime > word.getDate()) {
                                    if(word.getLevel() == Word.LEVEL_DAY || word.getLevel() == Word.LEVEL_WEEK) {
                                        if(currentTime - word.getDate() > Word.CHECK_INTERVAL.get(Word.LEVEL_DAY) * 3) {
                                            word.setLevel(Word.LEVEL_DAY);
                                        }
                                    }

                                    wordsCheckWordsArrayList.add(word);
                                }
                            }

                            handler.sendEmptyMessage(MainActivity.WORDS_ANALYNG_WND);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                    //Filter words
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
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

                    //Init words from another user
                    for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                        boolean isCurrentUserContainsThisWord = false;
                        if(Objects.equals(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue(), category)) {
                            for(int currentUserWordsCounter = 0; currentUserWordsCounter < wordArrayList.size(); currentUserWordsCounter++) {
                                if(Objects.equals(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue(), wordArrayList.get(currentUserWordsCounter).getWordInRussian())) {
                                    isCurrentUserContainsThisWord = true;
                                }
                            }

                            if(!isCurrentUserContainsThisWord) {
                                Word newWord = new Word(wordArrayList.size() + newWordsCounter);
                                newWord.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                                newWord.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                                newWord.setWordCategory(category);
                                newWord.setLevel(Word.LEVEL_ADDED);

                                newWordsCounter += 1;
                                sharingWordList.add(newWord);
                            }
                        }
                    }

                    //Checking if our categories list contains sharing category
                    if(categoryNames.contains(category)) {
                        isCategoryReal = true;
                    }

                    String message = "Новых слов для добавления " +
                            sharingWordList.size() +
                            "." +
                            " Добавить?" +
                            "\n" +
                            "Слова будут добавлены в категорию " +
                            category;

                    // ToDo: IOS DIALOG
                    new IOSDialog.Builder(getApplicationContext())
                            .message(message)
                            .negativeButtonText(getString(R.string.no))
                            .negativeClickListener(new IOSDialog.Listener() {
                                @Override
                                public void onClick(IOSDialog iosDialog) {
                                    iosDialog.dismiss();
                                }
                            })
                            .positiveButtonText("Да")
                            .positiveClickListener(new IOSDialog.Listener() {
                                @Override
                                public void onClick(IOSDialog iosDialog) {
                                    for(int wordsCounter = 0; wordsCounter < sharingWordList.size(); wordsCounter++) {
                                        Word word = sharingWordList.get(wordsCounter);
                                        Log.d("BIBKA", String.valueOf(word.getIndex()));
                                        word.sentWordToService();
                                    }

                                    if(!isCategoryReal) {
                                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("categories").child(String.valueOf(categoryNames.size())).setValue(category);
                                        categoryNames.add(category);

                                        Toast.makeText(getApplicationContext(), getString(R.string.newWords), Toast.LENGTH_SHORT).show();
                                    }

                                    wordArrayList.addAll(sharingWordList);

                                    callFragmentContentUpdate(CATEGORIES_FRAGMENT_KEY);

                                    iosDialog.dismiss();
                                }
                            })
                            .build()
                            .show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

}
