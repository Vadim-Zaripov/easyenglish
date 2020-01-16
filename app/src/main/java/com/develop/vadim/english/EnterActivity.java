package com.develop.vadim.english;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EnterActivity extends AppCompatActivity {

    SharedPreferences prefs = null;
    String TAG = "myLogs";
    public FirebaseUser user;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);
        Log.d(TAG, "started");
        prefs = getSharedPreferences("com.example.vadim.maintimetracker", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            Log.d(TAG, "firstrun");
            startActivity(new Intent(this, TutorialActivity.class));
            prefs.edit().putBoolean("firstrun", false).apply();
        }else {
            Log.d(TAG, "not firstrun");
            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            if (user == null || !user.isEmailVerified())
                startActivity(new Intent(this, AuthenticationActivity.class));
            else
                startActivity(new Intent(this, MainActivity.class));
        }
    }
}
