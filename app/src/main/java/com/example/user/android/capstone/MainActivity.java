package com.example.user.android.capstone;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView mEventsListTextView;
    private RecyclerView recyclerView;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    Button mCreateNewEventButton;

    Button mSignInUpButton;

    public static Button mSignOutMainButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mEventsListTextView = (TextView) findViewById(R.id.events_list);
        mCreateNewEventButton = (Button) findViewById(R.id.create_event_button);
        mSignInUpButton = (Button) findViewById(R.id.sign_in_up_button);

        mSignOutMainButton = (Button) findViewById(R.id.sign_out_main_button);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            mSignInUpButton.setVisibility(View.VISIBLE);
            mSignOutMainButton.setVisibility(View.GONE);
        }
        else {
            mSignInUpButton.setVisibility(View.GONE);
            mSignOutMainButton.setVisibility(View.VISIBLE);
        }


        mSignOutMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    mAuth.signOut();
                Toast.makeText(MainActivity.this, "You successfully signed out",
                        Toast.LENGTH_LONG).show();
                mSignInUpButton.setVisibility(View.VISIBLE);
                mSignOutMainButton.setVisibility(View.GONE);
            }
        });



        mEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Event> eventsListFromDatabase = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String id = (String) eventSnapshot.getKey();
                    String address = (String) eventSnapshot.child("address").getValue();
                    String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                    String dataTime = (String) eventSnapshot.child("dataTime").getValue();
                    String details = (String) eventSnapshot.child("details").getValue();
                    String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                    String sportType = (String) eventSnapshot.child("sportType").getValue();
                    Event e1 = new Event(id, sportType, address, dataTime, details, peopleNeeded, creatorId);
                    eventsListFromDatabase.add(e1);
                }

                recyclerView =  (RecyclerView) findViewById(R.id.recycle_view);
                EventAdapter myAdapter = new EventAdapter(getApplicationContext(), eventsListFromDatabase);
                recyclerView.setAdapter(myAdapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });


        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Class destinationClass = CreateNewEventActivity.class;
                Intent intentToStartCreateNewEventActivity = new Intent(getApplicationContext(), destinationClass);
                startActivity(intentToStartCreateNewEventActivity);
            }
        });

        mSignInUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Class destinationClass = SignUpActivity.class;
                Intent intentToStartCreateNewEventActivity = new Intent(getApplicationContext(), destinationClass);
                startActivity(intentToStartCreateNewEventActivity);
            }
        });

    }


}
