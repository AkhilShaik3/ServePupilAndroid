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

import java.util.Map;

public class ViewOthersRequestsActivity extends AppCompatActivity {

    private LinearLayout othersContainer;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference requestsRef, usersRef;
    private ValueEventListener requestsListener;

    private boolean isActivityDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_others_requests);

        othersContainer = findViewById(R.id.othersRequestsContainer);
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getUid();

        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadOthersRequests();
    }

    private void loadOthersRequests() {
        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isActivityDestroyed) {
                    // Activity is destroyed, skip loading UI
                    return;
                }

                othersContainer.removeAllViews();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (userId == null || userId.equals(currentUid)) continue;

                    for (DataSnapshot requestSnap : userSnap.getChildren()) {
                        String requestId = requestSnap.getKey();
                        RequestModel request = requestSnap.getValue(RequestModel.class);
                        if (request == null) continue;

                        View cardView = LayoutInflater.from(ViewOthersRequestsActivity.this)
                                .inflate(R.layout.item_other_request, othersContainer, false);

                        ImageView image = cardView.findViewById(R.id.requestImage);
                        TextView desc = cardView.findViewById(R.id.requestDescription);
                        TextView type = cardView.findViewById(R.id.requestType);
                        TextView place = cardView.findViewById(R.id.requestPlace);
                        TextView likeCount = cardView.findViewById(R.id.txtLikeCount);
                        TextView commentCount = cardView.findViewById(R.id.txtCommentCount);
                        TextView usernameView = cardView.findViewById(R.id.requestUsername);
                        ImageView heartIcon = cardView.findViewById(R.id.imgLike);
                        ImageView commentIcon = cardView.findViewById(R.id.imgComment);
                        Button btnReport = cardView.findViewById(R.id.btnReport);

                        // Glide with lifecycle check
                        if (!isFinishing() && !isActivityDestroyed) {
                            Glide.with(ViewOthersRequestsActivity.this)
                                    .load(request.getImageUrl())
                                    .placeholder(R.drawable.placeholder)
                                    .into(image);
                        }

                        desc.setText(request.getDescription());
                        type.setText(request.getRequestType());
                        place.setText(request.getPlace());

                        // Username
                        usersRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = snapshot.getValue(String.class);
                                usernameView.setText(name != null ? name : "Unknown");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                usernameView.setText("Unknown");
                            }
                        });

                        // Likes
                        Map<String, Boolean> likedBy = request.getLikedBy();
                        int likes = likedBy != null ? likedBy.size() : 0;
                        likeCount.setText(String.valueOf(likes));
                        boolean isLiked = likedBy != null && likedBy.containsKey(currentUid);
                        heartIcon.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_unfilled);

                        heartIcon.setOnClickListener(v -> toggleLike(userId, requestId, likedBy, heartIcon));

                        // Comments
                        Map<String, CommentModel> comments = request.getComments();
                        commentCount.setText(String.valueOf(comments != null ? comments.size() : 0));

                        commentIcon.setOnClickListener(v -> {
                            Intent intent = new Intent(ViewOthersRequestsActivity.this, CommentsActivity.class);
                            intent.putExtra("requestOwnerUid", userId);
                            intent.putExtra("requestId", requestId);
                            startActivity(intent);
                        });

                        // Report Button
                        btnReport.setOnClickListener(v -> {
                            DatabaseReference reportRef = FirebaseDatabase.getInstance()
                                    .getReference("reported_content")
                                    .child("requests")
                                    .child(requestId);

                            reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Toast.makeText(ViewOthersRequestsActivity.this, "This request is already reported", Toast.LENGTH_SHORT).show();
                                    } else {
                                        reportRef.setValue(true).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ViewOthersRequestsActivity.this, "Reported successfully", Toast.LENGTH_SHORT).show();
                                                btnReport.setEnabled(false);
                                            } else {
                                                Toast.makeText(ViewOthersRequestsActivity.this, "Failed to report", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ViewOthersRequestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        othersContainer.addView(cardView);

                        usernameView.setOnClickListener(v -> {
                            Intent intent = new Intent(ViewOthersRequestsActivity.this, OtherUserProfileActivity.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing() && !isActivityDestroyed) {
                    Toast.makeText(ViewOthersRequestsActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        requestsRef.addValueEventListener(requestsListener);
    }

    private void toggleLike(String ownerUid, String requestId, Map<String, Boolean> likedBy, ImageView heartIcon) {
        DatabaseReference likeRef = requestsRef.child(ownerUid).child(requestId).child("likedBy");
        String uid = FirebaseAuth.getInstance().getUid();

        if (likedBy != null && likedBy.containsKey(uid)) {
            likeRef.child(uid).removeValue();
        } else {
            likeRef.child(uid).setValue(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;

        if (requestsListener != null && requestsRef != null) {
            requestsRef.removeEventListener(requestsListener);
        }
    }
}
