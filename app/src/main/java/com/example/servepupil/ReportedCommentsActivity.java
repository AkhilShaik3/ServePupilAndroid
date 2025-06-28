package com.example.servepupil;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.firebase.database.*;

import java.util.*;

public class ReportedCommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<CommentModel> reportedComments = new ArrayList<>();
    private List<String> commentKeys = new ArrayList<>();

    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    private ValueEventListener reportedCommentsListener;
    private ValueEventListener requestsListener;

    private Set<String> reportedIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reported_comments);

        recyclerView = findViewById(R.id.recyclerComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this, reportedComments, commentKeys, "", "");
        recyclerView.setAdapter(adapter);

        listenToReportedComments();
    }

    private void listenToReportedComments() {
        reportedCommentsListener = databaseRef.child("reported_content").child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reportedIds.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            reportedIds.add(snap.getKey());
                        }
                        listenToRequests();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ReportedCommentsActivity.this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenToRequests() {
        // Remove old listener before attaching new
        if (requestsListener != null) {
            databaseRef.child("requests").removeEventListener(requestsListener);
        }

        requestsListener = databaseRef.child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reportedComments.clear();
                        commentKeys.clear();

                        for (DataSnapshot userNode : snapshot.getChildren()) {
                            for (DataSnapshot requestNode : userNode.getChildren()) {
                                DataSnapshot commentsNode = requestNode.child("comments");

                                for (DataSnapshot commentSnap : commentsNode.getChildren()) {
                                    String commentId = commentSnap.getKey();
                                    if (reportedIds.contains(commentId)) {
                                        CommentModel comment = commentSnap.getValue(CommentModel.class);
                                        if (comment != null) {
                                            reportedComments.add(comment);
                                            commentKeys.add(commentId);
                                        }
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (reportedComments.isEmpty()) {
                            Toast.makeText(ReportedCommentsActivity.this, "No reported comments", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ReportedCommentsActivity.this, "Error fetching comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportedCommentsListener != null) {
            databaseRef.child("reported_content").child("comments").removeEventListener(reportedCommentsListener);
        }
        if (requestsListener != null) {
            databaseRef.child("requests").removeEventListener(requestsListener);
        }
    }
}
