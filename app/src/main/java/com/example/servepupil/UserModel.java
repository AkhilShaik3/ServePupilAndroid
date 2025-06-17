package com.example.servepupil;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String id;
    private String name;
    private String bio;
    private String phone;
    private String imageUrl;
    private int followers;
    private int following;

    // Required empty constructor for Firebase
    public UserModel() {
    }

    // Full constructor
    public UserModel(String id, String name, String bio, String phone, String imageUrl, int followers, int following) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.followers = followers;
        this.following = following;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public int getFollowing() { return following; }
    public void setFollowing(int following) { this.following = following; }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("bio", bio);
        result.put("phone", phone);
        result.put("imageUrl", imageUrl);
        result.put("followers", followers);
        result.put("following", following);
        return result;
    }

    // Optional: Build from a Map (snapshot.getValue(Map.class))
    public static UserModel fromMap(String id, Map<String, Object> data) {
        return new UserModel(
                id,
                (String) data.getOrDefault("name", ""),
                (String) data.getOrDefault("bio", ""),
                (String) data.getOrDefault("phone", ""),
                (String) data.getOrDefault("imageUrl", ""),
                ((Long) data.getOrDefault("followers", 0L)).intValue(),
                ((Long) data.getOrDefault("following", 0L)).intValue()
        );
    }
}
