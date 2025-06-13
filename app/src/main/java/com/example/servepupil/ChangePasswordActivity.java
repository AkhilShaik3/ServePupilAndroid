package com.example.servepupil;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {


    EditText emailEditText;
    Button resetButton;
    FirebaseAuth mAuth;
    ImageView logoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        emailEditText = findViewById(R.id.editTextEmail);
        resetButton = findViewById(R.id.buttonReset);
        logoImage = findViewById(R.id.logoImage);
        mAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Reset link sent to your email.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}