package com.example.servepupil;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class UserHomeActivity extends AppCompatActivity {

    Button btnProfile, btnCreateRequest, btnViewOthers, btnViewMyRequests, btnLogout;
    FirebaseAuth mAuth;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        btnProfile = findViewById(R.id.btnProfile);
        btnCreateRequest = findViewById(R.id.btnCreateRequest);
        btnViewOthers = findViewById(R.id.btnViewOthers);
        btnViewMyRequests = findViewById(R.id.btnViewMyRequests);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(UserHomeActivity.this, LoginActivity.class));
            finish();
        });

        btnCreateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserHomeActivity.this, CreateRequestActivity.class));
            }
        });
        btnViewMyRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserHomeActivity.this, ViewMyRequestsActivity.class));
            }
        });

        btnViewOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserHomeActivity.this, ViewOthersRequestsActivity.class));
            }
        });

        btnProfile.setOnClickListener(view -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            databaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        startActivity(new Intent(UserHomeActivity.this,ProfileViewActivity.class));
                    } else {
                        startActivity(new Intent(UserHomeActivity.this, CreateProfileActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserHomeActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
