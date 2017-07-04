package com.example.user.android.capstone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateNewEventActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mSportTypeEdit;
    private EditText mSportAddressEdit;
    private EditText mSportDateTimeEdit;
    private EditText mSportDetailsEdit;
    private EditText mSportPeopleNeededEdit;
    private Button mCreateNewEventButton;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUserRef = mRootRef.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        mSportTypeEdit = (EditText) findViewById(R.id.et_sport_type);
        mSportAddressEdit = (EditText) findViewById(R.id.et_address);
        mSportDateTimeEdit = (EditText) findViewById(R.id.et_data_time);
        mSportDetailsEdit = (EditText) findViewById(R.id.et_details);
        mSportPeopleNeededEdit = (EditText) findViewById(R.id.et_people_needed);
        mCreateNewEventButton = (Button) findViewById(R.id.add_new_event_button);

        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userEmail = currentUser.getEmail();

                Query findUserQuery = mUserRef.orderByChild("email").equalTo(userEmail);

                findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                String sportCreatorId = eventSnapshot.getKey();// find user's id to create event

                                String sportType = mSportTypeEdit.getText().toString();
                                String sportAddress = mSportAddressEdit.getText().toString();
                                String sportDateTime = mSportDateTimeEdit.getText().toString();
                                String sportDetails = mSportDetailsEdit.getText().toString();
                                String sportPeopleNeeded = mSportPeopleNeededEdit.getText().toString();

                                if (sportAddress.equals("") || sportAddress.equals("") || sportDateTime.equals("") ||
                                        sportDetails.equals("") || sportPeopleNeeded.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Fill out all fields, please!", Toast.LENGTH_LONG).show();
                                } else {
                                    mEventsRef.push().setValue(new Event(sportType, sportAddress, sportDateTime, sportDetails, sportPeopleNeeded, sportCreatorId));
                                    Toast.makeText(getApplicationContext(), "You successfully created new sport event", Toast.LENGTH_LONG).show();
                                    clearForm();
                                    finish();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    private void clearForm() {
        mSportTypeEdit.setText("");
        mSportAddressEdit.setText("");
        mSportDateTimeEdit.setText("");
        mSportDetailsEdit.setText("");
        mSportPeopleNeededEdit.setText("");
    }
}
