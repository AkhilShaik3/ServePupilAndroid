//package com.example.servepupil;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class UserModel {
//    private String id;
//    private String name;
//    private String bio;
//    private String phone;
//    private String imageUrl;
//    private int followers;
//    private int following;
//
//    // Required empty constructor for Firebase
//    public UserModel() {
//    }
//
//    // Full constructor
//    public UserModel(String id, String name, String bio, String phone, String imageUrl, int followers, int following) {
//        this.id = id;
//        this.name = name;
//        this.bio = bio;
//        this.phone = phone;
//        this.imageUrl = imageUrl;
//        this.followers = followers;
//        this.following = following;
//    }
//
//    // Getters & Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getBio() { return bio; }
//    public void setBio(String bio) { this.bio = bio; }
//
//    public String getPhone() { return phone; }
//    public void setPhone(String phone) { this.phone = phone; }
//
//    public String getImageUrl() { return imageUrl; }
//    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
//
//    public int getFollowers() { return followers; }
//    public void setFollowers(int followers) { this.followers = followers; }
//
//    public int getFollowing() { return following; }
//    public void setFollowing(int following) { this.following = following; }
//
//    // Convert to Map for Firebase
//    public Map<String, Object> toMap() {
//        Map<String, Object> result = new HashMap<>();
//        result.put("name", name);
//        result.put("bio", bio);
//        result.put("phone", phone);
//        result.put("imageUrl", imageUrl);
//        result.put("followers", followers);
//        result.put("following", following);
//        return result;
//    }
//
//    // Optional: Build from a Map (snapshot.getValue(Map.class))
//    public static UserModel fromMap(String id, Map<String, Object> data) {
//        return new UserModel(
//                id,
//                (String) data.getOrDefault("name", ""),
//                (String) data.getOrDefault("bio", ""),
//                (String) data.getOrDefault("phone", ""),
//                (String) data.getOrDefault("imageUrl", ""),
//                ((Long) data.getOrDefault("followers", 0L)).intValue(),
//                ((Long) data.getOrDefault("following", 0L)).intValue()
//        );
//    }
//}


package com.example.servepupil;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String id;
    private String name;
    private String bio;
    private String phone;
    private String imageUrl;
    private boolean isBlocked;

    private Map<String, Boolean> followers;
    private Map<String, Boolean> following;

    public UserModel() {
        // Required for Firebase
    }

    public UserModel(String id, String name, String bio, String phone, String imageUrl,
                     Map<String, Boolean> followers, Map<String, Boolean> following, boolean isBlocked) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.followers = followers;
        this.following = following;
        this.isBlocked = isBlocked;
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

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public Map<String, Boolean> getFollowers() {
        return followers != null ? followers : new HashMap<>();
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        return following != null ? following : new HashMap<>();
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }

    public int getFollowersCount() {
        return followers != null ? followers.size() : 0;
    }

    public int getFollowingCount() {
        return following != null ? following.size() : 0;
    }

    // Convert to map for saving to Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("bio", bio);
        result.put("phone", phone);
        result.put("imageUrl", imageUrl);
        result.put("isBlocked", isBlocked);
        result.put("followers", followers != null && !followers.isEmpty() ? followers : 0);
        result.put("following", following != null && !following.isEmpty() ? following : 0);
        return result;
    }

    // Optional: Build from snapshot
    @SuppressWarnings("unchecked")
    public static UserModel fromMap(String id, Map<String, Object> data) {
        Map<String, Boolean> followersMap = new HashMap<>();
        Map<String, Boolean> followingMap = new HashMap<>();

        Object followersObj = data.get("followers");
        if (followersObj instanceof Map) {
            followersMap = (Map<String, Boolean>) followersObj;
        }

        Object followingObj = data.get("following");
        if (followingObj instanceof Map) {
            followingMap = (Map<String, Boolean>) followingObj;
        }

        return new UserModel(
                id,
                (String) data.getOrDefault("name", ""),
                (String) data.getOrDefault("bio", ""),
                (String) data.getOrDefault("phone", ""),
                (String) data.getOrDefault("imageUrl", ""),
                followersMap,
                followingMap,
                (Boolean) data.getOrDefault("isBlocked", false)
        );
    }
}
