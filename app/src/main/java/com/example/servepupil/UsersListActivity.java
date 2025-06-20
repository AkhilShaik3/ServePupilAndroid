package com.example.servepupil;

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
import com.google.firebase.database.*;

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
                    UserModel user = userSnap.getValue(UserModel.class);
                    if (user == null) continue;

                    // ✅ Fix: Ensure user ID is set manually from snapshot key
                    user.setId(userSnap.getKey());

                    View userView = LayoutInflater.from(UsersListActivity.this)
                            .inflate(R.layout.item_user_admin, usersContainer, false);

                    ImageView userImage = userView.findViewById(R.id.imgUser);
                    TextView name = userView.findViewById(R.id.txtName);
                    TextView address = userView.findViewById(R.id.txtAddress);
                    TextView phone = userView.findViewById(R.id.txtPhone);
                    ImageButton editBtn = userView.findViewById(R.id.btnEdit);
                    Button blockBtn = userView.findViewById(R.id.btnBlock);

                    name.setText(user.getName());
                    address.setText("2125 Saint Marc"); // Static address or update from DB if available
                    phone.setText(user.getPhone());

                    Glide.with(UsersListActivity.this)
                            .load(user.getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(userImage);

                    usersRef.child(user.getId()).child("isBlocked")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean isBlocked = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                                    blockBtn.setText(isBlocked ? "Unblock" : "Block");
                                    blockBtn.setBackgroundTintList(getResources().getColorStateList(
                                            isBlocked ? R.color.teal_700 : R.color.red
                                    ));

                                    blockBtn.setOnClickListener(v -> {
                                        usersRef.child(user.getId()).child("isBlocked").setValue(!isBlocked);
                                        Toast.makeText(UsersListActivity.this,
                                                isBlocked ? "User Unblocked" : "User Blocked",
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UsersListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    editBtn.setOnClickListener(v ->
                            Toast.makeText(UsersListActivity.this, "Edit clicked for " + user.getName(), Toast.LENGTH_SHORT).show()
                    );

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
