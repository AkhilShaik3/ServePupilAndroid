package com.example.servepupil;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowersFollowingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList;
    private DatabaseReference followRef, usersRef;
    private String uid;
    private String type; // "followers" or "following"
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_following);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBarUsers);

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        uid = FirebaseAuth.getInstance().getUid();
        type = getIntent().getStringExtra("type");

        if (uid == null || type == null) {
            Toast.makeText(this, "Invalid user or type", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reference to followers/following map for this user
        followRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child(type);
        // Reference to all users to fetch user info
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                userList.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(FollowersFollowingActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Count total user IDs to load for progress or empty check
                final int totalUsers = (int) snapshot.getChildrenCount();
                if (totalUsers == 0) {
                    Toast.makeText(FollowersFollowingActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // To track loaded users before updating adapter (optional)
                final int[] loadedCount = {0};

                for (DataSnapshot child : snapshot.getChildren()) {
                    String otherUserId = child.getKey();
                    if (otherUserId == null) {
                        loadedCount[0]++;
                        if (loadedCount[0] == totalUsers) {
                            adapter.notifyDataSetChanged();
                        }
                        continue;
                    }

                    usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            loadedCount[0]++;
                            Object val = userSnapshot.getValue();
                            if (val instanceof Map) {
                                UserModel user = userSnapshot.getValue(UserModel.class);
                                if (user != null) {
                                    userList.add(user);
                                }
                            }
                            if (loadedCount[0] == totalUsers) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loadedCount[0]++;
                            Toast.makeText(FollowersFollowingActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show();
                            if (loadedCount[0] == totalUsers) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FollowersFollowingActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
