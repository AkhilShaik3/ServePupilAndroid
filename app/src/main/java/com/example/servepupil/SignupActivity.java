package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.servepupil.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SignupActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button signupButton;
    TextView loginTextView;

    // Firebase Auth instance
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Signup Button Click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Create user in Firebase
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Signup success
                                        Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();

                                        // Optionally go to LoginActivity or Home
                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If signup fails
                                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        // Login Link Click
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
