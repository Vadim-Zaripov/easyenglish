package com.develop.vadim.english;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangeWordActivity extends AppCompatActivity {

    private EditText editTextRussian;
    private EditText editTextEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);


        editTextRussian = (EditText) findViewById(R.id.editTextRussian);
        editTextEnglish = (EditText) findViewById(R.id.editTextEnglish);

        editTextRussian.setText(MainActivity.rus);
        editTextEnglish.setText(MainActivity.eng);

    }

    public void Apply(View view) {
        String english = editTextEnglish.getText().toString();
        String russian = editTextRussian.getText().toString();
        if(!Objects.equals(russian, "") && !Objects.equals(english, "")){
            DatabaseReference ref = MainActivity.myRef.child("words").child(MainActivity.NEXT_DATE).child(MainActivity.ind);
            ref.child("Russian").setValue(russian);
            ref.child("English").setValue(english);
            ref.child("category").setValue("every_day");
            super.onBackPressed();
        }
    }

    public void Delete(View view){
        MainActivity.myRef.child("words").child(MainActivity.NEXT_DATE).child(MainActivity.ind).removeValue();
        super.onBackPressed();
    }
}
