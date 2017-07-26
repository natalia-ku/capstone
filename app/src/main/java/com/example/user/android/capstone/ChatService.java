package com.example.user.android.capstone;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.user.android.capstone.activity.ChatActivity;
import com.example.user.android.capstone.activity.MainActivity;
import com.example.user.android.capstone.model.ChatMessage;
import com.example.user.android.capstone.model.Event;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Random;


public class ChatService extends IntentService {
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mEventsRef = mRootRef.child("events");
    String currentUserEmail;
    private final String TAG = "myLogs";

    public ChatService() {
        super("ChatService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            currentUserEmail = intent.getStringExtra("currentUserEmail");
            findAndlistenToUserChats(currentUserEmail);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy service");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.putExtra("currentUserEmail", currentUserEmail);
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI);

    }

    private void findAndlistenToUserChats(String currentUserEmail) {
        Query userQuery = mUsersRef.orderByChild("email").equalTo(currentUserEmail);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        final String userID = userSnapshot.getKey();
                        for (DataSnapshot userEvent : userSnapshot.child("userEvents").getChildren()) {
                            String userEventId = userEvent.getKey().toString();
                            listenForNewMessages(userEventId, userID);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void listenForNewMessages(final String eventId, final String userID) {
        final Query allMessagesInChatQuery = mEventsRef.child(eventId).child("chat").limitToLast(1);
        allMessagesInChatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot messageSnapsot : dataSnapshot.getChildren()) {
                        if (messageSnapsot.child("messageUser").getValue() != null &&
                                messageSnapsot.child("messageTime").getValue() != null &&
                                messageSnapsot.child("messageText").getValue() != null &&
                                messageSnapsot.child("messageEmail").getValue() != null) {
                            final ChatMessage chatMessage = new ChatMessage(
                                    messageSnapsot.child("messageText").getValue().toString(),
                                    messageSnapsot.child("messageUser").getValue().toString(),
                                    Long.parseLong(messageSnapsot.child("messageTime").getValue().toString()),
                                    messageSnapsot.child("messageEmail").getValue().toString(),
                                    eventId);
                            final long messageSentTime = chatMessage.getMessageTime();
                            final String messageSentUserEmail = chatMessage.getMessageEmail();
                            Query lastVisitTimeForCurrentChat = mUsersRef.child(userID).child("userEvents").child(eventId);
                            lastVisitTimeForCurrentChat.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long lastVisitTimeForCurrentChat = Long.parseLong(dataSnapshot.getValue().toString());
                                    if (lastVisitTimeForCurrentChat < messageSentTime &&
                                            !messageSentUserEmail.equals(currentUserEmail)) {
                                        showNotification(chatMessage);
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


    private void showNotification(ChatMessage chatMessage) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(chatMessage.getMessageUser() + " " + " sent you a message: ")
                        .setColor(Color.parseColor("#EE5622"))
                        .setContentText(chatMessage.getMessageText());
        Intent resultIntent = new Intent(this, ChatActivity.class);
        resultIntent.putExtra("eventID", chatMessage.getEventId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this.getApplicationContext(),
                (int) (Math.random() * 100), resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(chatMessage.getEventId().hashCode(), mBuilder.build());
    }
}
