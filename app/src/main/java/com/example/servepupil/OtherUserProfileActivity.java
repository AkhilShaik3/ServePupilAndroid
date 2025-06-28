package com.example.servepupil;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Map;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String userId;
    private DatabaseReference usersRef, reportRef;
    private FirebaseUser currentUser;

    private TextView txtName, txtPhone, txtAddress, txtFollowers, txtFollowing;
    private Button btnFollow, btnReport, btnBlock;
    private ImageView profileImage;

    private ValueEventListener userListener;
    private ValueEventListener followListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        userId = getIntent().getStringExtra("userId");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        reportRef = FirebaseDatabase.getInstance().getReference("reported_content/users");

        profileImage = findViewById(R.id.profileImage);
        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        btnFollow = findViewById(R.id.btnFollow);
        btnReport = findViewById(R.id.btnReport);
        btnBlock = findViewById(R.id.btnBlock);

        setupUserListener();
        setupFollowListener();

        btnReport.setOnClickListener(v -> {
            if (userId == null || userId.equals(currentUser.getUid())) {
                Toast.makeText(this, "Cannot report yourself", Toast.LENGTH_SHORT).show();
                return;
            }

            reportRef.child(userId).setValue(true)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "User reported", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to report", Toast.LENGTH_SHORT).show());
        });

        btnBlock.setOnClickListener(v -> {
            if (userId == null) return;

            DatabaseReference userRef = usersRef.child(userId);

            userRef.child("isBlocked").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isBlocked = snapshot.getValue(Boolean.class);
                    if (isBlocked != null && isBlocked) {
                        userRef.child("isBlocked").setValue(false);
                        reportRef.child(userId).removeValue();
                        btnBlock.setText("Block User");
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                    } else {
                        userRef.child("isBlocked").setValue(true);
                        reportRef.child(userId).removeValue();
                        btnBlock.setText("Unblock User");
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.teal_700));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(OtherUserProfileActivity.this, "Failed to update block status", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnFollow.setOnClickListener(v -> {
            if (userId == null || currentUser == null || userId.equals(currentUser.getUid())) {
                Toast.makeText(this, "Invalid operation", Toast.LENGTH_SHORT).show();
                return;
            }

            final String currentUserId = currentUser.getUid();

            usersRef.child(currentUserId).child("following").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isFollowing = snapshot.exists();

                            if (isFollowing) {
                                usersRef.child(currentUserId).child("following").child(userId).removeValue();
                                usersRef.child(userId).child("followers").child(currentUserId).removeValue();
                                btnFollow.setText("Follow");
                                Toast.makeText(OtherUserProfileActivity.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                            } else {
                                usersRef.child(currentUserId).child("following").child(userId).setValue(true);
                                usersRef.child(userId).child("followers").child(currentUserId).setValue(true);
                                btnFollow.setText("Unfollow");
                                Toast.makeText(OtherUserProfileActivity.this, "Followed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(OtherUserProfileActivity.this, "Failed to update follow status", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void setupUserListener() {
        if (userId == null) return;

        userListener = usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String userBio = snapshot.child("bio").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                DataSnapshot followersSnapshot = snapshot.child("followers");
                long followers = (followersSnapshot.exists() && followersSnapshot.getValue() instanceof Map)
                        ? followersSnapshot.getChildrenCount() : 0;

                DataSnapshot followingSnapshot = snapshot.child("following");
                long following = (followingSnapshot.exists() && followingSnapshot.getValue() instanceof Map)
                        ? followingSnapshot.getChildrenCount() : 0;

                txtName.setText(userName != null ? userName : "");
                txtPhone.setText(phone != null ? phone : "");
                txtAddress.setText(userBio != null ? userBio : "");
                txtFollowers.setText(String.valueOf(followers));
                txtFollowing.setText(String.valueOf(following));

                Glide.with(OtherUserProfileActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(profileImage);

                if (currentUser != null && "admin@gmail.com".equalsIgnoreCase(currentUser.getEmail())) {
                    btnBlock.setVisibility(android.view.View.VISIBLE);
                    btnReport.setVisibility(android.view.View.GONE);
                    btnFollow.setVisibility(android.view.View.GONE);

                    Boolean isBlocked = snapshot.child("isBlocked").getValue(Boolean.class);
                    if (isBlocked != null && isBlocked) {
                        btnBlock.setText("Unblock User");
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.teal_700));
                    } else {
                        btnBlock.setText("Block User");
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                    }
                } else {
                    btnBlock.setVisibility(android.view.View.GONE);
                    btnReport.setVisibility(android.view.View.VISIBLE);
                    btnFollow.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherUserProfileActivity.this, "Error loading user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFollowListener() {
        if (currentUser == null || userId == null) return;

        followListener = usersRef.child(currentUser.getUid()).child("following").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            btnFollow.setText("Unfollow");
                        } else {
                            btnFollow.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnFollow.setText("Follow");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null && userId != null) {
            usersRef.child(userId).removeEventListener(userListener);
        }
        if (followListener != null && currentUser != null) {
            usersRef.child(currentUser.getUid()).child("following").child(userId).removeEventListener(followListener);
        }
    }
}
