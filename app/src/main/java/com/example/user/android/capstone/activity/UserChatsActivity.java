package com.example.user.android.capstone.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.adapter.EventAdapter;
import com.example.user.android.capstone.model.ChatMessage;
import com.example.user.android.capstone.model.Event;
import com.example.user.android.capstone.model.User;
import com.firebase.ui.database.FirebaseListAdapter;
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
import java.util.HashMap;
import java.util.List;

public class UserChatsActivity extends AppCompatActivity {
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUsersRef = mRootRef.child("users");
    List<String> userEventsList;
    List<Event> userEvents;
    RecyclerView recycleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);
        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser());
        userEventsList = new ArrayList();
        userEvents = new ArrayList<>();
        recycleView = (RecyclerView) findViewById(R.id.recycle_view_chat_list);
    }

    private void findUserIdForSignedInUser(final FirebaseUser currentUser) {
        Query findUserQuery = mUsersRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot userEvent : eventSnapshot.child("userEvents").getChildren()) {
                            userEventsList.add(userEvent.getKey().toString());
                        }
                        setUpAdapterForUserList(userEventsList);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }


    private void setUpAdapterForUserList(List<String> userEventsList) {
        for (final String userEventKey : userEventsList) {
            Query eventQuery = mEventsRef.orderByKey().equalTo(userEventKey);
            eventQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String eventId = "";
                        String eventCategory = "";
                        String eventPeopleNeeded = "";
                        String eventDetails = "";
                        String eventTitle = "";
                        String eventAddress = "";
                        String eventDate = "";
                        String eventTime = "";
                        String eventCreatorId = "";
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            eventId = (String) eventSnapshot.getKey().toString();
                            eventCategory = (String) eventSnapshot.child("sportCategory").getValue();
                            eventPeopleNeeded = (String) eventSnapshot.child("peopleNeeded").getValue().toString();
                            eventDetails = (String) eventSnapshot.child("details").getValue();
                            eventTitle = (String) eventSnapshot.child("title").getValue();
                            eventAddress = (String) eventSnapshot.child("address").getValue();
                            eventDate = (String) eventSnapshot.child("date").getValue();
                            eventTime = (String) eventSnapshot.child("time").getValue();
                            eventCreatorId = (String) eventSnapshot.child("creatorId").getValue();
                            Event event = new Event(eventCategory, eventId, eventTitle, eventAddress,
                                    eventDate, eventTime, eventDetails, eventPeopleNeeded, eventCreatorId);
                            userEvents.add(event);

                        }
                        EventAdapter myAdapter = new EventAdapter(getApplicationContext(),userEvents, UserChatsActivity.class);
                        recycleView.setAdapter(myAdapter);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recycleView.setLayoutManager(layoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

    }


}

