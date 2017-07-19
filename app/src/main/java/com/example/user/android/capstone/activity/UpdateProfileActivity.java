package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class UpdateProfileActivity extends AppCompatActivity {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUserRef = mRootRef.child("users");
    private EditText mEmailUpdateProfile;
    private EditText mPasswordUpdateProfile;
    private EditText mNameUpdateProfile;
    private EditText mGenderUpdateProfile;
    private EditText mPhotoUpdateProfile;
    private Button mUpdateProfileButton;
    private Button mDeleteProfileButton;
    private Spinner mAgeUpdateSpinner;
    private String userAgeFromSpinner;
    private ArrayAdapter<CharSequence> adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        setUpSpinner();
        final User user = (User) getIntent().getSerializableExtra("user");
        mUpdateProfileButton = (Button) findViewById(R.id.update_button);
        mDeleteProfileButton = (Button) findViewById(R.id.delete_profile_button);
        getUserData(user);
        deleteProfileEventListener(user);
        updateProfileEventListener(user);
    }

    private void updateProfileEventListener(final User user) {
        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query findUserIdQuery = mUserRef.orderByChild("email").equalTo(user.getEmail());
                findUserIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                userId = eventSnapshot.getKey();
                            }
                            updateUserInfo(userId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("ERROR");
                    }
                });

            }
        });
    }

    private void setUpSpinner() {
        mAgeUpdateSpinner = (Spinner) findViewById(R.id.age_update_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.age_sign_up_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAgeUpdateSpinner.setAdapter(adapter);
        mAgeUpdateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userAgeFromSpinner = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void deleteProfileEventListener(final User user){
        mDeleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query findUserIdQuery = mUserRef.orderByChild("email").equalTo(user.getEmail());
                findUserIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                userId = eventSnapshot.getKey();
                            }
                            deleteUserProfile(userId);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("ERROR");
                    }
                });

            }
        });
    }

    private void deleteUserProfile(String userId){
        mUserRef.child(userId).removeValue();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println( "User account deleted.");
                            Toast.makeText(getApplicationContext(), "You successfully deleted account", Toast.LENGTH_LONG).show();
                            Intent intentToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intentToMainActivity);
                        }
                    }
                });

    }

    private void updateUserInfo(String userId) {
        DatabaseReference userRef = mUserRef.child(userId);
        String email = mEmailUpdateProfile.getText().toString();
        String password = mPasswordUpdateProfile.getText().toString();
        String name = mNameUpdateProfile.getText().toString();
        String gender = mGenderUpdateProfile.getText().toString();
        String photo = mPhotoUpdateProfile.getText().toString();
        String age = userAgeFromSpinner;
        if (email.equals("") ||
                name.equals("") || gender.equals("") || photo.equals("") || age.equals("") || password.equals("")) {
            Toast.makeText(getApplicationContext(), "Fill out all fields, please!", Toast.LENGTH_LONG).show();
        } else {
            User user = new User(email, name, gender, photo, age);
            userRef.setValue(user);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (!email.equals(currentUser.getEmail())) {
                currentUser.updateEmail(email);
                currentUser.updatePassword(password);
            }
            Toast.makeText(getApplicationContext(), "You successfully updated profile info", Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            intent.putExtra("user", (Serializable) user);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    private void getUserData(User user) {
        mEmailUpdateProfile = (EditText) findViewById(R.id.update_field_email);
        mPasswordUpdateProfile = (EditText) findViewById(R.id.update_field_password);
        mNameUpdateProfile = (EditText) findViewById(R.id.update_field_name);
        mGenderUpdateProfile = (EditText) findViewById(R.id.update_field_gender);
        mPhotoUpdateProfile = (EditText) findViewById(R.id.update_field_photo);
        int position = adapter.getPosition(user.getAge());
        mAgeUpdateSpinner.setSelection(position);
        mEmailUpdateProfile.setText(user.getEmail());
        mNameUpdateProfile.setText(user.getName());
        mGenderUpdateProfile.setText(user.getGender());
        mPhotoUpdateProfile.setText(user.getPhoto());

    }
}
