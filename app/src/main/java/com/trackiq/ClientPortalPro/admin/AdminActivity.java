package com.trackiq.ClientPortalPro.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.trackiq.ClientPortalPro.auth.LoginActivity;
import com.trackiq.ClientPortalPro.databinding.ActivityAdminBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    
    private Uri selectedPdfUri = null;

    // Modern file picker for PDFs
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedPdfUri = result.getData().getData();
                    binding.tvSelectedFileName.setText("PDF Ready to Upload");
                    binding.tvSelectedFileName.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    binding.btnUploadFile.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Timeline Update Listener
        binding.btnUpdateRecord.setOnClickListener(v -> pushTimelineUpdate());
        
        // Document Upload Listeners
        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());
        binding.btnUploadFile.setOnClickListener(v -> uploadDocumentToFirebase());

        // Sovereign Sign Out
        binding.btnAdminLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * PHASE 8 LOGIC: Converts comma-separated text into a Dynamic Timeline
     */
    private void pushTimelineUpdate() {
        String uid = binding.etTargetUid.getText().toString().trim();
        String projectName = binding.etProjectName.getText().toString().trim();
        String milestoneRaw = binding.etMilestoneList.getText().toString().trim();
        String stepStr = binding.etCurrentStep.getText().toString().trim();

        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(projectName) || TextUtils.isEmpty(milestoneRaw) || TextUtils.isEmpty(stepStr)) {
            Toast.makeText(this, "Please fill all timeline fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert raw string to List of Milestones
        String[] stepsArray = milestoneRaw.split(",");
        List<String> stepsList = new ArrayList<>();
        for (String s : stepsArray) {
            if (!s.trim().isEmpty()) {
                stepsList.add(s.trim());
            }
        }

        int currentStep = Integer.parseInt(stepStr);

        binding.btnUpdateRecord.setEnabled(false);
        binding.btnUpdateRecord.setText("Publishing...");

        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectName", projectName);
        projectData.put("milestones", stepsList);
        projectData.put("currentStepIndex", currentStep);

        db.collection("projects").document(uid)
                .set(projectData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    binding.btnUpdateRecord.setEnabled(true);
                    binding.btnUpdateRecord.setText("Publish Timeline");
                    Toast.makeText(this, "Client Timeline Updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdateRecord.setEnabled(true);
                    binding.btnUpdateRecord.setText("Publish Timeline");
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * PHASE 6 LOGIC: Secure PDF Management
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Project PDF"));
    }

    private void uploadDocumentToFirebase() {
        String targetUid = binding.etDocTargetUid.getText().toString().trim();
        String docTitle = binding.etDocTitle.getText().toString().trim();

        if (TextUtils.isEmpty(targetUid) || TextUtils.isEmpty(docTitle) || selectedPdfUri == null) {
            Toast.makeText(this, "Target UID, Title, and File are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnUploadFile.setEnabled(false);
        binding.uploadProgressBar.setVisibility(View.VISIBLE);

        // Path: clients/{uid}/{timestamp}.pdf
        String fileName = System.currentTimeMillis() + ".pdf";
        StorageReference fileRef = storageRef.child("clients/" + targetUid + "/" + fileName);

        fileRef.putFile(selectedPdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveDocumentMetadata(targetUid, docTitle, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    resetUploadUI();
                    Toast.makeText(this, "Storage Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveDocumentMetadata(String uid, String title, String url) {
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> doc = new HashMap<>();
        doc.put("title", title);
        doc.put("date", date);
        doc.put("url", url);

        db.collection("projects").document(uid).collection("documents")
                .add(doc)
                .addOnSuccessListener(ref -> {
                    resetUploadUI();
                    Toast.makeText(this, "PDF Pushed to Client!", Toast.LENGTH_SHORT).show();
                    
                    // Cleanup upload fields
                    binding.etDocTargetUid.setText("");
                    binding.etDocTitle.setText("");
                    selectedPdfUri = null;
                    binding.tvSelectedFileName.setText("No file selected");
                })
                .addOnFailureListener(e -> {
                    resetUploadUI();
                    Toast.makeText(this, "Linking Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetUploadUI() {
        binding.btnUploadFile.setEnabled(true);
        binding.uploadProgressBar.setVisibility(View.GONE);
    }
}
