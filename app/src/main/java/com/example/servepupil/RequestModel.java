package com.example.servepupil;

public class RequestModel {
    private String description;
    private String requestType;
    private String place;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private long timestamp;

    public RequestModel() {
        // Required empty constructor for Firebase
    }

    public String getDescription() {
        return description;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPlace() {
        return place;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
