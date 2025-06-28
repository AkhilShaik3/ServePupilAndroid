package com.example.servepupil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.*;

public class EditRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText edtDescription, edtType, edtPlace;
    private ListView placeSuggestions;
    private ImageView imagePreview;
    private Button btnUpdate;
    private GoogleMap mMap;
    private MapView mapView;
    private Uri imageUri;

    private PlacesClient placesClient;
    private ArrayAdapter<String> adapter;
    private List<AutocompletePrediction> predictionList;
    private LatLng selectedLatLng;

    private FirebaseAuth mAuth;
    private DatabaseReference requestRef;
    private StorageReference storageRef;
    private String uid, requestId;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int IMAGE_PICK_REQUEST = 101;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_request);

        edtDescription = findViewById(R.id.edtDescription);
        edtType = findViewById(R.id.edtType);
        edtPlace = findViewById(R.id.edtPlace);
        placeSuggestions = findViewById(R.id.placeSuggestions);
        imagePreview = findViewById(R.id.imagePreview);
        btnUpdate = findViewById(R.id.btnUpdate);
        mapView = findViewById(R.id.mapView);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestId = getIntent().getStringExtra("requestId");
        requestRef = FirebaseDatabase.getInstance().getReference("requests").child(uid).child(requestId);
        storageRef = FirebaseStorage.getInstance().getReference("request_images");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDMYh_Jl6tzJCMD1aDn2TmySEE8ZPbzeMk");
        }
        placesClient = Places.createClient(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        placeSuggestions.setAdapter(adapter);

        edtPlace.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(s.toString()).build();
                placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener(response -> {
                            predictionList = response.getAutocompletePredictions();
                            List<String> suggestions = new ArrayList<>();
                            for (AutocompletePrediction prediction : predictionList) {
                                suggestions.add(prediction.getFullText(null).toString());
                            }
                            adapter.clear();
                            adapter.addAll(suggestions);
                            placeSuggestions.setVisibility(View.VISIBLE);
                        });
            }
        });

        placeSuggestions.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = predictionList.get(position);
            edtPlace.setText(prediction.getFullText(null).toString());
            placeSuggestions.setVisibility(View.GONE);

            FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(
                    prediction.getPlaceId(),
                    Arrays.asList(Place.Field.LAT_LNG)).build();

            placesClient.fetchPlace(placeRequest)
                    .addOnSuccessListener(response -> {
                        selectedLatLng = response.getPlace().getLatLng();
                        if (mMap != null && selectedLatLng != null) {
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Selected"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                        }
                    });
        });

        imagePreview.setOnClickListener(v -> openImagePicker());

        btnUpdate.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndUpdateRequest();
            } else {
                updateRequest(currentImageUrl);
            }
        });

        mapView.onCreate(savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null);
        mapView.getMapAsync(this);

        loadRequestData();
    }

    private void loadRequestData() {
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String description = snapshot.child("description").getValue(String.class);
                String type = snapshot.child("requestType").getValue(String.class);
                String place = snapshot.child("place").getValue(String.class);
                currentImageUrl = snapshot.child("imageUrl").getValue(String.class);
                double lat = snapshot.child("latitude").getValue(Double.class);
                double lng = snapshot.child("longitude").getValue(Double.class);

                edtDescription.setText(description);
                edtType.setText(type);
                edtPlace.setText(place);

                selectedLatLng = new LatLng(lat, lng);
                if (mMap != null && selectedLatLng != null) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Current Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                }

                Glide.with(EditRequestActivity.this).load(currentImageUrl).into(imagePreview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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

    private void uploadImageAndUpdateRequest() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.show();

        StorageReference imgRef = storageRef.child(uid + "/" + requestId + ".jpg");
        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    dialog.dismiss();
                    updateRequest(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRequest(String imageUrl) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("description", edtDescription.getText().toString());
        updatedData.put("requestType", edtType.getText().toString());
        updatedData.put("place", edtPlace.getText().toString());
        updatedData.put("latitude", selectedLatLng.latitude);
        updatedData.put("longitude", selectedLatLng.longitude);
        updatedData.put("imageUrl", imageUrl);

        requestRef.updateChildren(updatedData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapView.getMapAsync(this);
        } else {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (selectedLatLng != null) {
            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
        }
    }
}
