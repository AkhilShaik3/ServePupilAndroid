package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ViewOthersRequestsActivity extends AppCompatActivity {

    private LinearLayout othersContainer;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference requestsRef, usersRef;

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
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                othersContainer.removeAllViews();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (userId.equals(currentUid)) continue;

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

                        Glide.with(ViewOthersRequestsActivity.this)
                                .load(request.getImageUrl())
                                .placeholder(R.drawable.placeholder)
                                .into(image);

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

                        btnReport.setOnClickListener(v -> Toast.makeText(ViewOthersRequestsActivity.this, "Reported", Toast.LENGTH_SHORT).show());

                        othersContainer.addView(cardView);

                        usernameView.setOnClickListener(v -> {
                            Intent intent = new Intent(ViewOthersRequestsActivity.this, OtherUserProfileActivity.class);
                            intent.putExtra("userId", userId); // Pass the clicked user's ID
                            startActivity(intent);
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewOthersRequestsActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
