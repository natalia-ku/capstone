package com.example.user.android.capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    List<Event> eventsListFromDatabase = new ArrayList<>();
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    Button mCreateNewEventButton;
    Button mUserProfileButton;
    Button mSignInUpButton;
    Button mSignOutMainButton;
    Button mFutureEventsButton;
    Button mAllEventsButton;
    Button mEventsOnMapButton;

    TextView mFilterStatusTextView;
    String filterByCategory;
    Spinner spinner;

    boolean filterEventCategory;
    boolean filterFutureEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAllEventsButton = (Button) findViewById(R.id.all_events_button);
        mAllEventsButton.setVisibility(View.GONE);

        mFilterStatusTextView = (TextView) findViewById(R.id.filter_status);
        mFilterStatusTextView.setText("Select filter");
        filterEventCategory = false;
        filterFutureEvents = false;
        
        spinner = (Spinner) findViewById(R.id.sport_types_spinner);

        mFutureEventsButton = (Button) findViewById(R.id.future_events_button);
        mCreateNewEventButton = (Button) findViewById(R.id.create_event_button);
        mSignInUpButton = (Button) findViewById(R.id.sign_in_up_button);

        mSignOutMainButton = (Button) findViewById(R.id.sign_out_main_button);
        mUserProfileButton = (Button) findViewById(R.id.user_profile_button);
        mEventsOnMapButton = (Button) findViewById(R.id.events_map_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        mSignOutMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "You successfully signed out",
                        Toast.LENGTH_LONG).show();
                updateUI(null);
            }
        });

        mUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class destinationClass = UserProfileActivity.class;
                Intent intentToUserProfileActivity = new Intent(getApplicationContext(), destinationClass);
                startActivity(intentToUserProfileActivity);
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


        mEventsOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putParcelableArrayListExtra("eventList", (ArrayList<? extends Parcelable>) eventsListFromDatabase);
                startActivity(intent);
            }
        });

        // TO DISPLAY ALL EVENTS:
        displayListOfEvents(false, false);
        // SET UP future events filter and back to all events filter:
        futureEventsFilter();
        setUpSpinner();

    } // end onCreate

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            mSignInUpButton.setVisibility(View.VISIBLE);
            mSignOutMainButton.setVisibility(View.GONE);
            mUserProfileButton.setVisibility(View.GONE);
            mCreateNewEventButton.setVisibility(View.GONE);
        } else {
            mSignInUpButton.setVisibility(View.GONE);
            mSignOutMainButton.setVisibility(View.VISIBLE);
            mUserProfileButton.setVisibility(View.VISIBLE);
        }
    }


    private void displayListOfEvents(final boolean onlyFutureEventsFilter, final boolean categoryFilter) {
        mFilterStatusTextView.setText("Select filter");
        StringBuilder statusText = new StringBuilder("Filtered by ");
        if (onlyFutureEventsFilter) {
            statusText.append("future events ");
            mFilterStatusTextView.setText(statusText);
        }
        if (categoryFilter && !filterByCategory.equals("All")) {
            if (onlyFutureEventsFilter) {
                statusText.append("and ");
            }
            statusText.append("category: " + filterByCategory);
            mFilterStatusTextView.setText(statusText);
        }
        eventsListFromDatabase = new ArrayList<>();
        mEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String id = (String) eventSnapshot.getKey();
                    String address = (String) eventSnapshot.child("address").getValue();
                    String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                    String date = (String) eventSnapshot.child("dataTime").getValue();
                    String time = (String) eventSnapshot.child("time").getValue();
                    String details = (String) eventSnapshot.child("details").getValue();
                    String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                    String title = (String) eventSnapshot.child("title").getValue();
                    String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
                    Event e1 = new Event(sportCategory, id, title, address, date, time, details, peopleNeeded, creatorId);
                    eventsListFromDatabase.add(e1);
                }
                if (onlyFutureEventsFilter) {
                    Iterator<Event> iterEvent = eventsListFromDatabase.iterator();
                    while (iterEvent.hasNext()) {
                        Event event = iterEvent.next();
                        if (!checkIfDateInFuture(event.getDate())) {
                            iterEvent.remove();
                        }
                    }
                }
                if (categoryFilter) {
                    Iterator<Event> iterEvent = eventsListFromDatabase.iterator();
                    while (iterEvent.hasNext()) {
                        Event event = iterEvent.next();
                        if (!filterByCategory.equals("All")) {
                            if (!event.getSportCategory().equals(filterByCategory)) {
                                iterEvent.remove();
                            }
                        }
                    }
                }

                recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
                System.out.println("EVENT LIST FORM DATABASE SIZE " + eventsListFromDatabase.size());
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
    }

    private boolean checkIfDateInFuture(String date) {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date eventDate = formatter.parse(date);
            if (eventDate.after(today)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void futureEventsFilter() {
        mFutureEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterFutureEvents = true;
                displayListOfEvents(filterFutureEvents, filterEventCategory);
                mFutureEventsButton.setVisibility(View.GONE);
                mAllEventsButton.setVisibility(View.VISIBLE);
                spinner.setSelection(0);
            }
        });
        // Back to all events button:
        mAllEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterEventCategory = false;
                filterFutureEvents = false;
                displayListOfEvents(filterFutureEvents, filterEventCategory);
                mFutureEventsButton.setVisibility(View.VISIBLE);
                mAllEventsButton.setVisibility(View.GONE);
                spinner.setSelection(0);
            }
        });
    }


    private void setUpSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sport_types_all_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filterByCategory = (String) adapterView.getItemAtPosition(i);
                filterEventCategory = true;
                displayListOfEvents(filterFutureEvents, filterEventCategory);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
}
