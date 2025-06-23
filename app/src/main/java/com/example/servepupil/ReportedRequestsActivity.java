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

public class ReportedRequestsActivity extends AppCompatActivity {

    private LinearLayout reportedContainer;
    private DatabaseReference reportedRef, requestsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUid;

    private final Map<String, View> requestViewsMap = new HashMap<>(); // track views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_requests);

        reportedContainer = findViewById(R.id.reportedRequestsContainer);
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getUid();

        reportedRef = FirebaseDatabase.getInstance().getReference("reported_content").child("requests");
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadReportedRequests();
    }

    private void loadReportedRequests() {
        reportedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportedContainer.removeAllViews();
                requestViewsMap.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String requestId = snap.getKey();
                    fetchRequestDetails(requestId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportedRequestsActivity.this, "Failed to load reported requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRequestDetails(String requestId) {
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allUsersSnapshot) {
                for (DataSnapshot userSnap : allUsersSnapshot.getChildren()) {
                    String userId = userSnap.getKey();

                    if (userSnap.hasChild(requestId)) {
                        DatabaseReference requestNode = requestsRef.child(userId).child(requestId);

                        // 👇 Attach a live listener to this request in case it's deleted
                        requestNode.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot requestSnap) {
                                if (!requestSnap.exists()) {
                                    // Request was deleted → remove from UI
                                    View viewToRemove = requestViewsMap.get(requestId);
                                    if (viewToRemove != null) {
                                        reportedContainer.removeView(viewToRemove);
                                        requestViewsMap.remove(requestId);
                                    }
                                    return;
                                }

                                RequestModel request = requestSnap.getValue(RequestModel.class);
                                if (request == null) return;

                                if (requestViewsMap.containsKey(requestId)) return; // already rendered

                                View cardView = LayoutInflater.from(ReportedRequestsActivity.this)
                                        .inflate(R.layout.item_other_request, reportedContainer, false);

                                ImageView image = cardView.findViewById(R.id.requestImage);
                                TextView desc = cardView.findViewById(R.id.requestDescription);
                                TextView type = cardView.findViewById(R.id.requestType);
                                TextView place = cardView.findViewById(R.id.requestPlace);
                                TextView likeCount = cardView.findViewById(R.id.txtLikeCount);
                                TextView commentCount = cardView.findViewById(R.id.txtCommentCount);
                                TextView usernameView = cardView.findViewById(R.id.requestUsername);
                                ImageView heartIcon = cardView.findViewById(R.id.imgLike);
                                ImageView commentIcon = cardView.findViewById(R.id.imgComment);
                                Button btnDelete = cardView.findViewById(R.id.btnReport);

                                btnDelete.setText("Delete Request");

                                Glide.with(ReportedRequestsActivity.this)
                                        .load(request.getImageUrl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(image);

                                desc.setText(request.getDescription());
                                type.setText(request.getRequestType());
                                place.setText(request.getPlace());

                                Map<String, Boolean> likedBy = request.getLikedBy();
                                int likes = likedBy != null ? likedBy.size() : 0;
                                likeCount.setText(String.valueOf(likes));

                                Map<String, CommentModel> comments = request.getComments();
                                commentCount.setText(String.valueOf(comments != null ? comments.size() : 0));

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

                                // Comment click
                                commentIcon.setOnClickListener(v -> {
                                    Intent intent = new Intent(ReportedRequestsActivity.this, CommentsActivity.class);
                                    intent.putExtra("requestOwnerUid", userId);
                                    intent.putExtra("requestId", requestId);
                                    startActivity(intent);
                                });

                                // Delete request
                                btnDelete.setOnClickListener(v -> {
                                    requestsRef.child(userId).child(requestId).removeValue().addOnSuccessListener(unused -> {
                                        reportedRef.child(requestId).removeValue();
                                        Toast.makeText(ReportedRequestsActivity.this, "Request deleted", Toast.LENGTH_SHORT).show();
                                    });
                                });

                                usernameView.setOnClickListener(v -> {
                                    Intent intent = new Intent(ReportedRequestsActivity.this, OtherUserProfileActivity.class);
                                    intent.putExtra("userId", userId);
                                    startActivity(intent);
                                });

                                reportedContainer.addView(cardView);
                                requestViewsMap.put(requestId, cardView);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ReportedRequestsActivity.this, "Error fetching request", Toast.LENGTH_SHORT).show();
                            }
                        });

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportedRequestsActivity.this, "Error fetching users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
