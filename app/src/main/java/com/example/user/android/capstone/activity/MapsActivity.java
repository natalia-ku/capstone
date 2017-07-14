package com.example.user.android.capstone.activity;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.activity.EventInfoActivity;
import com.example.user.android.capstone.model.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // in onCreateView in MapFragment
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (getIntent().getParcelableArrayListExtra("eventList") != null) { // get List from Intent
            List<Event> eventsList;
            eventsList = getIntent().getParcelableArrayListExtra("eventList");
            for (Event event : eventsList) {
                LatLng address = getLocationFromAddress(event.getAddress());
                if (address != null) {
                    mMap.addMarker(new MarkerOptions().position(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            .title(event.getTitle()));
                }
            }
        }
        else{ // get Event from intent
            Event event;
            event = getIntent().getParcelableExtra("event");
            LatLng address = getLocationFromAddress(event.getAddress());
            if (address != null) {
                mMap.addMarker(new MarkerOptions().position(address)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .title(event.getTitle()));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6101, -122.2015),
                Math.max(10, mMap.getCameraPosition().zoom)));

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    public LatLng getLocationFromAddress(String strAddress) {
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


    @Override
    public void onInfoWindowClick(Marker marker) {
        System.out.println("IN INTENT!!");
        String markerTitle = marker.getTitle();
        Query findEventByTitleQuery = mEventsRef.orderByChild("title").equalTo(markerTitle);
        findEventByTitleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String eventId = "";
                    String address = "";
                    String creatorId = "";
                    String date = "";
                    String time = "";
                    String details = "";
                    String peopleNeeded = "";
                    String title = "";
                    String sportCategory = "";
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventId = eventSnapshot.getKey();
                        address = (String) eventSnapshot.child("address").getValue();
                        creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        date = (String) eventSnapshot.child("dataTime").getValue();
                        time = (String) eventSnapshot.child("time").getValue();
                        details = (String) eventSnapshot.child("details").getValue();
                        peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
                        title = (String) eventSnapshot.child("title").getValue();
                    }
                    Event event = new Event(sportCategory, eventId, title, address, date, time, details, peopleNeeded, creatorId);
                    if (!eventId.equals("")) {

                        Intent intentToGetEventDetailsActivity = new Intent(getApplicationContext(), EventInfoActivity.class);
                        intentToGetEventDetailsActivity.putExtra("event", (Parcelable) event);
                        startActivity(intentToGetEventDetailsActivity);
                    } else {
                        System.out.println("Error: cannot find event");
                    }
                } else {
                    System.out.println("Error: Nothing found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
