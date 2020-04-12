package com.develop.vadim.english.Basic;

import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Word implements Parcelable {

    public static final int LEVEL_ADDED = -2;
    public static final int LEVEL_ARCHIVED = -1;
    static final int LEVEL_DAY = 0;
    static final int LEVEL_WEEK = 1;
    static final int LEVEL_MONTH = 2;
    static final int LEVEL_QUARTER = 3;
    static final int LEVEL_HALF = 4;
    static final int LEVEL_YEAR = 5;
    static final HashMap<Integer, Long> CHECK_INTERVAL = new HashMap<Integer, Long>() {{
        put(LEVEL_DAY, 86400000L);
        put(LEVEL_WEEK, 604800000L);
        put(LEVEL_MONTH, 2592000000L);
        put(LEVEL_QUARTER, 7776000000L);
        put(LEVEL_HALF, 15552000000L);
        put(LEVEL_YEAR, 31104000000L);
    }};


    private static final int REMOVING_COMPLETE_KEY = 567;

    private String wordInEnglish;
    private String wordInRussian;
    private long level = LEVEL_DAY;
    private long date;
    private String ind;
    private String wordCategory = "default";

    private long index;

    public final static String WORD_TAG = "WordClass";

    public static final String russianDatabaseKey = "Russian";
    public static final String englishDatabaseKey = "English";
    public static final String categoryDatabaseKey = "category";
    public static final String dateKey = "date";
    public static final String levelDatabaseKey = "level";

    public Word(long ind) {
        this.index = ind;
        this.ind = String.valueOf(ind);
        Log.d(WORD_TAG, "Word : New word has been created");
    }

    protected Word(Parcel in) {
        wordInEnglish = in.readString();
        wordInRussian = in.readString();
        level = in.readLong();
        date = in.readLong();
        ind = in.readString();
        wordCategory = in.readString();
        index = in.readLong();
    }

    public String getInd() {
        return ind;
    }

    public static final Creator<Word> CREATOR = new Creator<Word>() {
        @Override
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public void sentWordToService() {
        Log.d(WORD_TAG, "Word : starting writing data to database");
        Date date = new Date();

        DatabaseReference databaseReference = MainActivity.reference.child("words").child(ind);
        databaseReference.child(russianDatabaseKey).setValue(wordInRussian);
        databaseReference.child(englishDatabaseKey).setValue(wordInEnglish);
        databaseReference.child(categoryDatabaseKey).setValue(wordCategory);
        databaseReference.child(levelDatabaseKey).setValue(level);
        databaseReference.child(dateKey).setValue(date.getTime());
    }

    public String getWordCategory() {
        return wordCategory;
    }

    public void setWordCategory(String wordCategory) {
        this.wordCategory = wordCategory;
    }

    public static String getCategoryDatabaseKey() {
        return categoryDatabaseKey;
    }

    public static String getDateKey() {
        return dateKey;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public void removeWordFromService(final Handler handler) {
        Log.d(WORD_TAG, "Word : starting deleting word from database");

        MainActivity.reference.child("words").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                DatabaseReference reference = MainActivity.reference.child("words").child(ind);
                reference.child(Word.englishDatabaseKey).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).child(Word.englishDatabaseKey).getValue());
                reference.child(Word.russianDatabaseKey).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).child(Word.russianDatabaseKey).getValue());
                reference.child(Word.dateKey).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).child(Word.dateKey).getValue());
                reference.child(Word.categoryDatabaseKey).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).child(Word.categoryDatabaseKey).getValue());
                reference.child(Word.levelDatabaseKey).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).child(Word.levelDatabaseKey).getValue());

                MainActivity.reference.child("words").child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "bib");
                        handler.sendEmptyMessage(Word.REMOVING_COMPLETE_KEY);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void changeWordInService(Word word) {
        Log.d(WORD_TAG, "Word : starting changing word information in database");

        DatabaseReference databaseReference = MainActivity.reference.child("words").child(ind);
        databaseReference.child(russianDatabaseKey).setValue(word.getWordInRussian());
        databaseReference.child(categoryDatabaseKey).setValue(0);
    }

    public String getWordInEnglish() {
        return wordInEnglish;
    }

    public void setWordInEnglish(String wordInEnglish) {
        this.wordInEnglish = wordInEnglish;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getWordInRussian() {
        return wordInRussian;
    }

    public void setWordInRussian(String wordInRussian) {
        this.wordInRussian = wordInRussian;
    }

    public static Word getWordFromService(long index) {
        Word newWord = new Word(index);
        newWord.setWordInEnglish(MainActivity.reference.child("words").child(MainActivity.NEXT_DATE).child(String.valueOf(index)).child(englishDatabaseKey).getKey());
        newWord.setWordInRussian(MainActivity.reference.child("words").child(MainActivity.NEXT_DATE).child(String.valueOf(index)).child(russianDatabaseKey).getKey());

        return newWord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getIndex() {
        return index;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wordInEnglish);
        dest.writeString(wordInRussian);
        dest.writeLong(level);
        dest.writeLong(date);
        dest.writeString(ind);
        dest.writeString(wordCategory);
        dest.writeLong(index);
    }
}
