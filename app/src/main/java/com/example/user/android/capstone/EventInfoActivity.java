package com.example.user.android.capstone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class EventInfoActivity extends AppCompatActivity {

    TextView mEventInfoSportType;
    TextView mEventInfoAddress;
    TextView mEventInfoDateTime;
    TextView mEventInfoDetails;
    TextView mEventInfoPeopleNeeded;
    TextView mEventInfoCreatorId;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        String eventId = getIntent().getStringExtra("id");
        System.out.println("EVENT ID: " + eventId);

        Query eventDetailsQuery = mEventsRef.orderByKey().equalTo("1");

        eventDetailsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String address = (String) eventSnapshot.child("address").getValue();
                        String dataTime = (String) eventSnapshot.child("dataTime").getValue();
                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        String details = (String) eventSnapshot.child("details").getValue();
                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        String sportType = (String) eventSnapshot.child("sportType").getValue();
                        Event e1 = new Event(sportType, address, dataTime, details, peopleNeeded, creatorId);
                        mEventInfoSportType.setText(e1.getSportType());
                        mEventInfoAddress.setText(e1.getAddress());
                        mEventInfoDateTime.setText(e1.getDataTime());
                        mEventInfoDetails.setText(e1.getDetails());
                        mEventInfoPeopleNeeded.setText(e1.getPeopleNeeded());
                        mEventInfoCreatorId.setText(e1.getCreatorId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//       

        mEventInfoSportType = (TextView) findViewById(R.id.event_sporttype_textview);
        mEventInfoAddress = (TextView) findViewById(R.id.event_address_textview);
        mEventInfoDateTime = (TextView) findViewById(R.id.event_date_time_textview);
        mEventInfoDetails = (TextView) findViewById(R.id.event_details_textview);
        mEventInfoPeopleNeeded = (TextView) findViewById(R.id.event_people_needed_textview);
        mEventInfoCreatorId = (TextView) findViewById(R.id.event_creator_id_textview);

    }
}
