package com.example.user.android.capstone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserChatsActivity extends AppCompatActivity {
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUsersRef = mRootRef.child("users");
    List<String> userEventsList;
    List<Event> userEvents;
    RecyclerView recycleView;
    String userID;
    ImageView mNewMessageIcon;
    EventAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);
        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser());
        userEventsList = new ArrayList();
        userEvents = new ArrayList<>();
        mNewMessageIcon = (ImageView) findViewById(R.id.new_message_icon);
        recycleView = (RecyclerView) findViewById(R.id.recycle_view_chat_list);

    }


    private void findUserIdForSignedInUser(final FirebaseUser currentUser) {
        Query findUserQuery = mUsersRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userID = userSnapshot.getKey();
                        for (DataSnapshot userEvent : userSnapshot.child("userEvents").getChildren()) {
                            userEventsList.add(userEvent.getKey().toString());
                        }
                    }
                    setUpAdapterForUserList(userEventsList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }


    private void setUpAdapterForUserList(final List<String> userEventsList) {
        for (final String userEventKey : userEventsList) {
            Query eventQuery = mEventsRef.orderByKey().equalTo(userEventKey);
            eventQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            Event event = new Event((String) eventSnapshot.child("sportCategory").getValue(),
                                    eventSnapshot.getKey().toString(),
                                    (String) eventSnapshot.child("title").getValue(),
                                    (String) eventSnapshot.child("address").getValue(),
                                    (String) eventSnapshot.child("date").getValue(),
                                    (String) eventSnapshot.child("time").getValue(),
                                    (String) eventSnapshot.child("details").getValue(),
                                    eventSnapshot.child("peopleNeeded").getValue().toString(),
                                    (String) eventSnapshot.child("creatorId").getValue());
                            userEvents.add(event);
                        }
                    }
                    System.out.println(userEvents.size());
                    System.out.println(userEventsList.size());
                    if (userEvents.size() == userEventsList.size()) {
                        listenForNewMessagesInUserChats(userEvents);
                        myAdapter = new EventAdapter(getApplicationContext(), userEvents, UserChatsActivity.class);
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


    private void listenForNewMessagesInUserChats(final List<Event> userEvents) {
        for (final Event event : userEvents) {
            final String eventID = event.getId();
            Query eventChat = mEventsRef.child(eventID).child("chat").limitToLast(1);
            eventChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (final DataSnapshot messageSnapsot : dataSnapshot.getChildren()) {
                            if (messageSnapsot.child("messageUser").getValue() != null &&
                                    messageSnapsot.child("messageTime").getValue() != null &&
                                    messageSnapsot.child("messageText").getValue() != null) {

                                final long messageSentTime = Long.parseLong(messageSnapsot.child("messageTime").getValue().toString());

                                Query lastVisitTimeForCurrentChat = mUsersRef.child(userID).child("userEvents").child(eventID);
                                lastVisitTimeForCurrentChat.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long lastVisitTimeForCurrentChat;
                                        lastVisitTimeForCurrentChat = new Date().getTime();
                                        if (dataSnapshot.getValue().toString().equals("true")){
                                            Date newDate = null;
                                            try {
                                                newDate = new SimpleDateFormat("yyyy-MM-dd").parse("2017-07-21");
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            lastVisitTimeForCurrentChat = newDate.getTime();
                                            mUsersRef.child(userID).child("userEvents").child(eventID).setValue(lastVisitTimeForCurrentChat);
                                        }
                                        else {
                                             lastVisitTimeForCurrentChat = Long.parseLong(dataSnapshot.getValue().toString());
                                        }
                                        int position = userEvents.indexOf(event);
                                        View view = recycleView.getLayoutManager().findViewByPosition(position);
                                        if (lastVisitTimeForCurrentChat < messageSentTime) {
                                            view.findViewById(R.id.new_message_icon).setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}

