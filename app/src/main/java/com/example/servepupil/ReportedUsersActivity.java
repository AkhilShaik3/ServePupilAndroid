package com.example.servepupil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class ReportedUsersActivity extends AppCompatActivity {

    private LinearLayout reportedUsersContainer;
    private DatabaseReference reportedRef, usersRef;

    private ValueEventListener reportedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_users);

        reportedUsersContainer = findViewById(R.id.reportedUsersContainer);
        reportedRef = FirebaseDatabase.getInstance().getReference("reported_content/users");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        listenToReportedUsers();
    }

    private void listenToReportedUsers() {
        reportedListener = reportedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportedUsersContainer.removeAllViews();

                if (!snapshot.hasChildren()) {
                    TextView emptyView = new TextView(ReportedUsersActivity.this);
                    emptyView.setText("No reported users.");
                    emptyView.setPadding(20, 40, 20, 40);
                    reportedUsersContainer.addView(emptyView);
                    return;
                }

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (userId == null) continue;

                    usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            String name = userSnapshot.child("name").getValue(String.class);
                            String bio = userSnapshot.child("bio").getValue(String.class);

                            View view = LayoutInflater.from(ReportedUsersActivity.this)
                                    .inflate(R.layout.item_reported_user, reportedUsersContainer, false);

                            TextView txtName = view.findViewById(R.id.txtReportedUserName);
                            TextView txtBio = view.findViewById(R.id.txtReportedUserBio);
                            Button btnBlock = view.findViewById(R.id.btnBlockUser);

                            txtName.setText(name != null ? name : "Unknown");
                            txtBio.setText(bio != null ? bio : "");

                            btnBlock.setOnClickListener(v -> {
                                usersRef.child(userId).child("isBlocked").setValue(true);
                                reportedRef.child(userId).removeValue();
                                Toast.makeText(ReportedUsersActivity.this, "User blocked", Toast.LENGTH_SHORT).show();
                            });

                            reportedUsersContainer.addView(view);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ReportedUsersActivity.this, "Failed to load user", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportedUsersActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportedRef != null && reportedListener != null) {
            reportedRef.removeEventListener(reportedListener);
        }
    }
}
