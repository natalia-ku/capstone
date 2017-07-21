package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.adapter.EventAdapter;
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

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private RecyclerView recyclerView;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private DatabaseReference mUserRef = mRootRef.child("users");
    private TextView mEventInfoCategory;
    private TextView mEventInfoTitle;
    private TextView mEventInfoDate;
    private TextView mEventInfoTime;
    private TextView mEventInfoDetails;
    private TextView mEventInfoPeopleNeeded;
    private TextView mEventInfoCreatorName;
    private TextView mPeopleCountTextView;
    private ImageView mEventPhoto;
    private TextView mEventInfoAddress;
    private Button mParticipateInEventButton;
    private Button mGetDirectionsButton;
    private Button mCancelParticipationButton;
    private FloatingActionButton mUpdateEvent;
    private Button mAddToCalendarButton;
    private Button mRateEventButton;
    private Button mOpenChatButton;
    private Button mEventOnMap;
    private boolean eventInPast;
    private ImageView mEventCreatorImage;
    private String userPhotoUrl;
    private String eventId;
    private String userId;
    private final int REQUEST_CODE = 23;
    private RatingBar ratingBar;
    private TextView txtRatingValue;
    private LinearLayout mRatingLayout;
    private TextView alreadyVotedTextView;
    private TextView ratingBarTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        initializeTextViewsAndButtons();

        Event event = getIntent().getParcelableExtra("event");
        eventId = event.getId();
        if (!event.checkIfDateInFuture(event.getDate())) {
            eventInPast = true;
        }
        updateEventUI(event);
        updateEventListener(event);
        getEventParticipants(eventId);

        if (currentUser != null) {
            findUserIdForSignedInUser(currentUser, event);
        } else {
            mUpdateEvent.setVisibility(View.GONE);
            mAddToCalendarButton.setVisibility(View.GONE);
            mParticipateInEventButton.setVisibility(View.GONE);
            mCancelParticipationButton.setVisibility(View.GONE);
        }
        setUpGetDirections();
        setUpCreatorIdEvent(event);
        getEventOnMapListener(event);
        listenForChangesInAttendeeList();
        addToCalendarListener(event);
        openChatListener(event);
        setUpParticipateInEventButton(event);
        cancelParticipationEvent(event);
    } // end of onCreate method

    private void checkIfDisplayRating() {
        Query checkIfUserAttendsEventQuery = mEventsRef.child(eventId).child("attendees");
        checkIfUserAttendsEventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userIsAttendee = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        if (eventSnapshot.getKey().equals(userId)) {
                            userIsAttendee = true;
                        }
                    }
                    if (eventInPast){
                        addListenerOnRatingBar();
                    }
                    if (eventInPast && userIsAttendee) {
                        mRatingLayout.setVisibility(View.VISIBLE);
                        Query votedUsersQuery = mEventsRef.child(eventId).child("votedUsers");
                        votedUsersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(userId)){
                                    alreadyVotedTextView.setVisibility(View.VISIBLE);
                                    ratingBarTitleTextView.setVisibility(View.GONE);
                                    mRateEventButton.setVisibility(View.GONE);
                                }
                                else {
                                    addListenerOnButton();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

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
                    Toast.makeText(getApplicationContext(), "Event is full", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void getEventOnMapListener(final Event event) {
        mEventOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("event", (Parcelable) event);
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

    private void findUserIdForSignedInUser(final FirebaseUser currentUser, final Event event) {
        Query findUserQuery = mUserRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userId = eventSnapshot.getKey();
                    }
                    checkIfDisplayRating();
                    checkIfUserAlreadyAttendee();
                    if (!event.getCreatorId().toString().equals(userId)) {
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
                if (eventInPast) {
                    mParticipateInEventButton.setVisibility(View.GONE);
                    mCancelParticipationButton.setVisibility(View.GONE);
                    mAddToCalendarButton.setVisibility(View.GONE);
                    if (userAlreadyInList) {
                        mOpenChatButton.setVisibility(View.VISIBLE);
                    } else {
                        mOpenChatButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeTextViewsAndButtons() {
        mEventPhoto = (ImageView) findViewById(R.id.event_info_photo);
        mPeopleCountTextView = (TextView) findViewById(R.id.people_going_count_textview);
        mEventCreatorImage = (ImageView) findViewById(R.id.event_creator_photo);
        mEventOnMap = (Button) findViewById(R.id.event_on_map_button);
        mOpenChatButton = (Button) findViewById(R.id.open_chat_button);
        mAddToCalendarButton = (Button) findViewById(R.id.add_to_calendar_button);
        mUpdateEvent = (FloatingActionButton) findViewById(R.id.update_event_button);
        mEventInfoTitle = (TextView) findViewById(R.id.event_title_textview);
        mEventInfoCategory = (TextView) findViewById(R.id.event_category_textview);
        mEventInfoAddress = (TextView) findViewById(R.id.event_address_textview);
        mEventInfoDate = (TextView) findViewById(R.id.event_date_textview);
        mEventInfoTime = (TextView) findViewById(R.id.event_time_textview);
        mEventInfoDetails = (TextView) findViewById(R.id.event_details_textview);
        mEventInfoPeopleNeeded = (TextView) findViewById(R.id.event_people_needed_textview);
        mEventInfoCreatorName = (TextView) findViewById(R.id.event_creator_id_textview);
        mGetDirectionsButton = (Button) findViewById(R.id.get_directions_button);
        mParticipateInEventButton = (Button) findViewById(R.id.paticipate_in_event_button);
        mCancelParticipationButton = (Button) findViewById(R.id.cancel_participation_in_event_button);
        mRateEventButton = (Button) findViewById(R.id.submit_rating_button);
        mRatingLayout = (LinearLayout) findViewById(R.id.rating_layout);
        alreadyVotedTextView = (TextView) findViewById(R.id.already_voted_textview);
        ratingBarTitleTextView = (TextView) findViewById(R.id.rating_layout_title);
        txtRatingValue = (TextView) findViewById(R.id.txtRatingValue);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
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

    private void setUpCreatorIdEvent(final Event event) {
        mEventInfoCreatorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = event.getCreatorId();
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
        EventAdapter.setImage(mEventPhoto, e1.getSportCategory());
        mEventInfoAddress.setText(e1.getAddress());
        mEventInfoDate.setText(e1.getDataTime());
        if (e1.getTime() != null) {
            mEventInfoTime.setText(" at " + e1.getTime());
        }
        mEventInfoDetails.setText(e1.getDetails());
        if (Integer.parseInt(e1.getPeopleNeeded()) == 0) {
            mEventInfoPeopleNeeded.setText("FULL");
            mEventInfoPeopleNeeded.setTextColor(Color.parseColor("#EA5251"));
            mEventInfoPeopleNeeded.setBackgroundResource(R.drawable.border_full);
        } else {
            if (Integer.parseInt(e1.getPeopleNeeded()) == 1) {
                mEventInfoPeopleNeeded.setText(e1.getPeopleNeeded() + " SPOT");
            } else {
                mEventInfoPeopleNeeded.setText(e1.getPeopleNeeded() + " SPOTS");
            }
            mEventInfoPeopleNeeded.setTextColor(Color.parseColor("#FF9800"));
            mEventInfoPeopleNeeded.setBackgroundResource(R.drawable.border_available);
        }
        findCreatorPhotoAndName(e1);
    }

    private void findCreatorPhotoAndName(Event event) {
        String userId = event.getCreatorId();
        Query findUserEmailQuery = mUserRef.orderByKey().equalTo(userId);
        findUserEmailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userPhotoUrl = eventSnapshot.child("photo").getValue().toString();
                        String photo = userPhotoUrl;
                        mEventInfoCreatorName.setText(eventSnapshot.child("name").getValue().toString());
                        Glide.with(getApplicationContext()).load(photo).asBitmap().centerCrop().into(new BitmapImageViewTarget(mEventCreatorImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                mEventCreatorImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpRecycleViewForUserList(List<User> eventUsers) {
        Integer count = eventUsers.size();
        if (count == 0) {
            mPeopleCountTextView.setText("Be the first to attend the event");
        } else if (count == 1) {
            mPeopleCountTextView.setText(count + " person is going");
        } else {
            mPeopleCountTextView.setText(count + " people are going");
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view_event_attendees);
        UserAdapter myAdapter = new UserAdapter(getApplicationContext(), eventUsers);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
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
                if (currentUser == null) {
                    mAddToCalendarButton.setVisibility(View.GONE);
                    mOpenChatButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                getEventParticipants(eventIdFinal);
                mAddToCalendarButton.setVisibility(View.GONE);
                mOpenChatButton.setVisibility(View.GONE);
                if (currentUser == null) {
                    mOpenChatButton.setVisibility(View.GONE);
                }
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

    public void addListenerOnRatingBar() {
        DatabaseReference currentEvent = mEventsRef.child(eventId);
        currentEvent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("rating")) {
                    String currentRatingString = dataSnapshot.child("rating").getValue().toString();
                    float currentRating = Float.parseFloat(currentRatingString);
                    ratingBar.setRating(currentRating);
                    String formattedValue = String.format("%.2f", currentRating);
                    txtRatingValue.setText("Current rating: " + formattedValue);
                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        public void onRatingChanged(RatingBar ratingBar, float rating,
                                                    boolean fromUser) {

                        }
                    });
                } else {
                    ratingBar.setRating(0);
                    txtRatingValue.setText("Be the first to rate this event!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void addListenerOnButton() {
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float ratingValue = ratingBar.getRating();
                DatabaseReference currentEvent = mEventsRef.child(eventId);
                currentEvent.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        float newRatingValue = 0;
                        float currentRating;
                        if (!dataSnapshot.hasChild("rating")) {
                            mEventsRef.child(eventId).child("rating").setValue(ratingValue);
                        } else if (dataSnapshot.hasChild("rating")) {
                            String currentRatingString = dataSnapshot.child("rating").getValue().toString();
                            currentRating = Float.parseFloat(currentRatingString);
                            newRatingValue = (currentRating + ratingValue) / 2;
                            mEventsRef.child(eventId).child("rating").setValue(newRatingValue);
                            mEventsRef.child(eventId).child("votedUsers").child(userId).setValue("true");
                        }
                        Toast.makeText(EventInfoActivity.this,
                                "Thanks for rating this event",
                                Toast.LENGTH_SHORT).show();
                        mRateEventButton.setVisibility(View.GONE);
                        String formattedValue = String.format("%.2f", newRatingValue);
                        txtRatingValue.setText("Current rating: " + formattedValue);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        });

    }
}
