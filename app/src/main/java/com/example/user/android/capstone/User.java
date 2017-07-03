package com.example.user.android.capstone;

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
