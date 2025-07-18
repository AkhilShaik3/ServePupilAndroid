package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.servepupil.EditProfileActivity;
import com.example.servepupil.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.example.servepupil.R;

public class ProfileViewActivity extends AppCompatActivity {

    ImageView imageProfile;
    TextView txtName, txtPhone, txtBio, txtFollowers, txtFollowing;
    Button btnEditProfile;
    TextView linkChangePassword;

    DatabaseReference userRef;
    FirebaseAuth mAuth;
    String uid;
    String userImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        imageProfile = findViewById(R.id.imageProfile);
        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtBio = findViewById(R.id.txtBio);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        linkChangePassword = findViewById(R.id.linkChangePassword);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadUserProfile();

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewActivity.this, EditProfileActivity.class);
                intent.putExtra("name", txtName.getText().toString());
                intent.putExtra("bio", txtBio.getText().toString().replace("Location: ", ""));
                intent.putExtra("phone", txtPhone.getText().toString().replace("Phone: ", ""));
                intent.putExtra("imageUrl", userImageUrl);
                startActivityForResult(intent, 1001);
            }
        });
        linkChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileViewActivity.this, ChangePasswordActivity.class));
            }
        });
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel user = snapshot.getValue(UserModel.class);

                    if (user != null) {
                        txtName.setText(user.getName());
                        txtPhone.setText("Phone: " + user.getPhone());
                        txtBio.setText("Location: " + user.getBio());
                        txtFollowers.setText(String.valueOf(user.getFollowers()));
                        txtFollowing.setText(String.valueOf(user.getFollowing()));

                        userImageUrl = user.getImageUrl();

                        Glide.with(ProfileViewActivity.this)
                                .load(userImageUrl)
                                .placeholder(R.drawable.placeholder)
                                .into(imageProfile);
                    }
                } else {
                    Toast.makeText(ProfileViewActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileViewActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadUserProfile();
        }
    }
}
