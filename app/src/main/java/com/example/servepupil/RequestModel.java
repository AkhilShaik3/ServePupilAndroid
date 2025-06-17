package com.example.servepupil;

import java.util.HashMap;
import java.util.Map;

public class RequestModel {
    private String id;
    private String description;
    private String requestType;
    private String place;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private long timestamp;
    private int likes;
    private Map<String, Boolean> likedBy;
    private Map<String, CommentModel> comments;

    public RequestModel() {
        // Default constructor
    }

    public RequestModel(String id, String description, String requestType, String place, double latitude, double longitude, String imageUrl, long timestamp) {
        this.id = id;
        this.description = description;
        this.requestType = requestType;
        this.place = place;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.likes = 0;
        this.likedBy = new HashMap<>();
        this.comments = new HashMap<>();
    }

    public String getId() {
        return id;
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

    public int getLikes() {
        return likes;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public Map<String, CommentModel> getComments() {
        return comments;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }

    public void setComments(Map<String, CommentModel> comments) {
        this.comments = comments;
    }
}
