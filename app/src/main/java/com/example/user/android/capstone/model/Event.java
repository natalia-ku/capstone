package com.example.user.android.capstone.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nataliakuleniuk on 6/28/17.
 */

public class Event  implements Parcelable, Serializable{

    private  String id;
    private String title;
    private  String address;
    private  String date;
    private  String time;
    private  String details;
    private  String peopleNeeded;
    private  String creatorId;
    private String sportCategory;


    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getSportCategory() {
        return sportCategory;
    }

    public void setSportCategory(String sportCategory) {
        this.sportCategory = sportCategory;
    }

    public String getAddress() {
        return address;
    }

    public String getDataTime() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public void setPeopleNeeded(String peopleNeeded) {
        this.peopleNeeded = peopleNeeded;
    }

    public String getPeopleNeeded() {
        return peopleNeeded;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getId() {
        return id;
    }

    public  static String placeAddress;

    public Event(String sportCategory, String id, String title, String address, String date, String time, String details, String peopleNeeded, String creatorId) {
        this.sportCategory = sportCategory;
        this.id = id;
        this.title = title;
        this.address = address;
        this.date = date;
        this.time = time;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;
    }

    public Event(String sportCategory, String title, String address, String date, String time, String details, String peopleNeeded, String creatorId) {
        this.sportCategory = sportCategory;
        this.id = null;
        this.title = title;
        this.address = address;
        this.date = date;
        this.time = time;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;


    }

    public   boolean checkIfDateInFuture(String date) {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date eventDate = formatter.parse(date);
            if (eventDate.after(today)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }





    // IMPLEMENT PARCELABLE INTERFACE:
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sportCategory);
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(address);
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(details);
        parcel.writeString(peopleNeeded);
        parcel.writeString(creatorId);
    }

    public void readFromParcel(Parcel in) {
        sportCategory = in.readString();
        id = in.readString();
        title = in.readString();
        address = in.readString();
        date = in.readString();
        time = in.readString();
        details = in.readString();
        peopleNeeded = in.readString();
        creatorId = in.readString();
    }


    protected Event(Parcel in) {
        sportCategory = in.readString();
        id = in.readString();
        title = in.readString();
        address = in.readString();
        date = in.readString();
        time = in.readString();
        details = in.readString();
        peopleNeeded = in.readString();
        creatorId = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };


}
