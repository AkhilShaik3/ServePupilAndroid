package com.example.servepupil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.*;

public class CreateRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText edtDescription, edtType, edtPlace;
    private ListView placeSuggestions;
    private ImageView imagePreview;
    private Button btnSubmit;
    private GoogleMap mMap;
    private MapView mapView;
    private Uri imageUri;

    private PlacesClient placesClient;
    private ArrayAdapter<String> adapter;
    private List<AutocompletePrediction> predictionList;
    private LatLng selectedLatLng = new LatLng(45.5017, -73.5673);

    private FirebaseAuth mAuth;
    private DatabaseReference requestRef;
    private StorageReference storageRef;
    private String uid;

    private static final int IMAGE_PICK_REQUEST = 101;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        edtDescription = findViewById(R.id.edtDescription);
        edtType = findViewById(R.id.edtType);
        edtPlace = findViewById(R.id.edtPlace);
        placeSuggestions = findViewById(R.id.placeSuggestions);
        imagePreview = findViewById(R.id.imagePreview);
        btnSubmit = findViewById(R.id.btnSubmit);
        mapView = findViewById(R.id.mapView);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        requestRef = FirebaseDatabase.getInstance().getReference("requests").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("request_images");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "YOUR_GOOGLE_MAPS_API_KEY");
        }
        placesClient = Places.createClient(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        placeSuggestions.setAdapter(adapter);

        edtPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener(response -> {
                            predictionList = response.getAutocompletePredictions();
                            List<String> suggestions = new ArrayList<>();
                            for (AutocompletePrediction prediction : predictionList) {
                                suggestions.add(prediction.getFullText(null).toString());
                            }
                            adapter.clear();
                            adapter.addAll(suggestions);
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        placeSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = predictionList.get(position);
            edtPlace.setText(prediction.getFullText(null).toString());
            placeSuggestions.setVisibility(View.GONE);

            FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(
                    prediction.getPlaceId(),
                    Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)).build();

            placesClient.fetchPlace(placeRequest)
                    .addOnSuccessListener(response -> {
                        selectedLatLng = response.getPlace().getLatLng();
                        if (mMap != null) {
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Selected"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                        }
                    });
        });

        imagePreview.setOnClickListener(v -> openImagePicker());

        btnSubmit.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndCreateRequest();
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle mapViewBundle = savedInstanceState != null ?
                savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : new Bundle();
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imagePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageAndCreateRequest() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.show();

        String requestId = requestRef.push().getKey();
        StorageReference imgRef = storageRef.child(uid + "/" + requestId + ".jpg");

        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    dialog.dismiss();
                    saveRequest(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRequest(String imageUrl) {
        String desc = edtDescription.getText().toString();
        String type = edtType.getText().toString();
        String place = edtPlace.getText().toString();

        String requestId = requestRef.push().getKey();
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("description", desc);
        requestData.put("requestType", type);
        requestData.put("place", place);
        requestData.put("latitude", selectedLatLng.latitude);
        requestData.put("longitude", selectedLatLng.longitude);
        requestData.put("imageUrl", imageUrl);
        requestData.put("timestamp", ServerValue.TIMESTAMP);

        requestRef.child(requestId).setValue(requestData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request submitted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Default"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 13f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
