package com.trackiq.ClientPortalPro.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.trackiq.ClientPortalPro.auth.LoginActivity;
import com.trackiq.ClientPortalPro.databinding.ActivityDashboardBinding;
import com.trackiq.ClientPortalPro.invoices.DocumentsActivity;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private ActivityDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupClickListeners();
        loadClientData();
    }

    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });

        binding.btnViewInvoices.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, DocumentsActivity.class));
        });
    }

    private void loadClientData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.tvWelcomeMessage.setText("Welcome back,\n" + currentUser.getEmail());

        DocumentReference docRef = db.collection("projects").document(currentUser.getUid());
        
        docRef.get().addOnCompleteListener(task -> {
            binding.dashboardLoading.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    
                    String projectName = document.getString("projectName");
                    String currentPhase = document.getString("currentPhase");
                    Long progressLong = document.getLong("progressPercentage");
                    
                    int progress = progressLong != null ? progressLong.intValue() : 0;
                    
                    binding.tvProjectName.setText(projectName != null ? projectName : "Unnamed Project");
                    binding.tvProjectPhase.setText(currentPhase != null ? currentPhase : "Planning");
                    binding.projectProgressBar.setProgress(progress);
                    
                } else {
                    Log.d(TAG, "No such document");
                    binding.tvProjectName.setText("No Active Projects");
                    binding.tvProjectPhase.setText("--");
                    binding.projectProgressBar.setProgress(0);
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
                Toast.makeText(this, "Failed to load project data. You may be offline.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
