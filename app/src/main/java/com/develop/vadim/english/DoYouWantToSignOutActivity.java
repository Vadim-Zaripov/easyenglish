package com.develop.vadim.english;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class DoYouWantToSignOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_you_want_to_sign_out);
    }

    public void SignOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(DoYouWantToSignOutActivity.this, AuthenticationActivity.class));
    }
}
