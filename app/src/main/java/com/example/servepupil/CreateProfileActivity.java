package com.example.servepupil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateProfileActivity extends AppCompatActivity {

    ImageView imageProfile;
    EditText inputName, inputBio, inputPhone;
    Button btnSubmit;

    Uri imageUri;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    StorageReference storageRef;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        imageProfile = findViewById(R.id.imageProfile);
        inputName = findViewById(R.id.inputName);
        inputBio = findViewById(R.id.inputBio);
        inputPhone = findViewById(R.id.inputPhone);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        imageProfile.setOnClickListener(v -> selectImage());

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadImageAndSaveProfile();
            }
        });
    }

    private boolean validateInputs() {
        if (inputName.getText().toString().isEmpty()
                || inputBio.getText().toString().isEmpty()
                || inputPhone.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).placeholder(R.drawable.placeholder).into(imageProfile);
        }
    }

    private void uploadImageAndSaveProfile() {
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imgRef = storageRef.child(imageName);

        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveProfile(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile(String imageUrl) {
        String uid = mAuth.getUid();
        String name = inputName.getText().toString();
        String bio = inputBio.getText().toString();
        String phone = inputPhone.getText().toString();

        // Empty maps for followers/following
        Map<String, Boolean> followers = new HashMap<>();
        Map<String, Boolean> following = new HashMap<>();

        // Create user model
        UserModel user = new UserModel(uid, name, bio, phone, imageUrl, followers, following, false);

        userRef.child(uid).setValue(user.toMap())
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfileActivity.this, "Profile Created", Toast.LENGTH_SHORT).show();
                    finish(); // You can also navigate to another activity here
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CreateProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
