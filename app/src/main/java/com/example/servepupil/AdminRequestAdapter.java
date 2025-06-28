package com.example.servepupil;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminRequestAdapter extends ArrayAdapter<RequestModel> {

    private Map<String, String> uidToUsername = new HashMap<>();
    private OnDeleteClickListener onDeleteClickListener;

    // To hold owner UIDs and request IDs to pass on comment icon click
    private List<String> ownerUids;
    private List<String> requestKeys;

    public interface OnDeleteClickListener {
        void onDelete(RequestModel request, int position);
    }

    public AdminRequestAdapter(Context context, List<RequestModel> requests,
                               List<String> ownerUids, List<String> requestKeys,
                               OnDeleteClickListener listener) {
        super(context, 0, requests);
        this.ownerUids = ownerUids;
        this.requestKeys = requestKeys;
        this.onDeleteClickListener = listener;
    }

    public void setUidToUsernameMap(Map<String, String> map) {
        if (map != null) {
            this.uidToUsername = map;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequestModel request = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_request_admin, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.requestImage);
        TextView desc = convertView.findViewById(R.id.requestDescription);
        TextView type = convertView.findViewById(R.id.requestType);
        TextView place = convertView.findViewById(R.id.requestPlace);
        TextView likeCount = convertView.findViewById(R.id.txtLikeCount);
        TextView commentCount = convertView.findViewById(R.id.txtCommentCount);
        TextView usernameView = convertView.findViewById(R.id.requestUsername);
        ImageView commentIcon = convertView.findViewById(R.id.imgComment);
        Button deleteBtn = convertView.findViewById(R.id.btnDelete);

        Glide.with(getContext())
                .load(request.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(image);

        desc.setText(request.getDescription());
        type.setText(request.getRequestType());
        place.setText(request.getPlace());

        int likes = request.getLikedBy() != null ? request.getLikedBy().size() : 0;
        likeCount.setText(String.valueOf(likes));

        int comments = request.getComments() != null ? request.getComments().size() : 0;
        commentCount.setText(String.valueOf(comments));

        // Username display
        String username = uidToUsername.get(request.getOwnerUid());
        usernameView.setText(username != null ? username : "Unknown");

        // Click username to open OtherUserProfileActivity
        usernameView.setOnClickListener(v -> {
            String ownerUid = request.getOwnerUid();
            if (ownerUid != null && !ownerUid.isEmpty()) {
                Intent intent = new Intent(getContext(), OtherUserProfileActivity.class);
                intent.putExtra("userId", ownerUid);
                getContext().startActivity(intent);
            }
        });

        // Comment icon click opens CommentsActivity
        commentIcon.setOnClickListener(v -> {
            String ownerUid = ownerUids.get(position);
            String requestId = requestKeys.get(position);

            Intent intent = new Intent(getContext(), CommentsActivity.class);
            intent.putExtra("requestOwnerUid", ownerUid);
            intent.putExtra("requestId", requestId);
            getContext().startActivity(intent);
        });

        deleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDelete(request, position);
            }
        });

        return convertView;
    }
}
