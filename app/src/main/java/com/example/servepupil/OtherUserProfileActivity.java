package com.example.servepupil;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String userId;
    private DatabaseReference usersRef, reportRef;
    private FirebaseUser currentUser;

    private TextView txtName, txtPhone, txtAddress, txtFollowers, txtFollowing;
    private Button btnFollow, btnReport, btnBlock;
    private ImageView profileImage;

    private String userName = "", userBio = "";

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


        loadUserInfo();

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
                        // Unblock user
                        userRef.child("isBlocked").setValue(false);
                        reportRef.child(userId).removeValue();
                        btnBlock.setText("Block User");
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.red)); // original color or red
                    } else {
                        // Block user
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
    }

    private void loadUserInfo() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                userBio = snapshot.child("bio").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                long followers = snapshot.child("followers").getChildrenCount();
                long following = snapshot.child("following").getChildrenCount();

                txtName.setText(userName);
                txtPhone.setText(phone);
                txtAddress.setText(userBio);
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
                        btnBlock.setBackgroundTintList(getResources().getColorStateList(R.color.red)); // or original red
                    }
                } else {
                    btnBlock.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherUserProfileActivity.this, "Error loading user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
