package com.develop.vadim.english.Basic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.develop.vadim.english.R;
import com.develop.vadim.english.utils.Utils;
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

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private final String TAG = "Authentication";
    private boolean state;

    public FirebaseUser user;

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private TextView loginTextView;
    private ImageView registerImageView;
    private ImageButton googleSignInButton;
    private SpinKitView spinKitView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences wordsCheckSharedPreferences;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        spinKitView = findViewById(R.id.spinKit);

        Utils.makeStatusBarTransparent(this);

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
        confirmPassword = findViewById(R.id.confrimEditText);
        loginTextView = findViewById(R.id.loginTextView);
        registerImageView = findViewById(R.id.registerImageView);
        googleSignInButton = findViewById(R.id.signInWithGoogleButton);

        auth = FirebaseAuth.getInstance();
        Log.d(TAG, "--started RegistrationActivity--");

        state = true;

        loginTextView.setOnClickListener(view -> {

            onBackPressed();
        });
        loginTextView.setOnTouchListener(Utils.loginTouchListener);

        registerImageView.setOnClickListener(registerClickListener);
        registerImageView.setOnTouchListener(Utils.loginTouchListener);
        googleSignInButton.setOnClickListener(view -> signInWithGoogle());

        Log.d(TAG, "--started RegistrationActivity--");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == 1) {
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

    private View.OnClickListener registerClickListener = v -> {
        spinKitView.setVisibility(View.VISIBLE);
        spinKitView.startAnimation(new AlphaAnimation(0f, 1f));

        if(!password.getText().toString().trim().equals("") && !confirmPassword.getText().toString().trim().equals("") &&   !email.getText().toString().trim().equals("")) {
            if(password.getText().toString().equals(confirmPassword.getText().toString())) {
                v.setClickable(false);

                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(task ->
                                Objects.requireNonNull(auth.getCurrentUser())
                                        .sendEmailVerification()
                                        .addOnSuccessListener(view -> {
                                            v.setClickable(true);
                                            spinKitView.setVisibility(View.INVISIBLE);

                                            Toast.makeText(RegisterActivity.this, "Письмо поттверждения отправлено на ваш E-mail", Toast.LENGTH_SHORT).show();
                                            state = false;
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {

                                            v.setClickable(true);
                                            Toast.makeText(RegisterActivity.this, "Произошла ошибка при отправки письма на введнный E-mail", Toast.LENGTH_SHORT).show();
                                        })
                        )
                        .addOnFailureListener(e -> {
                            spinKitView.setVisibility(View.INVISIBLE);

                            AtheneDialog atheneDialog = new AtheneDialog(RegisterActivity.this, AtheneDialog.SIMPLE_MESSAGE_TYPE);
                            atheneDialog.setMessageText(getString(R.string.no_internet_error));
                            atheneDialog.setPositiveClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    atheneDialog.dismiss();
                                }
                            });
                            atheneDialog.show();

                            doAfter(false);
                        });
            }
            else {
                Toast.makeText(getApplicationContext(), "Пароли не совпадают", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(RegisterActivity.this, "Вы заполнили не все поля", Toast.LENGTH_LONG).show();
        }
    };

    //####################################################

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        spinKitView.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = auth.getCurrentUser();
                            doAfter(true);

                        } else {
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
            state = true;
        }
    }

    private void controlFirstRun() {
        sharedPreferences.edit().putBoolean(getPackageName() + ".firstrun", true).apply();
    }

    private void signInWithGoogle() {
        spinKitView.setVisibility(View.VISIBLE);
        spinKitView.startAnimation(new AlphaAnimation(0f, 1f));

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, 1);
    }


}
