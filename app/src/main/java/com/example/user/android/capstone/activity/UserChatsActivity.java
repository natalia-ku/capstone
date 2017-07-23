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
import java.util.Date;
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
    long lastVisitTime;
    EventAdapter myAdapter;

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
            System.out.println("key" + userEventKey);
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
                            System.out.println("user events size:  " +userEvents.size());
//                            Set<Event> events = new HashSet<Event>();
//                            events.addAll(userEvents);
//                            userEvents.clear();
//                            userEvents.addAll(events);

//                            List<Event> temp = new ArrayList<Event>();
//                            for (int i = 0; i <userEvents.size(); i++){
//                                if(!temp.contains(userEvents.get(i))) {
//                                    temp.add(userEvents.get(i));
//                                }
//                            }
//                            System.out.println("TEMP LIST: " );
//                            for (Event e : temp){
//                                System.out.println("Event in Temp : " + e);
//                            }
//                            userEvents = temp;
                        }
                    }
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
//            final Event event = userEvents.get(i);
//            final int ii = i;
            String eventID = event.getId();
            Query eventChat = mEventsRef.child(eventID).child("chat").limitToLast(1);
            eventChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        System.out.println("in listen for new: " + event.getTitle());
                        System.out.println("_______________ NEW CHAT __________________");
                        System.out.println(dataSnapshot);
                        for (final DataSnapshot messageSnapsot : dataSnapshot.getChildren()) {
                            if (messageSnapsot.child("messageUser").getValue() != null &&
                                    messageSnapsot.child("messageTime").getValue() != null &&
                                    messageSnapsot.child("messageText").getValue() != null) {

                                final long messageSentTime = Long.parseLong(messageSnapsot.child("messageTime").getValue().toString());
                                Query lastVisitTimeQuery = mUsersRef.child(userID).child("lastTimeVisitedChats");

                                lastVisitTimeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long lastVisitTime = Long.parseLong(dataSnapshot.getValue().toString());
                                        if (lastVisitTime < messageSentTime) {
                                            System.out.println("*********");
                                            System.out.println("MESSAGE: " + messageSnapsot.child("messageText").getValue().toString());
                                            System.out.println(lastVisitTime + "  " + messageSentTime);
                                            System.out.println(" YOU HAVE NEW UNOPENED MESSAGE IN CHAT!!");

                                            int position = userEvents.indexOf(event);
                                            System.out.println(position);
//                                            myAdapter.notifyDataSetChanged();

                                            View view = recycleView.getLayoutManager().findViewByPosition(position);

                                            view.setBackgroundColor(getResources().getColor(R.color.accent));
                                        }
                                        if (event.equals(userEvents.get(userEvents.size() - 1))) {
                                            lastVisitTime = new Date().getTime();
                                            mUsersRef.child(userID).child("lastTimeVisitedChats").setValue(lastVisitTime);

                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
//
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

}

