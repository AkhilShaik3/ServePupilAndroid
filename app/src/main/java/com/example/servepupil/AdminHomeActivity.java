package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    Button btnViewUsers, btnViewReports, btnViewRequests, btnLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAuth = FirebaseAuth.getInstance();

        btnViewUsers = findViewById(R.id.btnViewUsers);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnViewRequests = findViewById(R.id.btnViewRequests);
        btnLogout = findViewById(R.id.btnLogout);


        btnViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, UsersListActivity.class));
            }
        });

        btnViewReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, ReportsOverviewActivity.class));
            }
        });

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
