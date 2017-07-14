package com.example.user.android.capstone.fragment;


import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.activity.EventInfoActivity;
import com.example.user.android.capstone.fragment.EventsFragmentInterface;
import com.example.user.android.capstone.model.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, EventsFragmentInterface, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    MapView mapView;

    GoogleMap mMap;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");

    public MapsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("MAPS ON CREATE VIEW");

        View view = inflater.inflate(R.layout.fragment_maps, container, false);



//        mapView = (MapView) view.findViewById(R.id.map_view);
//        mapView.getMapAsync(this);
       // updateList(new ArrayList<Event>());
        return view;
    }

        @Override
    public void onMapReady(GoogleMap googleMap) {
            System.out.println("ON MAP READY");
            mMap = googleMap;

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6101, -122.2015),
                    Math.max(10, mMap.getCameraPosition().zoom)));


        }


    @Override
    public void updateList(final List<Event> events) {
        System.out.println("MAPS UPDATE LIST");


        for (Event event : events) {
            LatLng address = getLocationFromAddress(event.getAddress());
            if (address != null) {
//                mMap.addMarker(new MarkerOptions().position(address)
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
//                        .title(event.getTitle()));
            }
        }



        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }


    public  void callMap(GoogleMap googleMap, List<Event> eventsList) {
        mMap = googleMap;
        System.out.println("MAPS CALL MAPS");
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

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getContext());
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
                        Intent intentToGetEventDetailsActivity = new Intent(getContext(), EventInfoActivity.class);
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
