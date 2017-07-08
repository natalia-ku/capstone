package com.example.user.android.capstone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nataliakuleniuk on 6/28/17.
 */

public class Event  implements Parcelable{

    private  String id;
    private String title;
    private  String address;
    private  String date;

    private  String time;
    private  String details;
    private  String peopleNeeded;
    private  String creatorId;


    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
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

    public String getPeopleNeeded() {
        return peopleNeeded;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getId() {
        return id;
    }

    public Event(String id, String title, String address, String date, String time, String details, String peopleNeeded, String creatorId) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.date = date;
        this.time = time;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;
    }

    public Event(String title, String address, String date, String time, String details, String peopleNeeded, String creatorId) {
        this.id = null;
        this.title = title;
        this.address = address;
        this.date = date;
        this.time = time;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;


    }



    // PARCELABLE:

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
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
