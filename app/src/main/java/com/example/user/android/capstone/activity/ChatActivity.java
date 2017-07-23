package com.example.user.android.capstone.activity;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.android.capstone.model.ChatMessage;
import com.example.user.android.capstone.model.Event;
import com.example.user.android.capstone.R;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private String userId;
    private String userName;
    private String userEmail;
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private TextView chatTitleTextView;
    String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatTitleTextView = (TextView) findViewById(R.id.chat_title);
        final Event event = getIntent().getParcelableExtra("event");
        if (event == null){
            eventID = getIntent().getStringExtra("eventID");
            Query findEventQuery = mEventsRef.child(eventID);
            findEventQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Event event = new Event(dataSnapshot.child("sportCategory").getValue().toString(),
                                dataSnapshot.getKey(),
                                dataSnapshot.child("title").getValue().toString(),
                                dataSnapshot.child("address").getValue().toString(),
                                dataSnapshot.child("dataTime").getValue().toString(),
                                dataSnapshot.child("time").getValue().toString(),
                                dataSnapshot.child("details").getValue().toString(),
                                dataSnapshot.child("peopleNeeded").getValue().toString(),
                                dataSnapshot.child("creatorId").getValue().toString());
                        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser(), event);
                        setUpSendMessageTextViewAndButton(event);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else {
            findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser(), event);
            setUpSendMessageTextViewAndButton(event);
        }


    }



    private void displayChatMessages(Event event) {
        final ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, mEventsRef.child(event.getId()).child("chat")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);
                messageText.setText(model.getMessageText());
                if (model.getMessageEmail() != null && model.getMessageEmail().equals(userEmail)) {
                    messageUser.setText("");
                    messageText.setBackground(getResources().getDrawable(R.drawable.bubble_out));
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(500, 0, 0, 0);
                    messageText.setLayoutParams(params);
                } else {
                    messageText.setBackground(getResources().getDrawable(R.drawable.bubble_in));
                    messageUser.setText(model.getMessageUser());
                }
            }
        };
        listOfMessages.setAdapter(adapter);

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listOfMessages.setSelection(adapter.getCount() - 1);
            }
        });
    }


    private void findUserIdForSignedInUser(final FirebaseUser currentUser, final Event event) {
        Query findUserQuery = mUsersRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userId = eventSnapshot.getKey();
                        userName = eventSnapshot.child("name").getValue().toString();
                        userEmail = eventSnapshot.child("email").getValue().toString();
                        Toast.makeText(getApplicationContext(), "Welcome " + userName, Toast.LENGTH_LONG).show();
                        if (event != null) {
                            displayChatMessages(event);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }


    private void   setUpSendMessageTextViewAndButton(final Event event) {
        chatTitleTextView.setText(event.getTitle() + " chat");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);
                DatabaseReference newMessage = mEventsRef.child(event.getId()).child("chat").push();
                newMessage.setValue(new ChatMessage(input.getText().toString(), userName, userEmail));
                input.setText("");
            }
        });
    }

}