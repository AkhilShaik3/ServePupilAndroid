package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class PersonalFeedActivity extends AppCompatActivity {

    private LinearLayout personalFeedContainer;
    private ProgressBar progressBar;

    private DatabaseReference usersRef, requestsRef;
    private String currentUserId;

    private ValueEventListener followingListener;
    private Map<String, ValueEventListener> userRequestListeners = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_feed);

        personalFeedContainer = findViewById(R.id.personalFeedContainer);
        progressBar = findViewById(R.id.feedProgressBar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");

        setupFollowingListener();
    }

    private void setupFollowingListener() {
        progressBar.setVisibility(View.VISIBLE);

        followingListener = usersRef.child(currentUserId).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot followingSnap) {
                        progressBar.setVisibility(View.GONE);

                        // Clear feed and remove previous request listeners
                        personalFeedContainer.removeAllViews();
                        removeAllUserRequestListeners();

                        if (!followingSnap.exists()) {
                            Toast.makeText(PersonalFeedActivity.this, "You are not following anyone yet.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot userIdSnap : followingSnap.getChildren()) {
                            String followedUserId = userIdSnap.getKey();
                            if (followedUserId != null) {
                                listenToUserRequests(followedUserId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PersonalFeedActivity.this, "Failed to load following list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenToUserRequests(String userId) {
        ValueEventListener userRequestsListener = requestsRef.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot requestsSnap) {
                        removeRequestsForUser(userId);

                        for (DataSnapshot reqSnap : requestsSnap.getChildren()) {
                            RequestModel request = reqSnap.getValue(RequestModel.class);
                            if (request != null) {
                                request.setId(reqSnap.getKey());
                                displayRequest(userId, request);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Optional error handling
                    }
                });

        userRequestListeners.put(userId, userRequestsListener);
    }

    private void removeAllUserRequestListeners() {
        for (Map.Entry<String, ValueEventListener> entry : userRequestListeners.entrySet()) {
            requestsRef.child(entry.getKey()).removeEventListener(entry.getValue());
        }
        userRequestListeners.clear();
    }

    private void removeRequestsForUser(String userId) {
        for (int i = personalFeedContainer.getChildCount() - 1; i >= 0; i--) {
            View child = personalFeedContainer.getChildAt(i);
            if (userId.equals(child.getTag())) {
                personalFeedContainer.removeViewAt(i);
            }
        }
    }

    private void displayRequest(String userId, RequestModel request) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_request_feed, personalFeedContainer, false);

        // Tag card by userId for removal when feed updates
        card.setTag(userId);

        ImageView imageView = card.findViewById(R.id.requestImage);
        TextView desc = card.findViewById(R.id.requestDescription);
        TextView type = card.findViewById(R.id.requestType);
        TextView place = card.findViewById(R.id.requestPlace);
        TextView username = card.findViewById(R.id.requestUsername);
        TextView likeCount = card.findViewById(R.id.txtLikeCount);
        TextView commentCount = card.findViewById(R.id.txtCommentCount);
        Button btnReport = card.findViewById(R.id.btnReport);
        ImageView imgLike = card.findViewById(R.id.imgLike);
        ImageView imgComment = card.findViewById(R.id.imgComment);

        desc.setText(request.getDescription());
        type.setText(request.getRequestType());
        place.setText(request.getPlace());

        Glide.with(this)
                .load(request.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(imageView);

        usersRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String name = snap.getValue(String.class);
                username.setText(name != null ? name : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                username.setText("Unknown");
            }
        });

        DatabaseReference requestRef = requestsRef.child(userId).child(request.getId());
        ValueEventListener requestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RequestModel updatedRequest = snapshot.getValue(RequestModel.class);
                if (updatedRequest != null) {
                    Map<String, Boolean> likedBy = updatedRequest.getLikedBy();
                    int likes = likedBy != null ? likedBy.size() : 0;
                    likeCount.setText(String.valueOf(likes));

                    boolean isLiked = likedBy != null && likedBy.containsKey(currentUserId);
                    imgLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_unfilled);

                    Map<String, CommentModel> comments = updatedRequest.getComments();
                    commentCount.setText(String.valueOf(comments != null ? comments.size() : 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        requestRef.addValueEventListener(requestListener);

        imgLike.setOnClickListener(v -> toggleLike(userId, request.getId()));

        imgComment.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalFeedActivity.this, CommentsActivity.class);
            intent.putExtra("requestOwnerUid", userId);
            intent.putExtra("requestId", request.getId());
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference("reported_content")
                    .child("requests").child(userId).child(request.getId());
            reportRef.setValue(true);
            Toast.makeText(PersonalFeedActivity.this, "Request reported", Toast.LENGTH_SHORT).show();
        });

        username.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalFeedActivity.this, OtherUserProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        personalFeedContainer.addView(card);
    }

    private void toggleLike(String ownerUid, String requestId) {
        DatabaseReference likeRef = requestsRef.child(ownerUid).child(requestId).child("likedBy");
        String uid = currentUserId;

        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.hasChild(uid);
                if (isLiked) {
                    likeRef.child(uid).removeValue();
                } else {
                    likeRef.child(uid).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalFeedActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (followingListener != null) {
            usersRef.child(currentUserId).child("following").removeEventListener(followingListener);
        }
        removeAllUserRequestListeners();
    }
}
