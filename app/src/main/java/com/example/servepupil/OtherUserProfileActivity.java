package com.example.servepupil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String userId;
    private DatabaseReference usersRef;

    private TextView txtName, txtPhone, txtAddress, txtFollowers, txtFollowing;
    private Button btnFollow, btnReport;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        userId = getIntent().getStringExtra("userId");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        profileImage = findViewById(R.id.profileImage);
        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        btnFollow = findViewById(R.id.btnFollow);
        btnReport = findViewById(R.id.btnReport);

        loadUserInfo();

        btnFollow.setOnClickListener(v ->
                Toast.makeText(this, "Follow clicked", Toast.LENGTH_SHORT).show()
        );

        btnReport.setOnClickListener(v ->
                Toast.makeText(this, "User reported", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserInfo() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String address = snapshot.child("bio").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                long followers = snapshot.child("followers").getChildrenCount();
                long following = snapshot.child("following").getChildrenCount();

                txtName.setText(name);
                txtPhone.setText(phone);
                txtAddress.setText(address);
                txtFollowers.setText(String.valueOf(followers));
                txtFollowing.setText(String.valueOf(following));

                Glide.with(OtherUserProfileActivity.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OtherUserProfileActivity.this, "Error loading user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
