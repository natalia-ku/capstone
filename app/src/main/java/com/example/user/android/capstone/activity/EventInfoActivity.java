package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.User;
import com.example.user.android.capstone.adapter.UserAdapter;
import com.example.user.android.capstone.model.Event;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    Button mAddToCalendarButton;
    Button mOpenChatButton;
    Button mEventOnMap;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUserRef = mRootRef.child("users");

    String eventId; // to create user-event relations
    String userId; // to create user-event relations
    final int REQUEST_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        initializeTextViewsAndButtons();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Event event = getIntent().getParcelableExtra("event");
        eventId = event.getId();

        updateEventUI(event);
        updateEventListener(event);
        getEventParticipants(eventId);

        // Find USER ID FOR SIGNED-IN user:
        if (currentUser != null) {
            findUserIdForSignedInUser(currentUser);
        } else {
            mParticipateInEventButton.setVisibility(View.GONE);
            mCancelParticipationButton.setVisibility(View.GONE);
        }

        setUpGetDirections();
        setUpCreatorIdEvent();
        getEventOnMapListener(event);
        setUpParticipateInEventButton(event);
        cancelParticipationEvent(event);
        listenForChangesInAttendeeList();
        addToCalendarListener(event);
        openChatListener(event);

    } // end of onCreate method


    private void updateEventListener(final Event event) {
        mUpdateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToUpdateEvent = new Intent(getApplicationContext(), UpdateEventActivity.class);
                intentToUpdateEvent.putExtra("event", (Serializable) event);
                startActivityForResult(intentToUpdateEvent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Event event = (Event) data.getSerializableExtra("event");
                updateEventUI(event);
            }
        }
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
                displayAttendeesList(userIdsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void displayAttendeesList(List<String> userIdsList) {
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
                        setUpRecycleViewForUserList(eventUsers);
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


    private void setUpParticipateInEventButton(final Event event) {
        mParticipateInEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = Integer.parseInt(event.getPeopleNeeded());
                if (count >= 1) {
                    addAttendeeToEvent(eventId, userId);
                    addEventToUserEventsList(eventId, userId);
                    mParticipateInEventButton.setVisibility(View.GONE);
                    mCancelParticipationButton.setVisibility(View.VISIBLE);
                    updateAttendeesCount(false, event);
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, we don't need more people for this event", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void getEventOnMapListener(final Event event){
        mEventOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("event",(Parcelable) event);
                        startActivity(intent);
            }
        });
    }

    private void cancelParticipationEvent(final Event event) {
        mCancelParticipationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAttendeeFromEvent(eventId, userId);
                removeEventFromUserEventList(eventId, userId);
                mParticipateInEventButton.setVisibility(View.VISIBLE);
                mCancelParticipationButton.setVisibility(View.GONE);
                updateAttendeesCount(true, event);

            }
        });
    }

    private void updateAttendeesCount(boolean increaseCount, Event event) {
        int count = Integer.parseInt(event.getPeopleNeeded());
        if (!increaseCount) {
            mEventsRef.child(eventId).child("peopleNeeded").setValue(count - 1);
            event.setPeopleNeeded(String.valueOf(count - 1));
            updateEventUI(event);
        } else {
            mEventsRef.child(eventId).child("peopleNeeded").setValue(count + 1);
            event.setPeopleNeeded(String.valueOf(count + 1));
            updateEventUI(event);
        }
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
                    if (!mEventInfoCreatorId.getText().toString().equals(userId)) {
                        mUpdateEvent.setVisibility(View.GONE);
                    }
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
                    mAddToCalendarButton.setVisibility(View.VISIBLE);
                    mOpenChatButton.setVisibility(View.VISIBLE);
                } else {
                    mAddToCalendarButton.setVisibility(View.GONE);
                    mOpenChatButton.setVisibility(View.GONE);
                    mCancelParticipationButton.setVisibility(View.GONE);
                    mParticipateInEventButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initializeTextViewsAndButtons() {
        mEventOnMap = (Button) findViewById(R.id.event_on_map_button);
        mOpenChatButton = (Button) findViewById(R.id.open_chat_button);
        mAddToCalendarButton = (Button) findViewById(R.id.add_to_calendar_button);
        mUpdateEvent = (Button) findViewById(R.id.update_event_button);
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


    private void updateEventUI(Event e1) {
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
                getEventParticipants(eventIdFinal);
                mAddToCalendarButton.setVisibility(View.VISIBLE);
                mOpenChatButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                getEventParticipants(eventIdFinal);
                mAddToCalendarButton.setVisibility(View.GONE);
                mOpenChatButton.setVisibility(View.GONE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addToCalendarListener(final Event event) {
        mAddToCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date eventDate = null;
                try {
                    eventDate = formatter.parse(event.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setType("vnd.android.cursor.item/event");
                calIntent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getAddress());
                calIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDetails());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventDate);
                startActivity(calIntent);
            }
        });
    }

    private void openChatListener(final Event event) {
        mOpenChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToOpenChat = new Intent(getApplicationContext(), ChatActivity.class);
                intentToOpenChat.putExtra("event", (Serializable) event);
                startActivity(intentToOpenChat);
            }
        });

    }

}
