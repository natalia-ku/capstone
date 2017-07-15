package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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


public class SignUpActivity extends AppCompatActivity implements
        View.OnClickListener {
    String userAgeFromSpinner;
    private static final String TAG = "EmailPassword";
    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mEmailSignUpField;
    private EditText mPasswordSignUpField;
    private FirebaseAuth mAuth;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("users");
    private EditText mNameSignUpField;
    private EditText mGenderSignUpField;
    private EditText mPhotoSignUpField;

    private Spinner mAgeSignUpSpinner;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViewsElements();
        mAuth = FirebaseAuth.getInstance();
        setUpSpinner();
    }






    
    private void initializeViewsElements(){
        mStatusTextView = (TextView) findViewById(R.id.status);
        // SIGN IN:
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        // SIGN UP:
        mEmailSignUpField = (EditText) findViewById(R.id.signup_field_email);
        mPasswordSignUpField = (EditText) findViewById(R.id.signup_field_password);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_up_button).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    private void setUpSpinner() {
        mAgeSignUpSpinner = (Spinner) findViewById(R.id.age_sign_up_form_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.age_sign_up_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAgeSignUpSpinner.setAdapter(adapter);
        mAgeSignUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userAgeFromSpinner = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                            //CREATE NEW USER IN DATABASE:
                            mNameSignUpField = (EditText) findViewById(R.id.name_sign_up_form_editview);
                            mGenderSignUpField = (EditText) findViewById(R.id.gender_sign_up_form_editview);
                            mPhotoSignUpField = (EditText) findViewById(R.id.photo_sign_up_form_editview);
                            mEmailSignUpField = (EditText) findViewById(R.id.signup_field_email);

                            String userName = mNameSignUpField.getText().toString();
                            String userGender = mGenderSignUpField.getText().toString();
                            String userPhoto = mPhotoSignUpField.getText().toString();
                            String userAge = userAgeFromSpinner;
                            String userEmail = mEmailSignUpField.getText().toString();

                            mUserRef.push().setValue(new User(userEmail, userName, userGender, userPhoto, userAge));
                            Toast.makeText(SignUpActivity.this, "You successfully created new account",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            String exceptionString = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            String exceptionEmailCollisionString = "com.google.firebase.auth.FirebaseAuthUserCollisionException";
                            if (task.getException().getClass().getName().equals(exceptionString)) {
                                Toast.makeText(SignUpActivity.this, "The email address is badly formatted",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            else if (task.getException().getClass().getName().equals(exceptionEmailCollisionString)) {
                                Toast.makeText(SignUpActivity.this, "User with this email already exists",
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
                            Toast.makeText(SignUpActivity.this, "You successfully signed in",
                                    Toast.LENGTH_SHORT).show();

                            startActivity( new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            String exceptionBadEmailString = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            String exceptionString = "com.google.firebase.auth.FirebaseAuthInvalidUserException";
                            if (task.getException().getClass().getName().equals(exceptionString)) {
                                Toast.makeText(SignUpActivity.this, "There is no user with given email and password." +
                                                "Try again or press SIGN UP to create new account",
                                        Toast.LENGTH_SHORT).show();
                            } else if (task.getException().getClass().getName().equals(exceptionBadEmailString)) {
                                Toast.makeText(SignUpActivity.this, "The email address is badly formatted",
                                        Toast.LENGTH_SHORT).show();
                            } else {
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
        if (user == null) {
            mStatusTextView.setText("signed out");
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