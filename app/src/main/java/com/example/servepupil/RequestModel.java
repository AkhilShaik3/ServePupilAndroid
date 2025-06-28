package com.example.servepupil;

import java.util.Map;

public class RequestModel {

    private String ownerUid;   // ADD THIS FIELD

    private String description;
    private String requestType;
    private String place;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private long timestamp;
    private Map<String, Boolean> likedBy;
    private Map<String, CommentModel> comments;

    public RequestModel() {
        // Required empty constructor for Firebase
    }

    // Getter and setter for ownerUid
    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    // Other getters and setters

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }

    public Map<String, CommentModel> getComments() {
        return comments;
    }

    public void setComments(Map<String, CommentModel> comments) {
        this.comments = comments;
    }
}
