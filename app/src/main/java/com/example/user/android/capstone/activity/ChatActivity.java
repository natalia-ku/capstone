package com.example.user.android.capstone.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUsersRef = mRootRef.child("users");
    String userId;
    String userName;
    String userEmail;
    TextView chatTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Event event = getIntent().getParcelableExtra("event");

        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser(), event);
        chatTitleTextView = (TextView) findViewById(R.id.chat_title);
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

    private void displayChatMessages(Event event) {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
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
                    messageUser.setTextColor(Color.parseColor("#0B93BF"));
                    messageText.setBackground(getResources().getDrawable(R.drawable.bubble_out));
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(770, 0, 0, 0);
                    messageText.setLayoutParams(params);
                } else {
                    messageText.setBackground(getResources().getDrawable(R.drawable.bubble_in));
                    messageUser.setText(model.getMessageUser());
                }
//                messageUser.setText(model.getMessageUser());
                // messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                //       model.getMessageTime()));
            }
        };

        listenForNewMessages(event);
        listOfMessages.setAdapter(adapter);
    }

    private void listenForNewMessages(final Event event) {
        Query allMessagesInChatQuery = mEventsRef.child(event.getId()).child("chat");
        allMessagesInChatQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("NEW MESSAGE IS ADDED!!!!!!!!");
                Query eventAttendeesQuery = mEventsRef.child(event.getId()).child("attendees");
                eventAttendeesQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userIdInAttendeesList = userSnapshot.getKey().toString();
                        if (userIdInAttendeesList.equals(userId)){
                            System.out.println("NEW MESSAGE FOR CURRENT USER!!!");
                        }


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("CHANGED!!!!!!!");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
}
