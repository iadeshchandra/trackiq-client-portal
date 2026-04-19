package com.trackiq.ClientPortalPro.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.trackiq.ClientPortalPro.auth.LoginActivity;
import com.trackiq.ClientPortalPro.databinding.ActivityAdminBinding;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding.btnUpdateRecord.setOnClickListener(v -> pushUpdateToDatabase());
        
        binding.btnAdminLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void pushUpdateToDatabase() {
        String uid = binding.etTargetUid.getText().toString().trim();
        String projectName = binding.etProjectName.getText().toString().trim();
        String phase = binding.etPhase.getText().toString().trim();
        String progressStr = binding.etProgress.getText().toString().trim();

        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(projectName) || TextUtils.isEmpty(phase) || TextUtils.isEmpty(progressStr)) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int progress = Integer.parseInt(progressStr);
        if (progress < 0 || progress > 100) {
            binding.etProgress.setError("Must be between 0 and 100");
            return;
        }

        binding.btnUpdateRecord.setEnabled(false);
        binding.btnUpdateRecord.setText("Updating...");

        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectName", projectName);
        projectData.put("currentPhase", phase);
        projectData.put("progressPercentage", progress);

        db.collection("projects").document(uid)
                .set(projectData)
                .addOnSuccessListener(aVoid -> {
                    binding.btnUpdateRecord.setEnabled(true);
                    binding.btnUpdateRecord.setText("Publish Update");
                    Toast.makeText(AdminActivity.this, "Record successfully updated!", Toast.LENGTH_LONG).show();
                    
                    // Clear fields after success
                    binding.etTargetUid.setText("");
                    binding.etProjectName.setText("");
                    binding.etPhase.setText("");
                    binding.etProgress.setText("");
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdateRecord.setEnabled(true);
                    binding.btnUpdateRecord.setText("Publish Update");
                    Toast.makeText(AdminActivity.this, "Error updating record: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
