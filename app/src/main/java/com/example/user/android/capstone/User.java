package com.example.user.android.capstone;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by nataliakuleniuk on 7/3/17.
 */

public class User {

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

    public static String userId; // for findUserIdByEmail
    String userID = "";

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

//    public static String findUserIdByEmail(FirebaseUser user){
//
//       // new FirebaseQueryTask().execute(user);
//
//        loadDataFromDatabase(user);
//
//        if (userId == null){
//            System.out.println("ERROR: Cannot find user in the Database");
//            System.out.println(userId);
//        }
//        System.out.println("USER ID BEFORE RETURN STATEMENT:");
//        System.out.println(userId);
//        return userId;
//    }
//
//    private static void loadDataFromDatabase(FirebaseUser user){
//        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference mUserRef = mRootRef.child("users");
//        String userEmail = user.getEmail();
//        Query findUserQuery = mUserRef.orderByChild("email").equalTo(userEmail);
//
//        findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
//                        setUpUserID(eventSnapshot.getKey());
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }
//
//
//
//    private static void setUpUserID(String key){
//        System.out.println("I AM HERE");
//        userId = key;
//    }



}





