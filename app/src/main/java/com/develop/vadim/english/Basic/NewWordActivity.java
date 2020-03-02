package com.develop.vadim.english.Basic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.develop.vadim.english.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class NewWordActivity extends AppCompatActivity {

    private EditText editTextRussian;
    private EditText editTextEnglish;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word);


        editTextRussian = (EditText)findViewById(R.id.editTextRussian);
        editTextEnglish = (EditText)findViewById(R.id.editTextEnglish);

        OldMainActivity.myRef.child("words")
                .child(OldMainActivity.NEXT_DATE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        index = (int)dataSnapshot.getChildrenCount();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void Apply(View view) {
        String english = editTextEnglish.getText().toString();
        String russian = editTextRussian.getText().toString();
        if(!Objects.equals(russian, "") && !Objects.equals(english, "")){
            DatabaseReference ref = OldMainActivity.myRef.child("words").child(OldMainActivity.NEXT_DATE).child(String.valueOf(index));
            ref.child("Russian").setValue(russian);
            ref.child("English").setValue(english);
            ref.child("category").setValue("every_day");
            super.onBackPressed();
        }
    }
}
