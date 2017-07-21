package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignInActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = "EmailPassword";
    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUserRef = mRootRef.child("users");
    private TextView mSignUpButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initializeViewsElements();
        mAuth = FirebaseAuth.getInstance();
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });
    }

    private void initializeViewsElements() {
        mStatusTextView = (TextView) findViewById(R.id.status);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mSignUpButton = (TextView) findViewById(R.id.goto_signup_button_activity);
        mSignUpButton.setPaintFlags(mSignUpButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }



    private void signIn(String email, String password) {
        if (!validateForm("signIn")) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignInActivity.this, "You successfully signed in",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            String exceptionBadEmailString = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            String exceptionString = "com.google.firebase.auth.FirebaseAuthInvalidUserException";
                            if (task.getException().getClass().getName().equals(exceptionString)) {
                                Toast.makeText(SignInActivity.this, "There is no user with given email and password." +
                                                "Try again or press SIGN UP to create new account",
                                        Toast.LENGTH_SHORT).show();
                            } else if (task.getException().getClass().getName().equals(exceptionBadEmailString)) {
                                Toast.makeText(SignInActivity.this, "The email address is badly formatted",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_LONG).show();
                            }
                            updateUI(null);
                        }
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText("Auth failed");
                        }
                    }
                });
    }

    private boolean validateForm(String type) {
        boolean valid = true;
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }
        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            mStatusTextView.setText("signed out");
            mEmailField.setText("");
            mPasswordField.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
    }
}