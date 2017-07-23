package com.example.user.android.capstone;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ChatService extends IntentService {
    private ChatMessage chatMessage;
    private FirebaseListAdapter<ChatMessage> adapter;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private String userId;
    private String userName;
    private String userEmail;
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private TextView chatTitleTextView;
    int messagesCount;
    String currentUserEmail;

    public ChatService() {
        super("ChatService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            currentUserEmail = intent.getStringExtra("currentUserEmail");
            System.out.println(currentUserEmail);
            findAndlistenToUserChats(currentUserEmail);
        }
    }


    private void findAndlistenToUserChats(String currentUserEmail) {
        Query userQuery = mUsersRef.orderByChild("email").equalTo(currentUserEmail);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot userEvent : eventSnapshot.child("userEvents").getChildren()) {
                            String userEventId = userEvent.getKey().toString();
                            listenForNewMessages( userEventId);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void listenForNewMessages(final String eventId) {
        final Query allMessagesInChatQuery = mEventsRef.child(eventId).child("chat").limitToLast(1);
        allMessagesInChatQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot messageSnapsot : dataSnapshot.getChildren() ){
                        if ( messageSnapsot.child("messageUser").getValue() != null &&
                                messageSnapsot.child("messageTime").getValue() != null &&
                                messageSnapsot.child("messageText").getValue() != null) {
                            ChatMessage chatMessage = new ChatMessage(messageSnapsot.child("messageText").getValue().toString(),
                                    messageSnapsot.child("messageUser").getValue().toString(),
                                    messageSnapsot.child("messageTime").getValue().toString(), eventId);
                        showNotification(chatMessage);
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
                        .setContentTitle(chatMessage.getMessageUser())
                        .setContentText(chatMessage.getMessageText());

        Intent resultIntent = new Intent(this, ChatActivity.class);
        resultIntent.putExtra("eventID", chatMessage.getEventId());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ChatActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =  PendingIntent.getActivity(this.getApplicationContext(),
                (int)(Math.random() * 100), resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(chatMessage.getEventId().hashCode(), mBuilder.build());
//
    }


}
