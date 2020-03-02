package com.develop.vadim.english.Basic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.develop.vadim.english.Basic.AuthenticationActivity;
import com.develop.vadim.english.R;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
    }

    public void Continue(View v){
        startActivity(new Intent(this, AuthenticationActivity.class));
    }
}
