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

public class ViewMyRequestsActivity extends AppCompatActivity {

    private LinearLayout requestsContainer;
    private DatabaseReference requestRef;
    private String uid;
    private FirebaseAuth mAuth;
    private ValueEventListener requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_requests);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();

        requestsContainer = findViewById(R.id.requestsContainer);
        requestRef = FirebaseDatabase.getInstance().getReference("requests").child(uid);

        loadRequests();
    }

    private void loadRequests() {
        requestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed()) return; // avoid crash after activity is destroyed

                requestsContainer.removeAllViews();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String requestId = snap.getKey();
                    RequestModel model = snap.getValue(RequestModel.class);
                    if (model == null) continue;

                    View cardView = LayoutInflater.from(ViewMyRequestsActivity.this).inflate(R.layout.item_request, requestsContainer, false);

                    ImageView image = cardView.findViewById(R.id.requestImage);
                    TextView desc = cardView.findViewById(R.id.requestDescription);
                    TextView type = cardView.findViewById(R.id.requestType);
                    TextView place = cardView.findViewById(R.id.requestPlace);
                    TextView likeCount = cardView.findViewById(R.id.txtLikeCount);
                    TextView commentCount = cardView.findViewById(R.id.txtCommentCount);
                    ImageView heartIcon = cardView.findViewById(R.id.imgLike);
                    ImageView commentIcon = cardView.findViewById(R.id.imgComment);

                    Button btnEdit = cardView.findViewById(R.id.btnEdit);
                    Button btnDelete = cardView.findViewById(R.id.btnDelete);

                    // Set image and text (safe from crash)
                    if (!isDestroyed()) {
                        Glide.with(ViewMyRequestsActivity.this)
                                .load(model.getImageUrl())
                                .placeholder(R.drawable.placeholder)
                                .into(image);
                    }

                    desc.setText(model.getDescription());
                    type.setText(model.getRequestType());
                    place.setText(model.getPlace());

                    // Likes
                    Map<String, Boolean> likedBy = model.getLikedBy();
                    int likes = likedBy != null ? likedBy.size() : 0;
                    likeCount.setText(String.valueOf(likes));

                    boolean isLiked = likedBy != null && likedBy.containsKey(uid);
                    heartIcon.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_unfilled);

                    // Comments
                    Map<String, CommentModel> commentsMap = model.getComments();
                    int comments = commentsMap != null ? commentsMap.size() : 0;
                    commentCount.setText(String.valueOf(comments));

                    // Toggle Like
                    heartIcon.setOnClickListener(v -> {
                        boolean currentlyLiked = likedBy != null && likedBy.containsKey(uid);
                        DatabaseReference likedByRef = requestRef.child(requestId).child("likedBy");

                        if (currentlyLiked) {
                            likedByRef.child(uid).removeValue();
                        } else {
                            likedByRef.child(uid).setValue(true);
                        }
                    });



                    // Delete
                    btnDelete.setOnClickListener(v -> {
                        requestRef.child(requestId).removeValue()
                                .addOnSuccessListener(unused -> Toast.makeText(ViewMyRequestsActivity.this, "Request deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(ViewMyRequestsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });

                    btnEdit.setOnClickListener(v -> {
                        Intent intent = new Intent(ViewMyRequestsActivity.this, EditRequestActivity.class);
                        intent.putExtra("requestId", requestId);
                        startActivity(intent);
                    });


                    // Open Comments
                    commentIcon.setOnClickListener(v -> {
                        if (uid != null && requestId != null) {
                            Intent intent = new Intent(ViewMyRequestsActivity.this, CommentsActivity.class);
                            intent.putExtra("requestOwnerUid", uid);
                            intent.putExtra("requestId", requestId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ViewMyRequestsActivity.this, "Error: UID or request ID is missing", Toast.LENGTH_SHORT).show();
                        }
                    });

                    requestsContainer.addView(cardView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isDestroyed()) {
                    Toast.makeText(ViewMyRequestsActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        requestRef.addValueEventListener(requestListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestListener != null) {
            requestRef.removeEventListener(requestListener);
        }
    }
}
