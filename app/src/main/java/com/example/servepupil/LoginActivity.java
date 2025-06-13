package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView forgotPasswordTextView, signupTextView;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Bind views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signupTextView = findViewById(R.id.signupTextView);

        // Login button click
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                // Redirect based on email
                                if (email.equalsIgnoreCase("admin@gmail.com")) {
                                    Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Forgot Password click
        forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Signup click
        signupTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
