package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class SignUpActivity extends AppCompatActivity  implements
        View.OnClickListener {

    private String userAgeFromSpinner;
    private static final String TAG = "EmailPassword";
    private EditText mEmailSignUpField;
    private EditText mPasswordSignUpField;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUserRef = mRootRef.child("users");
    private EditText mNameSignUpField;
    private EditText mGenderSignUpField;
    private EditText mPhotoSignUpField;
    private Spinner mAgeSignUpSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initializeViewsElements();
        mAuth = FirebaseAuth.getInstance();
        setUpSpinner();
    }


    private void initializeViewsElements(){
        mEmailSignUpField = (EditText) findViewById(R.id.signup_field_email);
        mPasswordSignUpField = (EditText) findViewById(R.id.signup_field_password);
        findViewById(R.id.sign_up_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        createAccount(mEmailSignUpField.getText().toString(), mPasswordSignUpField.getText().toString());
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
        if (!validateForm()) {
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



    private boolean validateForm() {
        boolean valid = true;
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
        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            mPasswordSignUpField.setText("");
            mEmailSignUpField.setText("");
        }
    }

}
