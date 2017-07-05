package com.example.user.android.capstone;

import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<Event> eventsList;
        eventsList = getIntent().getParcelableArrayListExtra("eventList");
        System.out.println(eventsList);
        for (Event event:eventsList){
            String addressString =  event.getAddress();
            LatLng address = getLocationFromAddress(addressString);
            if (address != null) {
                mMap.addMarker(new MarkerOptions().position(address).title(event.getSportType()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(address));
            }
        }


    }


    public LatLng getLocationFromAddress(String strAddress){
        Geocoder coder = new Geocoder(this);
        List<android.location.Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress,5);
            System.out.println("ADDRESS");
            System.out.println(address);
            if (address==null || address.size() == 0 ) {
                return null;
            }
            double latit = address.get(0).getLatitude();
            double longit =address.get(0).getLongitude();
            p1 = new LatLng(latit, longit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p1;
    }


}
