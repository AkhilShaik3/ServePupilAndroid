package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.Map;

public class UsersListActivity extends AppCompatActivity {

    private LinearLayout usersContainer;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        usersContainer = findViewById(R.id.usersContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersContainer.removeAllViews();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    if (userSnap.getValue() == null) continue;

                    // Safe deserialization with fromMap helper method
                    Map<String, Object> userMap = (Map<String, Object>) userSnap.getValue();
                    UserModel user = UserModel.fromMap(userSnap.getKey(), userMap);
                    if (user == null) continue;

                    View userView = LayoutInflater.from(UsersListActivity.this)
                            .inflate(R.layout.item_user_admin, usersContainer, false);

                    ImageView userImage = userView.findViewById(R.id.imgUser);
                    TextView name = userView.findViewById(R.id.txtName);
                    TextView address = userView.findViewById(R.id.txtAddress);
                    TextView phone = userView.findViewById(R.id.txtPhone);
//                    TextView followersView = userView.findViewById(R.id.txtFollowers);
//                    TextView followingView = userView.findViewById(R.id.txtFollowing);
                    ImageButton editBtn = userView.findViewById(R.id.btnEdit);
                    Button blockBtn = userView.findViewById(R.id.btnBlock);

                    name.setText(user.getName());
                    // Replace with real address or bio if you want
                    address.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "No bio");
                    phone.setText(user.getPhone());

//                    followersView.setText("Followers: " + user.getFollowersCount());
//                    followingView.setText("Following: " + user.getFollowingCount());

                    Glide.with(UsersListActivity.this)
                            .load(user.getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(userImage);

                    // Setup block/unblock button based on user's blocked status
                    boolean isBlocked = user.isBlocked();
                    blockBtn.setText(isBlocked ? "Unblock" : "Block");
                    blockBtn.setBackgroundTintList(getResources().getColorStateList(
                            isBlocked ? R.color.teal_700 : R.color.red
                    ));

                    blockBtn.setOnClickListener(v -> {
                        boolean newBlockedState = !user.isBlocked();
                        usersRef.child(user.getId()).child("isBlocked").setValue(newBlockedState);
                        Toast.makeText(UsersListActivity.this,
                                newBlockedState ? "User Blocked" : "User Unblocked",
                                Toast.LENGTH_SHORT).show();
                    });

                    editBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(UsersListActivity.this, AdminEditProfileActivity.class);
                        intent.putExtra("userId", user.getId());
                        startActivity(intent);
                    });


                    usersContainer.addView(userView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
