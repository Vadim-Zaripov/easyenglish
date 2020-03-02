package com.develop.vadim.english.Basic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
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
import java.util.Objects;

public class OldMainActivity extends AppCompatActivity {

    public final String TAG = "myLogs";
    public EditText editText;
    public static DatabaseReference myRef;
    public FirebaseUser user;
    public int amount, i;
    public TextView text_, incorrect, change_word;
    public String NOW_DATE;
    public static String NEXT_DATE;
    public String WEEK_DATE, MONTH_DATE, THREE_MONTH_DATE, SIX_MONTH_DATE;
    public DateFormat format;
    public Date now;
    private Button btnCheck, btnNext;
    public boolean answered = false;

    public static String rus, eng, ind;

    TapTargetSequence tapTargetSequence;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_main_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu);

        SharedPreferences prefs = getSharedPreferences("com.example.vadim.maintimetracker", MODE_PRIVATE);

        if(prefs.getBoolean("firstrun_testing", true)){
            prefs.edit().putBoolean("firstrun_testing", false).apply();
            firstLaunch();
        }


        incorrect = (TextView)findViewById(R.id.textView);
        change_word = (TextView)findViewById(R.id.textView4);

        btnCheck = (Button)findViewById(R.id.button);
        btnNext = (Button)findViewById(R.id.button8);
        editText = (EditText)findViewById(R.id.editText);
        text_ = (TextView)findViewById(R.id.textView3);

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



        user = FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        myRef.child("words")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Date date;
                        amount = (int) dataSnapshot.child(NOW_DATE).getChildrenCount();
                        int count;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                date = format.parse(String.valueOf(snapshot.getKey()));
                                if (date.before(now)) {
                                    count = (int)( (now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
                                    Log.d(TAG, "HELLO");
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        myRef.child("words").child(NOW_DATE).child(String.valueOf(amount)).child("English").setValue(ds.child("English").getValue());
                                        myRef.child("words").child(NOW_DATE).child(String.valueOf(amount)).child("Russian").setValue(ds.child("Russian").getValue());
                                        Log.d(TAG, String.valueOf(count));
                                        String category = ds.child("category").getValue().toString();
                                        if(count >= 3 && (Objects.equals(category, "every_week") || Objects.equals(category, "every_month"))){
                                            myRef.child("words").child(NOW_DATE).child(String.valueOf(amount)).child("category").setValue("every_day");
                                        }else {
                                            myRef.child("words").child(NOW_DATE).child(String.valueOf(amount)).child("category").setValue(category);
                                        }
                                        amount++;
                                    }
                                    myRef.child("words").child(String.valueOf(snapshot.getKey())).removeValue();
                                }
                            } catch (ParseException e) {
                                Log.d(TAG, e.toString());
                            }
                        }
                        i = amount-1;
                        Log.d(TAG, "Amount: " + String.valueOf(amount));
                        if (amount > 0) {
                            setText(i);
                        } else {
                            btnCheck.setClickable(false);
                            btnNext.setClickable(false);
                            change_word.setClickable(false);
                            text_.setText(R.string.end_of_words);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void Answered(View view) {
        btnCheck.setClickable(false);
        answered = true;
        myRef.child("words")
                .child(NOW_DATE)
                .child(String.valueOf(i))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get total available quest
                        rus = dataSnapshot.child("Russian").getValue().toString();
                        eng = dataSnapshot.child("English").getValue().toString();
                        myRef.child("words").child(NOW_DATE).child(String.valueOf(i)).removeValue();
                        if(Objects.equals(editText.getText().toString().toLowerCase(), eng)){
                            incorrect.setText("Верно!");
                            incorrect.setVisibility(View.VISIBLE);
                            switch(dataSnapshot.child("category").getValue().toString()){
                                case "every_day":
                                    Step(NEXT_DATE, "every_day_russian", eng, rus);
                                    break;
                                case "every_day_russian":
                                    Step(WEEK_DATE, "every_week", eng, rus);
                                    break;
                                case "every_week":
                                    Step(MONTH_DATE, "every_month", rus, eng);
                                    break;
                                case "every_month":
                                    Step(THREE_MONTH_DATE, "every_three_month", rus, eng);
                                    break;
                                case "every_three_month":
                                    Step(SIX_MONTH_DATE, "every_six_month", rus, eng);
                                    break;
                                case "every_six_month":
                                    MoveToArchive(rus, eng);
                                    break;
                            }
                        }else{
                            change_word.setClickable(true);
                            change_word.setVisibility(View.VISIBLE);
                            incorrect.setText("Hеправильно, на самом деле: " + eng);
                            incorrect.setVisibility(View.VISIBLE);
                            if(Objects.equals(dataSnapshot.child("category").getValue().toString(), "every_day_russian")){
                                Step(NEXT_DATE, "every_day", eng, rus);
                            }else {
                                Step(NEXT_DATE, "every_day", rus, eng);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

    }

    public void NextWord(View view){
        if(!answered){
            myRef.child("words")
                    .child(NOW_DATE)
                    .child(String.valueOf(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // get total available quest
                            String russian = dataSnapshot.child("Russian").getValue().toString();
                            String english = dataSnapshot.child("English").getValue().toString();
                            Toast.makeText(OldMainActivity.this, english, Toast.LENGTH_SHORT).show();
                            if(Objects.equals(dataSnapshot.child("category").getValue().toString(), "every_day_russian")){
                                String temp = russian;
                                russian = english;
                                english = temp;
                            }
                            myRef.child("words").child(NOW_DATE).child(String.valueOf(i)).removeValue();
                            Step(NEXT_DATE, "every_day", russian, english);
                            NextWordCont();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }else {
            NextWordCont();
        }
    }

    public void NextWordCont(){
        i --;
        editText.setText("");
        change_word.setClickable(false);
        change_word.setVisibility(View.INVISIBLE);
        incorrect.setVisibility(View.INVISIBLE);
        if(i >= 0){
            setText(i);
            answered = false;
            btnCheck.setClickable(true);
        }else{
            btnCheck.setClickable(false);
            btnNext.setClickable(false);
            text_.setText(R.string.end_of_words);
        }
    }

    public void MoveToArchive(final String russian, final String english){
        myRef.child("archive")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get total available quest
                        int a = (int) dataSnapshot.getChildrenCount();
                        myRef.child("archive").child(String.valueOf(a)).setValue(russian + " - " + english);
                        myRef.child("words").child(NOW_DATE).child(String.valueOf(i)).removeValue();
                        i --;
                        if(i >= 0){
                            setText(i);
                        }else{
                            text_.setText(R.string.end_of_words);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void Step(final String d, final String stage, final String russian, final String english){
        myRef.child("words")
                .child(d)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get total available quest
                        ind = String.valueOf((int) dataSnapshot.getChildrenCount());
                        myRef.child("words").child(d).child(ind).child("Russian").setValue(russian);
                        myRef.child("words").child(d).child(ind).child("English").setValue(english);
                        myRef.child("words").child(d).child(ind).child("category").setValue(stage);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setText(int k) {
        myRef.child("words")
                .child(NOW_DATE)
                .child(String.valueOf(k))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get total available quest
                        text_.setText(dataSnapshot.child("Russian").getValue().toString());

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    public void changeWord(View view){
        startActivity(new Intent(OldMainActivity.this, ChangeWordActivity.class));
    }

    public void firstLaunch(){
        tapTargetSequence = new TapTargetSequence(this);
        tapTargetSequence.targets(
                TapTarget.forToolbarMenuItem(toolbar, R.id.navigation_new_word, "New word", "Tap to create new word").id(1),
                TapTarget.forToolbarMenuItem(toolbar, R.id.navigation_archive, "Archive", "Find your learned words here").id(2));
        tapTargetSequence.listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                //Yay
            }
            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                //Action
            }
            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                //Boo
            }
        });
        tapTargetSequence.start();
    }

    @Override
    public void onBackPressed(){}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.navigation_new_word:
                startActivity(new Intent(OldMainActivity.this, NewWordActivity.class));
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(OldMainActivity.this, DoYouWantToSignOutActivity.class));
                break;
            case R.id.navigation_archive:
                startActivity(new Intent(OldMainActivity.this, ArchiveActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
