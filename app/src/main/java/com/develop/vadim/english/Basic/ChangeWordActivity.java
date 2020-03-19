package com.develop.vadim.english.Basic;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.develop.vadim.english.R;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class ChangeWordActivity extends AppCompatActivity {

    private EditText editTextRussian;
    private EditText editTextEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        editTextRussian = (EditText)findViewById(R.id.editTextRussian);
        editTextEnglish = (EditText)findViewById(R.id.editTextEnglish);

        editTextRussian.setText(OldMainActivity.rus);
        editTextEnglish.setText(OldMainActivity.eng);

    }

    public void Apply(View view) {
        String english = editTextEnglish.getText().toString();
        String russian = editTextRussian.getText().toString();
        if(!Objects.equals(russian, "") && !Objects.equals(english, "")){
            DatabaseReference ref = OldMainActivity.myRef.child("words").child(OldMainActivity.NEXT_DATE).child(OldMainActivity.ind);
            ref.child("Russian").setValue(russian);
            ref.child("English").setValue(english);
            ref.child("category").setValue("every_day");
            super.onBackPressed();
        }
    }

    public void Delete(View view){
        OldMainActivity.myRef.child("words").child(OldMainActivity.NEXT_DATE).child(OldMainActivity.ind).removeValue();
        super.onBackPressed();
    }
}
