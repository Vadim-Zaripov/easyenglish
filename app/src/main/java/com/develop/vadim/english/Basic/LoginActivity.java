package com.develop.vadim.english.Basic;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.develop.vadim.english.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {


    private FirebaseAuth auth;

    private final String TAG = "Authentication";
    private boolean state;

    public FirebaseUser user;

    private EditText email;
    private EditText password;
    private TextView registrationTextView;
    private ImageView loginImageView;
    private GoogleSignInButton googleSignInButton;
    private SpinKitView spinKitView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences wordsCheckSharedPreferences;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        wordsCheckSharedPreferences = getSharedPreferences(getPackageName() + ".wordsCheckFlag", MODE_PRIVATE);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), getString(R.string.undefinedError), Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        sharedPreferences = getSharedPreferences(getPackageName() + ".firstrun", MODE_PRIVATE);
        email = findViewById(R.id.emailEditView);
        password = findViewById(R.id.passwordEditText);
        registrationTextView = findViewById(R.id.registrationTextView);
        loginImageView = findViewById(R.id.loginImageView);
        googleSignInButton = findViewById(R.id.signInWithGoogleButton);
        spinKitView = findViewById(R.id.spinKit);

        auth = FirebaseAuth.getInstance();
        Log.d(TAG, "--started RegistrationActivity--");

        state = true;

        registrationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);
                startActivity(new Intent(v.getContext(), RegisterActivity.class), activityOptions.toBundle());
            }
        });

        loginImageView.setOnClickListener(loginClickListener);

        googleSignInButton.setOnClickListener(view -> signInWithGoogle());

        Log.d(TAG, "--started RegistrationActivity--");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sharedPreferences.getBoolean(getPackageName() + ".firstrun", false)) {
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this);
            startActivity(new Intent(this, MainActivity.class), activityOptions.toBundle());
        }
        else {
            wordsCheckSharedPreferences.edit().putInt(getPackageName()  + ".wordsCheckFlag", Calendar.getInstance().get(Calendar.DAY_OF_YEAR)).apply();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        spinKitView.getRootView().setClickable(true);
        spinKitView.setVisibility(View.INVISIBLE);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch(ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            spinKitView.getRootView().setClickable(false);
            spinKitView.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    spinKitView.setVisibility(View.INVISIBLE);
                    spinKitView.getRootView().setClickable(true);

                    if(task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        if(!user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Email не подтвержден!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            doAfter(true);
                        }
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                        doAfter(false);
                    }
                }
            });
        }
    };

    //####################################################

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        spinKitView.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        spinKitView.setVisibility(View.INVISIBLE)
                        ;
                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = auth.getCurrentUser();
                            doAfter(true);
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Ошибка решистрации", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    //####################################################
    public void doAfter(boolean res){
        if(res) {
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

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, 1);
    }


}
