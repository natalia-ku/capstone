package com.example.user.android.capstone;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class EventInfoActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView mEventInfoSportType;
    TextView mEventInfoAddress;
    TextView mEventInfoDate;
    TextView mEventInfoTime;
    TextView mEventInfoDetails;
    TextView mEventInfoPeopleNeeded;
    TextView mEventInfoCreatorId;
    Button mGetDirectionsButton;
    Button mParticipateInEventButton;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUserRef = mRootRef.child("users");

    String eventId; // to create user-event relations
    String userId; // to create user-event relations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        mEventInfoSportType = (TextView) findViewById(R.id.event_sporttype_textview);
        mEventInfoAddress = (TextView) findViewById(R.id.event_address_textview);
        mEventInfoDate = (TextView) findViewById(R.id.event_date_textview);
        mEventInfoTime = (TextView) findViewById(R.id.event_time_textview);
        mEventInfoDetails = (TextView) findViewById(R.id.event_details_textview);
        mEventInfoPeopleNeeded = (TextView) findViewById(R.id.event_people_needed_textview);
        mEventInfoCreatorId = (TextView) findViewById(R.id.event_creator_id_textview);
        mGetDirectionsButton = (Button) findViewById(R.id.get_directions_button);
        mParticipateInEventButton = (Button) findViewById(R.id.paticipate_in_event_button);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        eventId = getIntent().getStringExtra("id");

        // Find signed in user id:
        if (currentUser != null) {
            Query findUserQuery = mUserRef.orderByChild("email").equalTo(currentUser.getEmail());
            findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                           userId = eventSnapshot.getKey();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("ERROR");
                }
            });
            mParticipateInEventButton.setVisibility(View.VISIBLE);
        } else {
            mParticipateInEventButton.setVisibility(View.GONE);
        }

        getEventInfo(eventId);

        mGetDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addressString = mEventInfoAddress.getText().toString();
                Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    System.out.println("Couldn't call " + geoLocation.toString()
                            + ", no receiving apps installed!");
                }
            }
        });

        mEventInfoCreatorId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = mEventInfoCreatorId.getText().toString();
                Query findUserEmailQuery = mUserRef.orderByKey().equalTo(userId);
                findUserEmailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                // START ACTIVITY TO SHOW CREATOR IF EVENT:
                                String userEmail = eventSnapshot.child("email").getValue().toString();
                                Intent intentToSeeUserProfile = new Intent(getApplicationContext(), UserProfileActivity.class);
                                intentToSeeUserProfile.putExtra("userEmail", userEmail);
                                startActivity(intentToSeeUserProfile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mParticipateInEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("ON CLICK:");
                System.out.println(userId);
                addAttendeeToEvent(eventId, userId);
                addEventToUserEventsList(eventId, userId);
            }
        });

    }


    private void getEventInfo(String eventId) {
        Query eventDetailsQuery = mEventsRef.orderByKey().equalTo(eventId);
        eventDetailsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String address = (String) eventSnapshot.child("address").getValue();
                        String date = (String) eventSnapshot.child("dataTime").getValue();
                        String time = (String) eventSnapshot.child("time").getValue();
                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        String details = (String) eventSnapshot.child("details").getValue();
                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        String sportType = (String) eventSnapshot.child("sportType").getValue();

                        Event e1 = new Event(sportType, address, date, time, details, peopleNeeded, creatorId);

                        mEventInfoSportType.setText(e1.getSportType());
                        mEventInfoAddress.setText(e1.getAddress());
                        mEventInfoDate.setText(e1.getDataTime());
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
    }

    private void addAttendeeToEvent(String eventId, String userId) {
        mEventsRef.child(eventId).child("attendees").child(userId).setValue("true");
    }


    private void addEventToUserEventsList(String eventId, String userId) {
        mUserRef.child(userId).child("userEvents").child(eventId).setValue("true");
    }


}
