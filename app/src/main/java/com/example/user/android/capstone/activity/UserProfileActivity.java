package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.user.android.capstone.adapter.EventAdapter;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.Event;
import com.example.user.android.capstone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    TextView mUserName;
    TextView mUserEmail;
    TextView mUserAge;
    TextView mUserGender;
    ImageView mUserPhotoImage;
    FloatingActionButton mEditProfileButton;
    final int REQUEST_CODE = 23;
    TextView eventsUserCreatedTextView;
    TextView eventsUserParticipatedTextView;

    private FirebaseAuth mAuth;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("users");
    DatabaseReference mEventRef = mRootRef.child("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        String userEmail = getIntent().getStringExtra("userEmail"); // when clicked on other user profile
        mEditProfileButton = (FloatingActionButton) findViewById(R.id.edit_profile);
        if (userEmail == null) { // to see  signed in user own profile
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            userEmail = currentUser.getEmail();
        }
        else{
            mEditProfileButton.setVisibility(View.GONE);
        }
        setUpProfileInfo(userEmail);

    } // END of OnCreate

    private void setUpProfileInfo(String userEmail) {
        Query userProfileQuery = mUserRef.orderByChild("email").equalTo(userEmail);
        userProfileQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    final User user;
                    String currentUserId = "";
                    String name = "";
                    String email = "";
                    String age = "";
                    String gender = "";
                    String photo = "";
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        findTextViews();
                        currentUserId = (String) eventSnapshot.getKey();
                        name = (String) eventSnapshot.child("name").getValue();
                        email = (String) eventSnapshot.child("email").getValue();
                        age = (String) eventSnapshot.child("age").getValue();
                        gender = (String) eventSnapshot.child("gender").getValue();
                        photo = (String) eventSnapshot.child("photo").getValue();
                    }
                    user = new User(currentUserId, email, name, gender, photo, age);
                    setTextToViews(email, name, age, gender, photo);
                    findCreatedByUserEvents(currentUserId);
                    findEventsUserParticipatedIn(currentUserId);


                        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intentToUpdateProfile = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                                intentToUpdateProfile.putExtra("user", (Serializable) user);
                                startActivityForResult(intentToUpdateProfile, REQUEST_CODE);
                            }
                        });





                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                User user = (User) data.getSerializableExtra("user");
                setTextToViews(user.getEmail(), user.getName(), user.getAge(), user.getGender(), user.getPhoto());
            }
        }
    }


    private void findEventsUserParticipatedIn(String currentUserId) {
        // GET list of user event IDs:
        final List<String> eventIdsList = new ArrayList<>();
        Query getUserEventIdsQuery = mUserRef.child(currentUserId).child("userEvents");
        getUserEventIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventIdsList.add(eventSnapshot.getKey());
                    }
                    getEventsUserParticipatedIn(eventIdsList);
                } else {
                    eventsUserParticipatedTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getEventsUserParticipatedIn(List<String> eventIdsList) {
        //TO GET EVENTS USER PARTICIPATED IN:
        final List<Event> userEvents = new ArrayList<>();
        for (String eventID : eventIdsList) {
            Query eventsUserAttendsQuery = mEventRef.orderByKey().equalTo(eventID);
            eventsUserAttendsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String id = eventSnapshot.getKey();
                            String address = (String) eventSnapshot.child("address").getValue();
                            String date = (String) eventSnapshot.child("dataTime").getValue();
                            String time = (String) eventSnapshot.child("time").getValue();
                            String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                            String details = (String) eventSnapshot.child("details").getValue();
                            String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                            String title = (String) eventSnapshot.child("title").getValue();
                            String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();

                            Event e1 = new Event(sportCategory, id, title, address, date, time, details, peopleNeeded, creatorId);
                            userEvents.add(e1);
                        }
                        setUpRecycleView(userEvents, false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void findCreatedByUserEvents(String currentUserId) {
        final List<Event> userEvents = new ArrayList<>();
        //TO GET EVENTS USER CREATED
        Query createdByUserEventsQuery = mEventRef.orderByChild("creatorId").equalTo(currentUserId);
        createdByUserEventsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String id = eventSnapshot.getKey();
                        String address = (String) eventSnapshot.child("address").getValue();
                        String date = (String) eventSnapshot.child("dataTime").getValue();
                        String time = (String) eventSnapshot.child("time").getValue();
                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        String details = (String) eventSnapshot.child("details").getValue();
                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        String title = (String) eventSnapshot.child("title").getValue();
                        String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();

                        Event e1 = new Event(sportCategory, id, title, address, date, time, details, peopleNeeded, creatorId);
                        userEvents.add(e1);
                    }
                }
                if (userEvents.size() == 0) {
                    eventsUserCreatedTextView.setVisibility(View.GONE);
                }
                setUpRecycleView(userEvents, true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void findTextViews() {
        mUserName = (TextView) findViewById(R.id.name_profile_info);
        mUserEmail = (TextView) findViewById(R.id.email_profile_info);
        mUserAge = (TextView) findViewById(R.id.age_profile_info);
        mUserGender = (TextView) findViewById(R.id.gender_profile_info);
        mUserPhotoImage = (ImageView) findViewById(R.id.user_photo);

        eventsUserCreatedTextView = (TextView) findViewById(R.id.events_user_created_textview);
        eventsUserParticipatedTextView = (TextView) findViewById(R.id.events_user_participated_textview);
    }

    private void setTextToViews(String email, String name, String age, String gender, String photo) {
        mUserEmail.setText(email);
        mUserName.setText(name);
        mUserAge.setText(age);
        mUserGender.setText(gender);
        Glide.with(getApplicationContext()).load(photo).asBitmap().centerCrop().into(new BitmapImageViewTarget(mUserPhotoImage) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplication().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                mUserPhotoImage.setImageDrawable(circularBitmapDrawable);
            }
        });

    }

    private void setUpRecycleView(List<Event> userEvents, boolean eventsCreatedByUser) {
        if (eventsCreatedByUser) {
            recyclerView = (RecyclerView) findViewById(R.id.recycle_view_events_created_by_user);
        } else {
            recyclerView = (RecyclerView) findViewById(R.id.recycle_view_events_user_participated_in);
        }
        EventAdapter myAdapter = new EventAdapter(getApplicationContext(), userEvents);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


    }


}
