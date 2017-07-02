package com.example.user.android.capstone;

/**
 * Created by nataliakuleniuk on 6/28/17.
 */

public class Event {

    private final String id;
    private final String sportType;
    private final String address;
    private final String dataTime;
    private final String details;
    private final String peopleNeeded;
    private final String creatorId;


    public String getSportType() {
        return sportType;
    }

    public String getAddress() {
        return address;
    }

    public String getDataTime() {
        return dataTime;
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

    public Event(String id, String sportType, String address, String dataTime, String details, String peopleNeeded, String creatorId) {
        this.id = id;
        this.sportType = sportType;
        this.address = address;
        this.dataTime = dataTime;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;
    }

    public Event( String sportType, String address, String dataTime, String details, String peopleNeeded, String creatorId) {
        this.id = null;
        this.sportType = sportType;
        this.address = address;
        this.dataTime = dataTime;
        this.details = details;
        this.peopleNeeded = peopleNeeded;
        this.creatorId = creatorId;


    }
}
