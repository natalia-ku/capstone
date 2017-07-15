package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.fragment.EventFragment;
import com.example.user.android.capstone.model.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    List<Event> eventsListFromDatabase = new ArrayList<>();
    private FirebaseAuth mAuth;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    FloatingActionButton mCreateNewEventButton;
    Button mUserProfileButton;
    Button mSignInUpButton;
    Button mSignOutMainButton;
    RadioButton mAllEventsNewButton;
    RadioButton mFutureEventsNewButton;
    RadioButton mEventsOnMapButton;
    RadioButton mEventOnListButton;
    FrameLayout fl;
    String filterByCategory;
    Spinner spinner;
    boolean filterEventCategory;
    boolean filterFutureEvents;
    boolean listView;
    EventFragment eventFragment;


    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);


        initializeTextViewsAndButtons();
        eventFragment = new EventFragment();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        setOnClickListeners(null);
        initListFragment();
        listView = true;
        displayListOfEvents(true, false, listView);
        futureEventsFilter();
        setUpSpinner();
        if (currentUser == null) {
            hideItem();
        }
    } // end onCreate

    private void hideItem() {
        Menu navMenu = nvDrawer.getMenu();
        navMenu.findItem(R.id.nav_first_fragment).setVisible(false);
        navMenu.findItem(R.id.nav_fourth_fragment).setVisible(false);
    }

    private void showItem() {
        Menu navMenu = nvDrawer.getMenu();
        navMenu.findItem(R.id.nav_first_fragment).setVisible(true);
        navMenu.findItem(R.id.nav_fourth_fragment).setVisible(true);
        navMenu.findItem(R.id.nav_second_fragment).setVisible(false);
    }


    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Class destinationClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                destinationClass = UserProfileActivity.class;
                break;
            case R.id.nav_second_fragment:
                destinationClass = SignUpActivity.class;
                break;
            case R.id.nav_third_fragment:
                destinationClass = MainActivity.class;
                break;
            case R.id.nav_fourth_fragment:
                destinationClass = null;
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "You successfully signed out",
                        Toast.LENGTH_LONG).show();
                updateUI(null);
                break;
            default:
                destinationClass = MainActivity.class;
        }
        if (destinationClass != null) {
            Intent intent = new Intent(getApplicationContext(), destinationClass);
            startActivity(intent);
            menuItem.setChecked(true);
            setTitle(menuItem.getTitle());
            mDrawer.closeDrawers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayListOfEvents(final boolean onlyFutureEventsFilter, final boolean categoryFilter, final boolean listView) {
        eventsListFromDatabase = new ArrayList<>();
        mEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event e1 = createEventFromSnapshot(eventSnapshot);
                    eventsListFromDatabase.add(e1);
                }
                if (onlyFutureEventsFilter) {
                    Iterator<Event> iterEvent = eventsListFromDatabase.iterator();
                    while (iterEvent.hasNext()) {
                        Event event = iterEvent.next();
                        if (!event.checkIfDateInFuture(event.getDate())) {
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
                setOnClickListeners(eventsListFromDatabase);


                if (listView) {
                    initListFragment();
                    updateFragment();
                } else {
                    setUpMap(eventsListFromDatabase);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    private void initListFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameEvents, eventFragment);
        if (isDestroyed()) {
            return;
        }
        ft.commitAllowingStateLoss();
    }

    private void updateFragment() {
        eventFragment.updateList(eventsListFromDatabase);
    }

    private void futureEventsFilter() {
        mAllEventsNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterFutureEvents = false;
                displayListOfEvents(filterFutureEvents, filterEventCategory, listView);
                spinner.setSelection(0);
            }
        });
        // Back to future events button:
        mFutureEventsNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterFutureEvents = true;
                displayListOfEvents(filterFutureEvents, filterEventCategory, listView);
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
                System.out.println("FILTER FUTURE EVENTS:" + filterFutureEvents);
                displayListOfEvents(filterFutureEvents, filterEventCategory, listView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void setOnClickListeners(final List<Event> eventsList) {
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

        mEventOnListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl.setVisibility(View.VISIBLE);
                listView = true;
            }
        });


        mEventsOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView = false;
                setUpMap(eventsList);
            }
        });
    }

    private void setUpMap(final List<Event> eventsList) {
//        mEventsOnMapButton.setVisibility(View.GONE);
//        mEventOnListButton.setVisibility(View.VISIBLE);
        fl.setVisibility(View.GONE);
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        map.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap mMap) {
                mMap.clear();
                if (eventsList != null) {
                    for (Event event : eventsList) {
                        LatLng address = getLocationFromAddress(event.getAddress());
                        if (address != null) {
                            mMap.addMarker(new MarkerOptions().position(address)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                    .title(event.getTitle()));
                        }
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6101, -122.2015),
                            Math.max(10, mMap.getCameraPosition().zoom)));

                }
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return false;
                    }
                });
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        String markerTitle = marker.getTitle();
                        Query findEventByTitleQuery = mEventsRef.orderByChild("title").equalTo(markerTitle);
                        findEventByTitleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Event event = null;
                                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                        event = createEventFromSnapshot(eventSnapshot);
                                    }
                                    if (!event.getId().equals("")) {
                                        Intent intentToGetEventDetailsActivity = new Intent(getApplicationContext(), EventInfoActivity.class);
                                        intentToGetEventDetailsActivity.putExtra("event", (Parcelable) event);
                                        startActivity(intentToGetEventDetailsActivity);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                });

            }
        });
    }

    private Event createEventFromSnapshot(DataSnapshot eventSnapshot) {
        String eventId = "";
        String address = "";
        String creatorId = "";
        String date = "";
        String time = "";
        String details = "";
        String peopleNeeded = "";
        String title = "";
        String sportCategory = "";
        eventId = eventSnapshot.getKey();
        address = (String) eventSnapshot.child("address").getValue();
        date = (String) eventSnapshot.child("dataTime").getValue();
        time = (String) eventSnapshot.child("time").getValue();
        details = (String) eventSnapshot.child("details").getValue();
        peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
        sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
        title = (String) eventSnapshot.child("title").getValue();
        creatorId = eventSnapshot.child("creatorId").getValue().toString();

        Event event = new Event(sportCategory, eventId, title, address, date, time, details, peopleNeeded, creatorId);
        return event;
    }

    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<android.location.Address> address;
        LatLng p1 = null;
        try {
            if (strAddress != null) {
                address = coder.getFromLocationName(strAddress, 5);
                if (address == null || address.size() == 0) {
                    return null;
                }
                double latit = address.get(0).getLatitude();
                double longit = address.get(0).getLongitude();
                p1 = new LatLng(latit, longit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }

    private void initializeTextViewsAndButtons() {
        mAllEventsNewButton = (RadioButton) findViewById(R.id.all_events_button);
        mFutureEventsNewButton = (RadioButton) findViewById(R.id.future_events_button);
        mFutureEventsNewButton.setChecked(true);

        filterEventCategory = false;
        filterFutureEvents = true;
        spinner = (Spinner) findViewById(R.id.sport_types_spinner);
        mCreateNewEventButton = (FloatingActionButton) findViewById(R.id.create_event_button);
        mSignInUpButton = (Button) findViewById(R.id.sign_in_up_button);
        mSignOutMainButton = (Button) findViewById(R.id.sign_out_main_button);
        mUserProfileButton = (Button) findViewById(R.id.user_profile_button);
        mEventsOnMapButton = (RadioButton) findViewById(R.id.events_map_button);
        mEventOnListButton = (RadioButton) findViewById(R.id.events_list_button);
        mEventOnListButton.setChecked(true);
        fl = (FrameLayout) findViewById(R.id.frameEvents);

    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            mSignInUpButton.setVisibility(View.VISIBLE);
            mSignOutMainButton.setVisibility(View.GONE);
            mUserProfileButton.setVisibility(View.GONE);
            mCreateNewEventButton.setVisibility(View.GONE);
            hideItem();
        } else {
            mSignInUpButton.setVisibility(View.GONE);
            mSignOutMainButton.setVisibility(View.VISIBLE);
            mUserProfileButton.setVisibility(View.VISIBLE);
        }
    }

}
