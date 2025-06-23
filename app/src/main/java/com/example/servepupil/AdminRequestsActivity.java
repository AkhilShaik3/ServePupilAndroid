package com.example.servepupil;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRequestsActivity extends AppCompatActivity {

    private ListView requestsListView;
    private AdminRequestAdapter adapter;
    private final List<RequestModel> allRequests = new ArrayList<>();

    private DatabaseReference requestsRef;
    private DatabaseReference usersRef;

    private final List<String> requestKeys = new ArrayList<>();
    private final List<String> ownerUids = new ArrayList<>();
    private final Map<String, String> uidToUsername = new HashMap<>();

    private ValueEventListener requestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_requests);

        requestsListView = findViewById(R.id.requestsListView);
        requestsRef = FirebaseDatabase.getInstance().getReference("requests");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        adapter = new AdminRequestAdapter(this, allRequests, requestKeys, ownerUids, (request, position) -> deleteRequest(position));
        requestsListView.setAdapter(adapter);

        loadAllUsernamesAndListenRequests();
    }

    private void loadAllUsernamesAndListenRequests() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                uidToUsername.clear();
                for (DataSnapshot userSnap : userSnapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    String username = userSnap.child("name").getValue(String.class);
                    if (uid != null && username != null) {
                        uidToUsername.put(uid, username);
                    }
                }
                listenToRequestsRealtime();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminRequestsActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenToRequestsRealtime() {
        if (requestsListener != null) {
            requestsRef.removeEventListener(requestsListener);
        }

        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allRequests.clear();
                requestKeys.clear();
                ownerUids.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String userId = userSnap.getKey();
                    if (userId == null) continue;

                    for (DataSnapshot requestSnap : userSnap.getChildren()) {
                        String requestId = requestSnap.getKey();
                        RequestModel request = requestSnap.getValue(RequestModel.class);
                        if (request != null) {
                            request.setOwnerUid(userId);
                            allRequests.add(request);
                            requestKeys.add(requestId);
                            ownerUids.add(userId);
                        }
                    }
                }

                adapter.setUidToUsernameMap(uidToUsername);
                adapter.notifyDataSetChanged();

                if (allRequests.isEmpty()) {
                    Toast.makeText(AdminRequestsActivity.this, "No requests found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminRequestsActivity.this, "Failed to load requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        requestsRef.addValueEventListener(requestsListener);
    }

    private void deleteRequest(int position) {
        String ownerUid = ownerUids.get(position);
        String requestId = requestKeys.get(position);

        requestsRef.child(ownerUid).child(requestId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminRequestsActivity.this, "Request deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminRequestsActivity.this, "Failed to delete request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestsListener != null) {
            requestsRef.removeEventListener(requestsListener);
        }
    }
}
