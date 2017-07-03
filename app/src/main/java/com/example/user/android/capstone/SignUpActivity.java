package com.example.user.android.capstone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignUpActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignOutButton;

    private EditText mEmailSignUpField;
    private EditText mPasswordSignUpField;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mStatusTextView = (TextView) findViewById(R.id.status);

        // SIGN IN:
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        // SIGN UP:
        mEmailSignUpField = (EditText) findViewById(R.id.signup_field_email);
        mPasswordSignUpField = (EditText) findViewById(R.id.signup_field_password);


        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_up_button).setOnClickListener(this);
//        findViewById(R.id.sign_out_button).setOnClickListener(this);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);

        mAuth = FirebaseAuth.getInstance();

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm("signUp")) {
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "You successfully created new account",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            String exceptionString = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            if(task.getException().getClass().getName().equals(exceptionString)){
                                Toast.makeText(SignUpActivity.this, "The email address is badly formatted",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                            else {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            }
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
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
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            String exceptionBadEmailString = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            String exceptionString = "com.google.firebase.auth.FirebaseAuthInvalidUserException";
                            if (task.getException().getClass().getName().equals(exceptionString)) {
                                Toast.makeText(SignUpActivity.this, "There is no user with given email and password." +
                                                "Try again or press SIGN UP to create new account",
                                        Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                            }
                            else if (task.getException().getClass().getName().equals(exceptionBadEmailString))
                            {
                                Toast.makeText(SignUpActivity.this, "The email address is badly formatted",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
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

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }


    private boolean validateForm(String type) {
        boolean valid = true;
        if (type.equals("signIn")) {
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
        } else if (type.equals("signUp")) {
            String email = mEmailSignUpField.getText().toString();
            if (TextUtils.isEmpty(email)) {
                mEmailSignUpField.setError("Required.");
                valid = false;
            } else {
                mEmailSignUpField.setError(null);
            }

            String password = mPasswordSignUpField.getText().toString();
            if (TextUtils.isEmpty(password)) {
                mPasswordSignUpField.setError("Required.");
                valid = false;
            } else {
                mPasswordSignUpField.setError(null);
            }
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText("signed in");

            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.field_email).setVisibility(View.GONE);
            findViewById(R.id.field_password).setVisibility(View.GONE);
            findViewById(R.id.enter_password_text_view).setVisibility(View.GONE);
            findViewById(R.id.enter_email_text_view).setVisibility(View.GONE);
            findViewById(R.id.sign_up_button).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_in_textview).setVisibility(View.GONE);

            findViewById(R.id.signup_field_email).setVisibility(View.GONE);
            findViewById(R.id.signup_field_password).setVisibility(View.GONE);
            findViewById(R.id.sign_up_textview).setVisibility(View.GONE);
            findViewById(R.id.enter_email_text_view).setVisibility(View.GONE);
            findViewById(R.id.enter_password_signup_text_view).setVisibility(View.GONE);
            findViewById(R.id.enter_email_signup_text_view).setVisibility(View.GONE);
            findViewById(R.id.sign_up_textview).setVisibility(View.GONE);
        } else {
            mStatusTextView.setText("signed out");

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_up_button).setVisibility(View.VISIBLE);

            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.field_email).setVisibility(View.VISIBLE);
            findViewById(R.id.field_password).setVisibility(View.VISIBLE);
            findViewById(R.id.enter_password_text_view).setVisibility(View.VISIBLE);
            findViewById(R.id.enter_email_text_view).setVisibility(View.VISIBLE);

            findViewById(R.id.sign_in_textview).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_up_textview).setVisibility(View.VISIBLE);
            findViewById(R.id.signup_field_email).setVisibility(View.VISIBLE);
            findViewById(R.id.signup_field_password).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_up_textview).setVisibility(View.VISIBLE);
            findViewById(R.id.enter_email_text_view).setVisibility(View.VISIBLE);
            findViewById(R.id.enter_password_signup_text_view).setVisibility(View.VISIBLE);
            findViewById(R.id.enter_email_signup_text_view).setVisibility(View.VISIBLE);
            mEmailField.setText("");
            mPasswordField.setText("");
            mPasswordSignUpField.setText("");
            mEmailSignUpField.setText("");

            

        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_up_button) {
            createAccount(mEmailSignUpField.getText().toString(), mPasswordSignUpField.getText().toString());
        } else if (i == R.id.sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}