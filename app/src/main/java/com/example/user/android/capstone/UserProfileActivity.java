package com.example.user.android.capstone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    TextView mUserName;
    TextView mUserEmail;
    TextView mUserAge;
    TextView mUserGender;
    TextView mUserPhoto;

    private FirebaseAuth mAuth;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserRef = mRootRef.child("users");
    DatabaseReference mEventRef = mRootRef.child("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserEmail = currentUser.getEmail();

        Query userProfileQuery = mUserRef.orderByChild("email").equalTo(currentUserEmail);
               userProfileQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()) {
                           for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                               mUserName = (TextView) findViewById(R.id.name_profile_info);
                               mUserEmail = (TextView) findViewById(R.id.email_profile_info);
                               mUserAge = (TextView) findViewById(R.id.age_profile_info);
                               mUserGender = (TextView) findViewById(R.id.gender_profile_info);
                               mUserPhoto = (TextView) findViewById(R.id.photo_profile_info);

                               String name = (String) eventSnapshot.child("name").getValue();
                               String email = (String) eventSnapshot.child("email").getValue();
                               String age = (String) eventSnapshot.child("age").getValue();
                               String gender = (String) eventSnapshot.child("gender").getValue();
                               String photo = (String) eventSnapshot.child("photo").getValue();

                               mUserEmail.setText(name);
                               mUserName.setText(email);
                               mUserAge.setText(age);
                               mUserGender.setText(gender);
                               mUserPhoto.setText(photo);

                               // TO FIND EVENTS CREATED BY USER:
                               String currentUserId = (String) eventSnapshot.getKey();
                               findCreatedByUserEvents(currentUserId);
                           }
                       }
                   }
                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
    }


    private void findCreatedByUserEvents(String currentUserId){
        final List<Event> userEvents = new ArrayList<>();
        //TO GET EVENTS USER CREATED
        Query createdByUserEventsQuery = mEventRef.orderByChild("creatorId").equalTo(currentUserId);
        createdByUserEventsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String id =  eventSnapshot.getKey();
                        String address = (String) eventSnapshot.child("address").getValue();
                        String date = (String) eventSnapshot.child("dataTime").getValue();
                        String time = (String) eventSnapshot.child("time").getValue();
                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        String details = (String) eventSnapshot.child("details").getValue();
                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        String sportType = (String) eventSnapshot.child("sportType").getValue();

                        Event e1 = new Event(id, sportType, address, date, time, details, peopleNeeded, creatorId);
                        userEvents.add(e1);
                    }
                }
// SET UP LAYOUT FOR SHOWING USE EVENTS:
                recyclerView =  (RecyclerView) findViewById(R.id.recycle_view_events_created_by_user);
                EventAdapter myAdapter = new EventAdapter(getApplicationContext(), userEvents);
                recyclerView.setAdapter(myAdapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
