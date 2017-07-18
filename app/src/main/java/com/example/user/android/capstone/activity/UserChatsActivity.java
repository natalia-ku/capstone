package com.example.user.android.capstone.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.android.capstone.R;
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
    String userId;
    String userName;
    String userEmail;
    private  ListView listview;
    TextView chatTitleTextView;
    List<String> userEventsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);
      listview = (ListView) findViewById(R.id.user_chats_listview);
        findUserIdForSignedInUser(FirebaseAuth.getInstance().getCurrentUser());
        userEventsList = new ArrayList();
    }



    private void findUserIdForSignedInUser(final FirebaseUser currentUser) {
        Query findUserQuery = mUsersRef.orderByChild("email").equalTo(currentUser.getEmail());
        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userId = eventSnapshot.getKey();
                        userName = eventSnapshot.child("name").getValue().toString();
                        userEmail = eventSnapshot.child("email").getValue().toString();

                        for (DataSnapshot userEvent :  eventSnapshot.child("userEvents").getChildren()) {
                            userEventsList.add(userEvent.getKey().toString());
                        }

                            // Adapter
                            final StableArrayAdapter adapter = new StableArrayAdapter(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, userEventsList);
                            listview.setAdapter(adapter);
                            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, final View view,
                                                        int position, long id) {
                                    final String item = (String) parent.getItemAtPosition(position);
                                    Query eventQuery = mEventsRef.orderByKey().equalTo(item);
                                    eventQuery.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
//
                                                final Event event;
                                                String eventId = "";
                                                String eventCategory = "";
                                                String eventPeopleNeeded = "";
                                                String eventDetails = "";
                                                String eventTitle = "";
                                                String eventAddress = "";
                                                String eventDate = "";
                                                String eventTime = "";
                                                String eventCreatorId = "";
                                                System.out.println("EVENTSNAPSHOT");
                                                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                                                    System.out.println(eventSnapshot);
                                                     eventId = (String) eventSnapshot.getKey().toString();
                                                    System.out.println(eventId);
                                                     eventCategory = (String) eventSnapshot.child("sportCategory").getValue();
                                                    System.out.println(eventCategory);
                                                    eventPeopleNeeded = (String) eventSnapshot.child("peopleNeeded").getValue().toString();
                                                     eventDetails = (String) eventSnapshot.child("details").getValue();
                                                     eventTitle = (String) eventSnapshot.child("title").getValue();
                                                    System.out.println(eventTitle);
                                                    eventAddress = (String) eventSnapshot.child("address").getValue();
                                                     eventDate = (String) eventSnapshot.child("date").getValue();
                                                     eventTime = (String) eventSnapshot.child("time").getValue();
                                                     eventCreatorId = (String) eventSnapshot.child("creatorId").getValue();
                                                }
                                                event = new Event(eventCategory, eventId, eventTitle, eventAddress,
                                                        eventDate, eventTime, eventDetails, eventPeopleNeeded, eventCreatorId);
                                                Intent intentToOpenChat = new Intent(getApplicationContext(), ChatActivity.class);
                                                intentToOpenChat.putExtra("event", (Serializable) event);
                                                startActivity(intentToOpenChat);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }

                            });



                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR");
            }
        });
    }




private class StableArrayAdapter extends ArrayAdapter<String> {

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,
                              List<String> objects) {
        super(context, textViewResourceId, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
}



