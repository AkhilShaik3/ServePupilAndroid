package com.example.servepupil;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<CommentModel> comments;
    private List<String> commentKeys;
    private String requestOwnerUid, requestId;
    private String currentUid = "", currentEmail = "";

    public CommentAdapter(Context context, List<CommentModel> comments, List<String> commentKeys,
                          String requestOwnerUid, String requestId) {
        this.context = context;
        this.comments = comments;
        this.commentKeys = commentKeys;
        this.requestOwnerUid = requestOwnerUid;
        this.requestId = requestId;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
            currentEmail = user.getEmail();
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtComment, txtTimestamp;
        Button btnReport, btnDelete;

        public CommentViewHolder(View itemView) {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            btnReport = itemView.findViewById(R.id.btnReport);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = comments.get(position);
        String commentId = commentKeys.get(position);

        holder.txtComment.setText(comment.getText());

        long timestamp = comment.getTimestamp();
        String formattedTime = DateFormat.format("dd MMM yyyy HH:mm", new Date(timestamp)).toString();
        holder.txtTimestamp.setText(formattedTime);

        String uid = comment.getUid();
        if (uid == null) {
            holder.txtUsername.setText("Unknown");
        } else {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    holder.txtUsername.setText(name != null ? name : "User");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.txtUsername.setText("User");
                }
            });
        }

        holder.btnReport.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);

        // Report button for normal users
        if (!currentUid.equals(uid) && !"admin@gmail.com".equalsIgnoreCase(currentEmail)) {
            holder.btnReport.setVisibility(View.VISIBLE);
            holder.btnReport.setOnClickListener(v -> {
                DatabaseReference reportRef = FirebaseDatabase.getInstance()
                        .getReference("reported_content")
                        .child("comments")
                        .child(commentId);

                reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(context, "Comment already reported", Toast.LENGTH_SHORT).show();
                        } else {
                            reportRef.setValue(true)
                                    .addOnSuccessListener(unused -> Toast.makeText(context, "Comment reported", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to report comment", Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error reporting comment", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Delete button for admin
        if ("admin@gmail.com".equalsIgnoreCase(currentEmail)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

                // Always delete from reported_content
                rootRef.child("reported_content/comments").child(commentId).removeValue();

                if (!TextUtils.isEmpty(requestOwnerUid) && !TextUtils.isEmpty(requestId)) {
                    // Known requestId path
                    rootRef.child("requests")
                            .child(requestOwnerUid)
                            .child(requestId)
                            .child("comments")
                            .child(commentId)
                            .removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                // Do NOT remove locally here
                            });
                } else {
                    // Brute-force deletion
                    rootRef.child("requests").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                for (DataSnapshot reqSnap : userSnap.getChildren()) {
                                    DataSnapshot commentsSnap = reqSnap.child("comments");
                                    if (commentsSnap.hasChild(commentId)) {
                                        reqSnap.getRef().child("comments").child(commentId)
                                                .removeValue()
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                                    // Do NOT remove locally here
                                                });
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
