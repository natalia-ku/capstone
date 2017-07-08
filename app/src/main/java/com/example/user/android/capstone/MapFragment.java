//package com.example.user.android.capstone;
//
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.content.Intent;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//
//import java.io.IOException;
//import java.util.List;
//
///**
// * Created by nataliakuleniuk on 7/7/17.
// */
//
//public class MapFragment extends Fragment implements OnMapReadyCallback,  GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
//
//    private GoogleMap mMap;
//    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
//    DatabaseReference mEventsRef = mRootRef.child("events");
//MapView mapView;
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.activity_main, container, false);
//        SupportMapFragment mapFragment = new SupportMapFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.mapView, mapFragment).commit();
//        mapFragment.getMapAsync(this);
//        return view;
//    }
//
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        List<Event> eventsList;
//        eventsList = getIntent().getParcelableArrayListExtra("eventList");
//        for (Event event : eventsList) {
//            String addressString = event.getAddress();
//            LatLng address = getLocationFromAddress(addressString);
//            if (address != null) {
//                mMap.addMarker(new MarkerOptions().position(address)
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
//                        .title(event.getSportType()));
//            }
//        }
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6101, -122.2015),
//                Math.max(10, mMap.getCameraPosition().zoom)));
//
//        mMap.setOnMarkerClickListener(this);
//        mMap.setOnInfoWindowClickListener(this);
//
//
//    }
//
//    @Override
//    public boolean onMarkerClick(final Marker marker) {
//        return false;
//    }
//
//    public LatLng getLocationFromAddress(String strAddress) {
//        Geocoder coder = new Geocoder(this);
//        List<android.location.Address> address;
//        LatLng p1 = null;
//        try {
//            address = coder.getFromLocationName(strAddress, 5);
//            if (address == null || address.size() == 0) {
//                return null;
//            }
//            double latit = address.get(0).getLatitude();
//            double longit = address.get(0).getLongitude();
//            p1 = new LatLng(latit, longit);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return p1;
//    }
//
//
//    @Override
//    public void onInfoWindowClick(Marker marker) {
//        String markerTitle = marker.getTitle();
//        Query findEventByTitleQuery = mEventsRef.orderByChild("sportType").equalTo(markerTitle);
//        findEventByTitleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String eventId = "";
//                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
//                        eventId = eventSnapshot.getKey();
//                    }
//                    if (!eventId.equals("")) {
//                        Intent intentToGetEventDetailsActivity = new Intent(getApplicationContext(), EventInfoActivity.class);
//                        intentToGetEventDetailsActivity.putExtra("id", eventId);
//                        startActivity(intentToGetEventDetailsActivity);
//                    } else {
//                        System.out.println("Error: cannot find event");
//                    }
//                } else {
//                    System.out.println("Error: Nothing found");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//
//
//
//}