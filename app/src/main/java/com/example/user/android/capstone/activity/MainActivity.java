package com.example.user.android.capstone.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.fragment.EventFragment;
import com.example.user.android.capstone.model.Event;
import com.example.user.android.capstone.model.User;
import com.example.user.android.capstone.utils.MapUtils;
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

import static com.example.user.android.capstone.R.id.imageView;

public class MainActivity extends AppCompatActivity {

    private List<Event> eventsListFromDatabase = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private FloatingActionButton mCreateNewEventButton;
    private RadioButton mAllEventsNewButton;
    private RadioButton mEventOnListButton;
    private RadioButton mFutureEventsNewButton;
    private RadioButton mEventsOnMapButton;
    private FrameLayout fl;
    private String filterByCategory;
    private Spinner spinner;
    private boolean filterEventCategory;
    private boolean filterFutureEvents;
    private boolean listView;
    private EventFragment eventFragment;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView mImageProfileView;
    private TextView mUserNameTextView;
    protected LinearLayout frameLayout;
    private MapUtils mapUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (LinearLayout) findViewById(R.id.main_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mapUtil = new MapUtils(getApplicationContext());
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        initializeTextViewsAndButtons();
        eventFragment = new EventFragment();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        setOnClickListeners(null);
        initListFragment();
        listView = true;
        displayListOfEvents(true, false, listView);
        futureEventsFilter();
        setUpSpinner();
        if (currentUser == null) {
            hideMenuItems();
        } else {
            showMenuItems();
            findUserByEmail(currentUser.getEmail());
        }
    } // end onCreate


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
        eventFragment.updateList(eventsListFromDatabase, false);
    }

    private void futureEventsFilter() {
        mAllEventsNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAllEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
                mFutureEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button);
                filterFutureEvents = false;
                displayListOfEvents(filterFutureEvents, filterEventCategory, listView);
                spinner.setSelection(0);
            }
        });
        mFutureEventsNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFutureEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
                mAllEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button);
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
                displayListOfEvents(filterFutureEvents, filterEventCategory, listView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void setOnClickListeners(final List<Event> eventsList) {
        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Class destinationClass = CreateNewEventActivity.class;
                Intent intentToStartCreateNewEventActivity = new Intent(getApplicationContext(), destinationClass);
                startActivity(intentToStartCreateNewEventActivity);
            }
        });

        mEventOnListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEventOnListButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
                mEventsOnMapButton.setBackgroundResource(R.drawable.corner_radio_button);
                fl.setVisibility(View.VISIBLE);
                listView = true;
            }
        });

        mEventsOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEventOnListButton.setBackgroundResource(R.drawable.corner_radio_button);
                mEventsOnMapButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
                listView = false;
                setUpMap(eventsList);
            }
        });
    }

    private void setUpMap(final List<Event> eventsList) {
        fl.setVisibility(View.GONE);
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        map.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap mMap) {
                mMap.clear();
                if (eventsList != null) {
                    for (Event event : eventsList) {
                        LatLng address = mapUtil.getLocationFromAddress(event.getAddress());
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

    private void initializeTextViewsAndButtons() {
        mAllEventsNewButton = (RadioButton) findViewById(R.id.all_events_button);
        mFutureEventsNewButton = (RadioButton) findViewById(R.id.future_events_button);
        mFutureEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
        mAllEventsNewButton.setBackgroundResource(R.drawable.corner_radio_button);
        mFutureEventsNewButton.setChecked(true);
        filterEventCategory = false;
        filterFutureEvents = true;
        spinner = (Spinner) findViewById(R.id.sport_types_spinner);
        mCreateNewEventButton = (FloatingActionButton) findViewById(R.id.create_event_button);
        mEventsOnMapButton = (RadioButton) findViewById(R.id.events_map_button);
        mEventOnListButton = (RadioButton) findViewById(R.id.events_list_button);
        mEventOnListButton.setBackgroundResource(R.drawable.corner_radio_button_blue);
        mEventsOnMapButton.setBackgroundResource(R.drawable.corner_radio_button);
        mEventOnListButton.setChecked(true);
        fl = (FrameLayout) findViewById(R.id.frameEvents);
    }

// ---------------NAVIGATION:---------------

    private void hideMenuItems() {
        Menu navMenu = nvDrawer.getMenu();
        navMenu.findItem(R.id.nav_profile).setVisible(false);
        navMenu.findItem(R.id.nav_signout).setVisible(false);
        navMenu.findItem(R.id.nav_user_chats).setVisible(false);
        navMenu.findItem(R.id.nav_signin_signup).setVisible(true);
        nvDrawer.getHeaderView(0).setVisibility(View.GONE);
        mCreateNewEventButton.setVisibility(View.GONE);
    }

    private void showMenuItems() {
        Menu navMenu = nvDrawer.getMenu();
        navMenu.findItem(R.id.nav_profile).setVisible(true);
        navMenu.findItem(R.id.nav_signout).setVisible(true);
        navMenu.findItem(R.id.nav_signin_signup).setVisible(false);
        navMenu.findItem(R.id.nav_user_chats).setVisible(true);
        mCreateNewEventButton.setVisibility(View.VISIBLE);
        View headerLayout = nvDrawer.getHeaderView(0);
        headerLayout.setVisibility(View.VISIBLE);
        mImageProfileView = (ImageView) headerLayout.findViewById(imageView);
        mUserNameTextView = (TextView) headerLayout.findViewById(R.id.user_name_textview);

    }

    private void findUserByEmail(String email) {
        Query userProfileQuery = mUsersRef.orderByChild("email").equalTo(email);
        userProfileQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final User user;
                    String currentUserId = "";
                    String name = "";
                    String email = "";
                    String age = "";
                    String gender = "";
                    String photo = "";
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        currentUserId = (String) eventSnapshot.getKey();
                        name = (String) eventSnapshot.child("name").getValue();
                        email = (String) eventSnapshot.child("email").getValue();
                        age = (String) eventSnapshot.child("age").getValue();
                        gender = (String) eventSnapshot.child("gender").getValue();
                        photo = (String) eventSnapshot.child("photo").getValue();
                    }
                    user = new User(currentUserId, email, name, gender, photo, age);
                    String photoUrl = user.getPhoto();
                    mUserNameTextView.setText("Hello, " + user.getName());
                    Glide.with(getApplicationContext()).load(photoUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(mImageProfileView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplication().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mImageProfileView.setImageDrawable(circularBitmapDrawable);
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
            case R.id.nav_profile:
                destinationClass = UserProfileActivity.class;
                break;
            case R.id.nav_signin_signup:
                destinationClass = SignInActivity.class;
                break;
            case R.id.nav_home:
                destinationClass = MainActivity.class;
                break;
            case R.id.nav_signout:
                destinationClass = null;
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "You successfully signed out",
                        Toast.LENGTH_LONG).show();
                hideMenuItems();
                break;
            case R.id.nav_user_chats:
                destinationClass = UserChatsActivity.class;
                break;
            default:
                destinationClass = MainActivity.class;
        }
        if (destinationClass != null) {
            Intent intent = new Intent(getApplicationContext(), destinationClass);
            startActivity(intent);
            setTitle("SportMate");
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


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String queryString = query.toLowerCase();
                List<Event> filteredList = new ArrayList<>();
                for (Event event : eventsListFromDatabase) {
                    if (event.getTitle().toLowerCase().contains(queryString) ||
                            event.getDetails().toLowerCase().contains(queryString) ||
                            event.getAddress().toLowerCase().contains(queryString)) {
                        filteredList.add(event);
                    }
                }
                if (filteredList.size() == 0) {
                    Toast.makeText(MainActivity.this, "Nothing was found", Toast.LENGTH_LONG).show();
                } else {
                    eventFragment.updateList(filteredList, true);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
