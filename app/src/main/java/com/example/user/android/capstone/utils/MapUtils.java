package com.example.user.android.capstone.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by nataliakuleniuk on 7/19/17.
 */

public class MapUtils {
protected  Context context;

    public MapUtils(Context context){
        this.context = context.getApplicationContext();
    }


    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this.context);
        List<Address> address;
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







}
