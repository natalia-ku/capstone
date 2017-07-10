package com.example.user.android.capstone;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class EventInfoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    TextView mEventInfoTitle;
    TextView mEventInfoCategory;
    TextView mEventInfoAddress;
    TextView mEventInfoDate;
    TextView mEventInfoTime;
    TextView mEventInfoDetails;
    TextView mEventInfoPeopleNeeded;
    TextView mEventInfoCreatorId;
    Button mGetDirectionsButton;
    Button mParticipateInEventButton;
    Button mCancelParticipationButton;
    Button mUpdateEvent;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUserRef = mRootRef.child("users");

    String eventId; // to create user-event relations
    String userId; // to create user-event relations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        initializeTextViews();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

       // eventId = getIntent().getStringExtra("id");
        Event event = getIntent().getParcelableExtra("event");
        eventId = event.getId();

        
        getEventInfo(event);


        updateEventListener();
        // getEventInfo(eventId);
        getEventParticipants(eventId);


        // Find USER ID FOR SIGNED-IN user:
        if (currentUser != null) {
            findUserIdForSignedInUser(currentUser);
            String currentUserId = userId;
            System.out.println("USER ID " + currentUserId);
            System.out.println("CREATOR ID " + mEventInfoCreatorId.getText());
            if (!mEventInfoCreatorId.getText().toString().equals(currentUserId)) {
                mUpdateEvent.setVisibility(View.GONE);
            }
        } else {
            mParticipateInEventButton.setVisibility(View.GONE);
            mCancelParticipationButton.setVisibility(View.GONE);
        }


        setUpGetDirections();
        setUpCreatorIdEvent();
        setUpParticipateInEventButton();

        setUpParticipateInEventButton();
        cancelParticipationEvent();

        listenForChangesInAttendeeList();


    } // end of onCreate method


    private void updateEventListener() {

        mUpdateEvent = (Button) findViewById(R.id.update_event_button);
        mUpdateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = (String) mEventInfoAddress.getText();
                String date = (String) mEventInfoDate.getText();
                String time = (String) mEventInfoTime.getText();
                String creatorId = (String) mEventInfoCreatorId.getText();
                String details = (String) mEventInfoDetails.getText();
                String peopleNeeded = (String) mEventInfoPeopleNeeded.getText();
                String title = (String) mEventInfoTitle.getText();
                String sportCategory = (String) mEventInfoCategory.getText();

                Event event = new Event(sportCategory, title, address, date, time, details, peopleNeeded, creatorId);

                Intent intentToUpdateEvent = new Intent(getApplicationContext(), UpdateEventActivity.class);
                intentToUpdateEvent.putExtra("event", (Serializable) event);
                startActivity(intentToUpdateEvent);
            }
        });
    }

    private void getEventParticipants(String currentEventId) {
        // GET list of participants IDs:
        final List<String> userIdsList = new ArrayList<>();
        Query getEventUserIdsQuery = mEventsRef.child(currentEventId).child("attendees");
        getEventUserIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userIdsList.add(eventSnapshot.getKey());
                    }
                }
                getEventAttendees(userIdsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getEventAttendees(List<String> userIdsList) {
        //TO GET USERS, PARTICIPATED IN EVENT:
        final List<User> eventUsers = new ArrayList<>();
        if (eventUsers.size() == 0) {
            setUpRecycleViewForUserList(eventUsers);
        }
        for (String userID : userIdsList) {
            Query eventUsersQuery = mUserRef.orderByKey().equalTo(userID);
            eventUsersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String id = eventSnapshot.getKey();
                            String email = (String) eventSnapshot.child("email").getValue();
                            String name = (String) eventSnapshot.child("name").getValue();
                            String gender = (String) eventSnapshot.child("gender").getValue();
                            String photo = (String) eventSnapshot.child("photo").getValue();
                            String age = (String) eventSnapshot.child("age").getValue();
                            User u1 = new User(id, email, name, gender, photo, age);
                            eventUsers.add(u1);
                        }
                        // SET UP LAYOUT FOR SHOWING USERS:
                        setUpRecycleViewForUserList(eventUsers);
                    } else {
                        System.out.println("Error: no data was found");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void addAttendeeToEvent(String eventId, String userId) {
        mEventsRef.child(eventId).child("attendees").child(userId).setValue("true");
    }

    private void addEventToUserEventsList(String eventId, String userId) {
        mUserRef.child(userId).child("userEvents").child(eventId).setValue("true");
    }

    private void removeAttendeeFromEvent(String eventId, String userId) {
        mEventsRef.child(eventId).child("attendees").child(userId).removeValue();

    }

    private void removeEventFromUserEventList(String eventId, String userId) {
        mUserRef.child(userId).child("userEvents").child(eventId).removeValue();
    }


    private void setUpParticipateInEventButton() {
        mParticipateInEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAttendeeToEvent(eventId, userId);
                addEventToUserEventsList(eventId, userId);
                mParticipateInEventButton.setVisibility(View.GONE);
                mCancelParticipationButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cancelParticipationEvent() {
        mCancelParticipationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAttendeeFromEvent(eventId, userId);
                removeEventFromUserEventList(eventId, userId);
                mParticipateInEventButton.setVisibility(View.VISIBLE);
                mCancelParticipationButton.setVisibility(View.GONE);
            }
        });
    }


    private void findUserIdForSignedInUser(FirebaseUser currentUser) {
        Query findUserQuery = mUserRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userId = eventSnapshot.getKey();
                    }
                    checkIfUserAlreadyAttendee();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }

    private void checkIfUserAlreadyAttendee() {
        // CHECK if user already in attendees list:
        Query checkIfUserAttendsEventQuery = mEventsRef.child(eventId).child("attendees");
        checkIfUserAttendsEventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userAlreadyInList = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        if (eventSnapshot.getKey().equals(userId)) {
                            userAlreadyInList = true;
                        }
                    }
                }
                if (userAlreadyInList) {
                    mCancelParticipationButton.setVisibility(View.VISIBLE);
                    mParticipateInEventButton.setVisibility(View.GONE);
                } else {
                    mCancelParticipationButton.setVisibility(View.GONE);
                    mParticipateInEventButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initializeTextViews() {
        mEventInfoTitle = (TextView) findViewById(R.id.event_title_textview);
        mEventInfoCategory = (TextView) findViewById(R.id.event_category_textview);
        mEventInfoAddress = (TextView) findViewById(R.id.event_address_textview);
        mEventInfoDate = (TextView) findViewById(R.id.event_date_textview);
        mEventInfoTime = (TextView) findViewById(R.id.event_time_textview);
        mEventInfoDetails = (TextView) findViewById(R.id.event_details_textview);
        mEventInfoPeopleNeeded = (TextView) findViewById(R.id.event_people_needed_textview);
        mEventInfoCreatorId = (TextView) findViewById(R.id.event_creator_id_textview);
        mGetDirectionsButton = (Button) findViewById(R.id.get_directions_button);
        mParticipateInEventButton = (Button) findViewById(R.id.paticipate_in_event_button);
        mCancelParticipationButton = (Button) findViewById(R.id.cancel_participation_in_event_button);
    }

    private void setUpGetDirections() {
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
    }


    private void setUpCreatorIdEvent() {
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
    }


//    private void getEventInfo(String eventId) {
//        Query eventDetailsQuery = mEventsRef.orderByKey().equalTo(eventId);
//        eventDetailsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
//                        String address = (String) eventSnapshot.child("address").getValue();
//                        String date = (String) eventSnapshot.child("dataTime").getValue();
//                        String time = (String) eventSnapshot.child("time").getValue();
//                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
//                        String details = (String) eventSnapshot.child("details").getValue();
//                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
//                        String title = (String) eventSnapshot.child("title").getValue();
//                        String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
//
//                        Event e1 = new Event(sportCategory, title, address, date, time, details, peopleNeeded, creatorId);
//                        mEventInfoCategory.setText(e1.getSportCategory());
//                        mEventInfoTitle.setText(e1.getTitle());
//                        mEventInfoAddress.setText(e1.getAddress());
//                        mEventInfoDate.setText(e1.getDataTime());
//                        mEventInfoTime.setText(e1.getTime());
//                        mEventInfoDetails.setText(e1.getDetails());
//                        mEventInfoPeopleNeeded.setText(e1.getPeopleNeeded());
//                        mEventInfoCreatorId.setText(e1.getCreatorId());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void getEventInfo(Event e1) {
        mEventInfoCategory.setText(e1.getSportCategory());
        mEventInfoTitle.setText(e1.getTitle());
        mEventInfoAddress.setText(e1.getAddress());
        mEventInfoDate.setText(e1.getDataTime());
        mEventInfoTime.setText(e1.getTime());
        mEventInfoDetails.setText(e1.getDetails());
        mEventInfoPeopleNeeded.setText(e1.getPeopleNeeded());
        mEventInfoCreatorId.setText(e1.getCreatorId());
    }


    private void setUpRecycleViewForUserList(List<User> eventUsers) {
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_event_attendees);
        UserAdapter myAdapter = new UserAdapter(getApplicationContext(), eventUsers);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }


    private void listenForChangesInAttendeeList() {
        DatabaseReference attendeeRef = mEventsRef.child(eventId).child("attendees");
        final String eventIdFinal = eventId;
        attendeeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("I AM IN CHILD ADDED");
                getEventParticipants(eventIdFinal);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("I AM IN CHILD CHANGED");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("I AM IN CHILD REMOVE");
                getEventParticipants(eventIdFinal);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
