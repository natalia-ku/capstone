package com.example.user.android.capstone.model;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by nataliakuleniuk on 7/3/17.
 */

public class User implements Serializable {

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoto() {
        return photo;
    }

    public String getAge() {
        return age;
    }

    private final String id;
    private final String email;
    private final String name;
    private final String gender;
    private final String photo;
    private final String age;


    public User(String id, String email, String name, String gender, String photo, String age) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.photo = photo;
        this.age = age;
    }

    public User( String email, String name, String gender, String photo, String age) {
        this.id = null;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.photo = photo;
        this.age = age;
    }



}





