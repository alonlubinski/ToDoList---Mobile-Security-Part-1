package com.alon.todolist.utils.models;

import java.util.ArrayList;


public class Activity {

    private String id, type, timestampStart, timestampEnd;
    private ArrayList<Location> locationArrayList;

    // Constructor.
    public Activity() {
        locationArrayList = new ArrayList<>();
    }

    // Getters and setters.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestampStart() {
        return timestampStart;
    }

    public void setTimestampStart(String timestampStart) {
        this.timestampStart = timestampStart;
    }

    public String getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(String timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

    public ArrayList<Location> getLocationArrayList() {
        return locationArrayList;
    }

    public void setLocationArrayList(ArrayList<Location> locationArrayList) {
        this.locationArrayList = locationArrayList;
    }

    public void addLocationToArrayList(Location location){
        this.locationArrayList.add(location);
    }
}
