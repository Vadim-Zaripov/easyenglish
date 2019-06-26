package com.develop.vadim.english;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference myRef;

    private final String TAG = "myLogs";
    private boolean state;
     //####################################################

    public FirebaseUser user;

    //####################################################

    private EditText _email;
    private EditText _password;
    private EditText _confirm_password;

    public void signIn(String email , String password) {
        Log.d(TAG, "signing " + email + " " + password);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    user = mAuth.getCurrentUser();
                    if(!user.isEmailVerified()){
                        Toast.makeText(AuthenticationActivity.this, "Email is not verified. Check verification email", Toast.LENGTH_SHORT).show();
                    }else{
                        doAfter(true);
                    }
                }else {
                    Toast.makeText(AuthenticationActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    TextView textView = (TextView) findViewById(R.id.textView8);
                    textView.setVisibility(View.VISIBLE);
                    textView.setClickable(true);
                    doAfter(false);
                }

            }
        });
    }

    public void register (String email , String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(AuthenticationActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(AuthenticationActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                onClickSignIn(_email);
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

    //####################################################

    public void onClickSignIn(View v){
        setContentView(R.layout.activity_sign_in);
        state = false;

        _email = (EditText) findViewById(R.id.edTextEmail);
        _password = (EditText) findViewById(R.id.edTextPassword);
    }

    public void onClickRegister(View v){
        setContentView(R.layout.activity_register);
        state = false;

        _email = (EditText) findViewById(R.id.edTextEmail1);
        _password = (EditText) findViewById(R.id.edTextPassword1);
        _confirm_password = (EditText) findViewById(R.id.edTextPasswordConf);
    }

    public void goSignIn(View v){
        //v.setClickable(false);
        signIn(_email.getText().toString(), _password.getText().toString());
    }

    public void goRegister(View v){
        if(Objects.equals(_confirm_password.getText().toString(), _password.getText().toString())) {
            v.setClickable(false);
            register(_email.getText().toString(), _password.getText().toString());
        }else
            Toast.makeText(AuthenticationActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
    }

    //####################################################
    public void doAfter(boolean res){
        if(res) {
            //startActivity(new Intent(this, Main2Activity.class));
            Log.d(TAG, "--finished RegistrationActivity--");
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "--started RegistrationActivity--");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }

            }
        };

        myRef = FirebaseDatabase.getInstance().getReference();
        state = true;

        Log.d(TAG, "--started RegistrationActivity--");
    }

    public void onBackPressed(){
        if(state)
            super.onBackPressed();
        else{
            setContentView(R.layout.activity_main_register);
            state = true;
        }
    }

    public void Reset(final View view) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(_email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            TextView v = (TextView) view;
                            v.setText("Письмо для восстановления пароля отправлено на ваш Email");
                        }else{
                            Toast.makeText(AuthenticationActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
