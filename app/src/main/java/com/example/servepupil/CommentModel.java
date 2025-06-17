package com.example.servepupil;

public class CommentModel {
    private String uid;
    private String text;
    private long timestamp;

    public CommentModel() {
        // Required for Firebase deserialization
    }

    public CommentModel(String uid, String text, long timestamp) {
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
