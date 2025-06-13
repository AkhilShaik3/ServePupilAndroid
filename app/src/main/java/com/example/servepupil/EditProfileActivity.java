package com.example.servepupil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editBio, editPhone;
    ImageView profileImage;
    Button saveProfileBtn;

    Uri selectedImageUri;
    String imageUrl;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    StorageReference storageRef;
    String uid;

    private static final int PICK_IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editBio = findViewById(R.id.editBio);
        editPhone = findViewById(R.id.editPhone);
        profileImage = findViewById(R.id.profileImage);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + uid + ".jpg");

        // Get data from intent
        Intent intent = getIntent();
        editName.setText(intent.getStringExtra("name"));
        editBio.setText(intent.getStringExtra("bio"));
        editPhone.setText(intent.getStringExtra("phone"));
        imageUrl = intent.getStringExtra("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(profileImage);
        }

        profileImage.setOnClickListener(view -> openImagePicker());

        saveProfileBtn.setOnClickListener(view -> {
            if (selectedImageUri != null) {
                uploadImageAndSave();
            } else {
                saveProfile(imageUrl);
            }
        });
    }

    private void openImagePicker() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pick, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(profileImage);
        }
    }

    private void uploadImageAndSave() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    saveProfile(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile(String finalImageUrl) {
        String name = editName.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("bio", bio);
        updateData.put("phone", phone);
        updateData.put("imageUrl", finalImageUrl);

        userRef.updateChildren(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Set success result
                    finish();             // Finish and go back to ProfileViewActivity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
