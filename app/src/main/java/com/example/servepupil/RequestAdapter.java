package com.example.servepupil;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Map;

public class RequestAdapter extends ArrayAdapter<RequestModel> {

    public RequestAdapter(Context context, List<RequestModel> requests) {
        super(context, 0, requests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequestModel request = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_request, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.requestImage);
        TextView desc = convertView.findViewById(R.id.requestDescription);
        TextView type = convertView.findViewById(R.id.requestType);
        TextView place = convertView.findViewById(R.id.requestPlace);
        TextView likeCount = convertView.findViewById(R.id.txtLikeCount);
        TextView commentCount = convertView.findViewById(R.id.txtCommentCount);
        ImageView likeIcon = convertView.findViewById(R.id.imgLike);
        ImageView commentIcon = convertView.findViewById(R.id.imgComment);
        Button editBtn = convertView.findViewById(R.id.btnEdit);
        Button deleteBtn = convertView.findViewById(R.id.btnDelete);

        // Load image
        Glide.with(getContext())
                .load(request.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(image);

        // Bind data
        desc.setText(request.getDescription());
        type.setText(request.getRequestType());
        place.setText(request.getPlace());

        // Calculate likes count from likedBy map
        int likes = request.getLikedBy() != null ? request.getLikedBy().size() : 0;
        likeCount.setText(String.valueOf(likes));

        commentCount.setText(String.valueOf(request.getComments() != null ? request.getComments().size() : 0));

        // Handle heart icon state
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUid = auth.getUid();
        Map<String, Boolean> likedBy = request.getLikedBy();

        if (likedBy != null && likedBy.containsKey(currentUid)) {
            likeIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            likeIcon.setImageResource(R.drawable.ic_heart_unfilled);
        }

        // Optional: Add click listeners if needed for likeIcon, edit, delete etc.

        return convertView;
    }
}
