package com.example.user.android.capstone.activity;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private String userId;
    private String userName;
    private String userEmail;
    private DatabaseReference mEventsRef = mRootRef.child("events");
    String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Event event = getIntent().getParcelableExtra("event");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (event != null) {
            setToolbarIconAndTitle(toolbar, event.getTitle());
        }
        if (event == null) {
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
        } else {
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
                final TextView messageText = (TextView) v.findViewById(R.id.message_text);
                final TextView messageUser = (TextView) v.findViewById(R.id.message_user);
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
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userId = userSnapshot.getKey();
                        userName = userSnapshot.child("name").getValue().toString();
                        userEmail = userSnapshot.child("email").getValue().toString();
                        if (event != null) {
                            displayChatMessages(event);
                        }
                    }
                    long lastVisitedChatTime = new Date().getTime();
                    mUsersRef.child(userId).child("userEvents").child(event.getId()).setValue(lastVisitedChatTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }

    @Override
    public void onBackPressed() {
        System.out.println("ON BACK PRESSED");
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), UserChatsActivity.class);
        startActivity(intent);
    }

    private void setToolbarIconAndTitle(Toolbar toolbar, String title) {
        if (toolbar != null) {
            System.out.println("TITLE" + title);
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


    private void setUpSendMessageTextViewAndButton(final Event event) {
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