package com.example.servepupil;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.Glide;

import java.util.List;

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

        Glide.with(getContext())
                .load(request.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(image);

        desc.setText(request.getDescription());
        type.setText(request.getRequestType());
        place.setText(request.getPlace());

        return convertView;
    }
}
