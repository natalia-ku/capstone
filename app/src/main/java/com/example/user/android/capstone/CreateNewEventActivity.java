package com.example.user.android.capstone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateNewEventActivity extends AppCompatActivity {
    //    private String mForecast;
    private EditText mSportTypeEdit;
    private EditText mSportAddressEdit;
    private EditText mSportDateTimeEdit;
    private EditText mSportDetailsEdit;
    private EditText mSportPeopleNeededEdit;
    private EditText mSportCreatorIdEdit; //in future, it will come from currently signed in user
    private Button mCreateNewEventButton;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);


        mCreateNewEventButton = (Button) findViewById(R.id.add_new_event_button);

        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSportTypeEdit = (EditText) findViewById(R.id.et_sport_type);
                mSportAddressEdit = (EditText) findViewById(R.id.et_address);
                mSportDateTimeEdit = (EditText) findViewById(R.id.et_data_time);
                mSportDetailsEdit = (EditText) findViewById(R.id.et_details);
                mSportPeopleNeededEdit = (EditText) findViewById(R.id.et_people_needed);
                mSportCreatorIdEdit = (EditText) findViewById(R.id.et_creator_id);

                String sportType = mSportTypeEdit.getText().toString();
                String sportAddress = mSportAddressEdit.getText().toString();
                String sportDateTime = mSportDateTimeEdit.getText().toString();
                String sportDetails = mSportDetailsEdit.getText().toString();
                String sportPeopleNeeded = mSportPeopleNeededEdit.getText().toString();
                String sportCreatorId = mSportCreatorIdEdit.getText().toString();

                mEventsRef.push().setValue(new Event(sportType, sportAddress, sportDateTime, sportDetails, sportPeopleNeeded, sportCreatorId));

            }
        });


//        Intent intentThatStartedThisActivity = getIntent();


//        if (intentThatStartedThisActivity != null) {
//            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
//                mForecast = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
//                mWeatherDisplay.setText(mForecast);
//            }
//        }
    }
}
