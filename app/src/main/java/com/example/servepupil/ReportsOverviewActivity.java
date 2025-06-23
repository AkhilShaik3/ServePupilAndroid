package com.example.servepupil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ReportsOverviewActivity extends AppCompatActivity {

    Button btnReportedRequests, btnReportedUsers, btnReportedComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_overview);

        btnReportedRequests = findViewById(R.id.btnReportedRequests);
        btnReportedUsers = findViewById(R.id.btnReportedUsers);
        btnReportedComments = findViewById(R.id.btnReportedComments);

        btnReportedRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportedRequestsActivity.class));
        });

        btnReportedUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportedUsersActivity.class));
        });

        btnReportedComments.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportedCommentsActivity.class));
        });
    }
}
