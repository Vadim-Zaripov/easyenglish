package com.develop.vadim.english.Basic;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;

public class Word {
    private String wordInEnglish;
    private String wordInRussian;
    private int level = EVERY_DAY;
    private long date;
    private String ind;
    private String wordCategory = "no category";

    private long index;

    public final static String WORD_TAG = "WordClass";

    public static final String russianDatabaseKey = "Russian";
    public static final String englishDatabaseKey = "English";
    public static final String categoryDatabaseKey = "category";
    public static final String dateKey = "date";
    public static final String levelDatabaseKey = "level";

    public static final byte
            EVERY_DAY = 0,
            EVERY_TWO_DAYS = 1,
            EVERY_THREE_DAYS = 2,
            EVERY_FOUR_DAYS = 3,
            EVERY_FIVE_DAYS = 4,
            EVERY_SIX_DAYS = 5,
            EVERY_WEEK = 6,
            EVERY_TWO_WEEKS = 7,
            EVERY_MONTH = 8,
            EVERY_TWO_MONTHS = 9,
            EVERY_SIX_MONTHS = 10,
            EVERY_YEAR = 11;

    public Word(long ind) {
        this.index = ind;
        this.ind = String.valueOf(ind);
        Log.d(WORD_TAG, "Word : New word has been created");
    }

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

    public void removeWordFromService() {
        Log.d(WORD_TAG, "Word : starting deleting word from database");

        MainActivity.reference.child("words").child(ind).removeValue();

    }

    public void changeWordInService() {
        Log.d(WORD_TAG, "Word : starting changing word information in database");

        DatabaseReference databaseReference = MainActivity.reference.child("words").child(ind);
        databaseReference.child(russianDatabaseKey).setValue(wordInRussian);
        databaseReference.child(englishDatabaseKey).setValue(wordInEnglish);
        databaseReference.child(categoryDatabaseKey).setValue(EVERY_DAY);
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

    public void downgradeLevel() {
        if(level != EVERY_DAY) {
            level -= 1;
        }
    }
}
