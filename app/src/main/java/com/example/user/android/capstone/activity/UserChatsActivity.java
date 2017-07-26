package com.example.user.android.capstone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.adapter.UserChatsAdapter;
import com.example.user.android.capstone.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserChatsActivity extends AppCompatActivity {
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUsersRef = mRootRef.child("users");
    List<String> userEventsList;
    List<Event> userEvents;
    RecyclerView recycleView;
    String userID;
    String currentUserEmail;
    ImageView mNewMessageIcon;
    UserChatsAdapter myAdapter;
    TextView eventSportTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);
        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser());
        userEventsList = new ArrayList();
        userEvents = new ArrayList<>();
        mNewMessageIcon = (ImageView) findViewById(R.id.new_message_icon);
        recycleView = (RecyclerView) findViewById(R.id.recycle_view_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setToolbarIconAndTitle(toolbar, "Your current chats");
    }


    private void findUserIdForSignedInUser(final FirebaseUser currentUser) {
        Query findUserQuery = mUsersRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userID = userSnapshot.getKey();
                        currentUserEmail = userSnapshot.child("email").getValue().toString();
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
                    if (userEvents.size() == userEventsList.size()) {
                        listenForNewMessagesInUserChats(userEvents);
                        myAdapter = new UserChatsAdapter(getApplicationContext(), userEvents, UserChatsActivity.class);
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
                                final String messageSentUserEmail = messageSnapsot.child("messageEmail").getValue().toString();

                                Query lastVisitTimeForCurrentChat = mUsersRef.child(userID).child("userEvents").child(eventID);
                                lastVisitTimeForCurrentChat.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long lastVisitTimeForCurrentChat;
                                        lastVisitTimeForCurrentChat = new Date().getTime();
                                        if (dataSnapshot.getValue().toString().equals("true")) {
                                            Date newDate = null;
                                            try {
                                                newDate = new SimpleDateFormat("yyyy-MM-dd").parse("2017-07-21");
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            lastVisitTimeForCurrentChat = newDate.getTime();
                                            mUsersRef.child(userID).child("userEvents").child(eventID).setValue(lastVisitTimeForCurrentChat);
                                        } else {
                                            lastVisitTimeForCurrentChat = Long.parseLong(dataSnapshot.getValue().toString());
                                        }
                                        int position = userEvents.indexOf(event);
                                        View view = recycleView.getLayoutManager().findViewByPosition(position);
                                        if (lastVisitTimeForCurrentChat < messageSentTime &&
                                                !messageSentUserEmail.equals(currentUserEmail)) {
                                            view.findViewById(R.id.new_message_icon).setVisibility(View.VISIBLE);
//                                            final ImageView iv = (ImageView) findViewById(R.id.new_message_icon);
//                                            iv.setOnHoverListener(new View.OnHoverListener() {
//                                                @Override
//                                                public boolean onHover(View view, MotionEvent motionEvent) {
//                                                    System.out.println("IN HOVER!!");
//                                                    Animation rotation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation);
//                                                    rotation.setRepeatCount(Animation.INFINITE);
//                                                    iv.startAnimation(rotation);
//                                                    return false;
//                                                }
//                                            });
                                            eventSportTitleTextView = (TextView) findViewById(R.id.event_sport_title);
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


    private void setToolbarIconAndTitle(Toolbar toolbar, String title) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setTitle(title);
            toolbar.setNavigationIcon(R.drawable.back_arrow_white2);
            Drawable drawable = getResources().getDrawable(R.drawable.back_arrow_white2);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Drawable newdrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 70, 70, true));
            newdrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(newdrawable);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}

