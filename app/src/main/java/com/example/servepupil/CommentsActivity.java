package com.example.servepupil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class CommentsActivity extends AppCompatActivity {

    private String requestOwnerUid;
    private String requestId;

    private RecyclerView recyclerView;
    private EditText edtComment;
    private Button btnPost;

    private List<CommentModel> commentList = new ArrayList<>();
    private CommentAdapter adapter;

    private DatabaseReference commentsRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        requestOwnerUid = getIntent().getStringExtra("requestOwnerUid");
        requestId = getIntent().getStringExtra("requestId");

        recyclerView = findViewById(R.id.recyclerComments);
        edtComment = findViewById(R.id.edtComment);
        btnPost = findViewById(R.id.btnPost);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(this, commentList);
        recyclerView.setAdapter(adapter);

        commentsRef = FirebaseDatabase.getInstance()
                .getReference("requests")
                .child(requestOwnerUid)
                .child(requestId)
                .child("comments");

        currentUser = mAuth.getCurrentUser();

        // 🔒 Hide comment input if admin
        if (currentUser != null && "admin@gmail.com".equalsIgnoreCase(currentUser.getEmail())) {
            edtComment.setVisibility(View.GONE);
            btnPost.setVisibility(View.GONE);
        }

        monitorRequestDeletion(); // 👈 Watch if request is deleted
        fetchComments();

        btnPost.setOnClickListener(v -> {
            String text = edtComment.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            postComment(text);
        });
    }

    private void fetchComments() {
        commentsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CommentModel comment = snap.getValue(CommentModel.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }

                // Show newest first
                Collections.sort(commentList, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Error loading comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String text) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        String commentId = commentsRef.push().getKey();
        if (commentId == null) return;

        CommentModel comment = new CommentModel(uid, text, System.currentTimeMillis());

        commentsRef.child(commentId).setValue(comment)
                .addOnSuccessListener(unused -> edtComment.setText(""));
    }

    // ✅ Listen for request deletion and auto-exit
    private void monitorRequestDeletion() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("requests")
                .child(requestOwnerUid)
                .child(requestId);

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(CommentsActivity.this, "This request was deleted", Toast.LENGTH_SHORT).show();
                    finish(); // close the activity
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Failed to monitor request", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
