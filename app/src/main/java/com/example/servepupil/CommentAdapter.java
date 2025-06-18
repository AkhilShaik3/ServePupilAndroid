package com.example.servepupil;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<CommentModel> comments;

    public CommentAdapter(Context context, List<CommentModel> comments) {
        this.context = context;
        this.comments = comments;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsername, txtComment, txtTimestamp;

        public CommentViewHolder(View itemView) {
            super(itemView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
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

        holder.txtComment.setText(comment.getText());

        long timestamp = comment.getTimestamp();
        String formattedTime = DateFormat.format("dd MMM yyyy HH:mm", new Date(timestamp)).toString();
        holder.txtTimestamp.setText(formattedTime);

        String uid = comment.getUid();
        if (uid == null) {
            holder.txtUsername.setText("Unknown");
            return;
        }

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


    @Override
    public int getItemCount() {
        return comments.size();
    }
}
