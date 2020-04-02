package com.develop.vadim.english.Basic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    private DatabaseReference reference;

    private final String TAG = "Authentication";
    private boolean state;

    public FirebaseUser user;

    private EditText email;
    private EditText password;
    private EditText confrimPassword;
    private TextView registrationTextView;
    private TextView loginTextView;
    private ImageView registerImageView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences wordsCheckSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        wordsCheckSharedPreferences = getSharedPreferences(getPackageName() + ".wordsCheckFlag", MODE_PRIVATE);

        sharedPreferences = getSharedPreferences(getPackageName() + ".firstrun", MODE_PRIVATE);
        email = findViewById(R.id.emailEditView);
        password = findViewById(R.id.passwordEditText);
        confrimPassword = findViewById(R.id.confrimEditText);
        registrationTextView = findViewById(R.id.registrationTextView);
        loginTextView = findViewById(R.id.loginTextView);
        registerImageView = findViewById(R.id.registerImageView);

        auth = FirebaseAuth.getInstance();
        Log.d(TAG, "--started RegistrationActivity--");

        if(sharedPreferences.getBoolean(getPackageName() + ".firstrun", false)) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else {
            wordsCheckSharedPreferences.edit().putInt(getPackageName()  + ".wordsCheckFlag", Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
        }

        reference = FirebaseDatabase.getInstance().getReference();
        state = true;

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confrimPassword.setVisibility(View.INVISIBLE);
                registerImageView.setOnClickListener(loginClickListener);
            }
        });

        registrationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confrimPassword.setVisibility(View.VISIBLE);
                confrimPassword.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.appear));
                registerImageView.setOnClickListener(registerClickListener);
            }
        });

        Log.d(TAG, "--started RegistrationActivity--");
    }

    private View.OnClickListener registerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(AuthenticationActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(AuthenticationActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();

                                    state = false;
                                }else{
                                    Toast.makeText(AuthenticationActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(AuthenticationActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        doAfter(false);
                    }
                }
            });
        }
    };

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        if(!user.isEmailVerified()) {
                            Toast.makeText(AuthenticationActivity.this, "Email не подтвержден!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            doAfter(true);
                        }
                    }
                    else {
                        Toast.makeText(AuthenticationActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                        doAfter(false);
                    }
                }
            });
        }
    };

    //####################################################

    //####################################################
    public void doAfter(boolean res){
        if(res) {
            //startActivity(new Intent(this, Main2Activity.class));
            Log.d(TAG, "--finished RegistrationActivity--");
            startActivity(new Intent(this, MainActivity.class));
            controlFirstRun();
        }
    }

    public void onBackPressed(){
        if(state)
            super.onBackPressed();
        else{
            setContentView(R.layout.activity_main_register);
            state = true;
        }
    }

    private void controlFirstRun() {
        sharedPreferences.edit().putBoolean(getPackageName() + ".firstrun", true).apply();
    }
}
