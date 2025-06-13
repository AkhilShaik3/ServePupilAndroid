package com.example.servepupil;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ViewMyRequestsActivity extends AppCompatActivity {

    private ListView listView;
    private List<RequestModel> requestList;
    private RequestAdapter adapter;
    private DatabaseReference requestRef;
    private FirebaseAuth mAuth;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_requests);

        listView = findViewById(R.id.listViewRequests);
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(this, requestList);
        listView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        requestRef = FirebaseDatabase.getInstance().getReference("requests").child(uid);

        fetchRequests();
    }

    private void fetchRequests() {
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot requestSnap : snapshot.getChildren()) {
                    RequestModel model = requestSnap.getValue(RequestModel.class);
                    if (model != null) {
                        requestList.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewMyRequestsActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
