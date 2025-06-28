package com.example.servepupil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdminEditProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText editName, editBio, editPhone;
    private Button saveProfileBtn;

    private DatabaseReference userRef;
    private StorageReference storageRef;

    private String userId;
    private Uri selectedImageUri = null;
    private String currentImageUrl = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Show selected image immediately
                        Glide.with(AdminEditProfileActivity.this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.placeholder)
                                .into(profileImage);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_profile);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        editBio = findViewById(R.id.editBio);
        editPhone = findViewById(R.id.editPhone);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(userId + ".jpg");

        loadUserData();

        profileImage.setOnClickListener(v -> openImagePicker());

        saveProfileBtn.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdminEditProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                UserModel user = snapshot.getValue(UserModel.class);
                if (user != null) {
                    editName.setText(user.getName());
                    editBio.setText(user.getBio());
                    editPhone.setText(user.getPhone());

                    currentImageUrl = user.getImageUrl();
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        Glide.with(AdminEditProfileActivity.this)
                                .load(currentImageUrl)
                                .placeholder(R.drawable.placeholder)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminEditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Name required");
            editName.requestFocus();
            return;
        }

        // If image was changed, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveData(name, bio, phone);
        } else {
            // No image change, just update other fields
            updateUserData(name, bio, phone, currentImageUrl);
        }
    }

    private void uploadImageAndSaveData(String name, String bio, String phone) {
        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateUserData(name, bio, phone, downloadUrl);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserData(String name, String bio, String phone, String imageUrl) {
        userRef.child("name").setValue(name);
        userRef.child("bio").setValue(bio);
        userRef.child("phone").setValue(phone);
        userRef.child("imageUrl").setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AdminEditProfileActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AdminEditProfileActivity.this, "Update failed: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
