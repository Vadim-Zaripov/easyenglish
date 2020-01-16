package com.develop.vadim.english;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

public class Word {
    private String wordInEnglish;
    private String wordInRussian;
    private long index;

    public final static String WORD_TAG = "WordClass";

    private final String russianDatabaseKey = "Russian";
    private final String englishDatabaseKey = "English";
    private final String categoryDtabaseKey = "category";

    public Word(long index) {
        Log.d(WORD_TAG, "Word : New word has been created");
        this.index = index;
    }

    public void sentWordToService() {
        Log.d(WORD_TAG, "Word : starting writing data to database");

        DatabaseReference databaseReference = MainActivity.myRef.child("words").child(MainActivity.NEXT_DATE).child(getIndex());
        databaseReference.child(russianDatabaseKey).setValue(russianDatabaseKey);
        databaseReference.child(englishDatabaseKey).setValue(russianDatabaseKey);
        databaseReference.child(categoryDtabaseKey).setValue("every_day");
    }

    public void removeWordFromService() {
        Log.d(WORD_TAG, "Word : starting deleting word from database");

        MainActivity.myRef.child("words").child(MainActivity.NEXT_DATE).child(MainActivity.ind).removeValue();

    }

    public void changeWordInService() {
        Log.d(WORD_TAG, "Word : starting changing word information in database");

        DatabaseReference databaseReference = MainActivity.myRef.child("words").child(MainActivity.NEXT_DATE).child(MainActivity.ind);
        databaseReference.child(russianDatabaseKey).setValue(russianDatabaseKey);
        databaseReference.child(englishDatabaseKey).setValue(russianDatabaseKey);
        databaseReference.child(categoryDtabaseKey).setValue("every_day");
    }

    public String getWordInEnglish() {
        return wordInEnglish;
    }

    public void setWordInEnglish(String wordInEnglish) {
        this.wordInEnglish = wordInEnglish;
    }

    public String getWordInRussian() {
        return wordInRussian;
    }

    public void setWordInRussian(String wordInRussian) {
        this.wordInRussian = wordInRussian;
    }

    private String getIndex() {
        return String.valueOf(index);
    }

}
