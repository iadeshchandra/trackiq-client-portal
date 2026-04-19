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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.trackiq.ClientPortalPro.auth.LoginActivity;
import com.trackiq.ClientPortalPro.databinding.ActivityAdminBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    
    private Uri selectedPdfUri = null;

    // The modern, non-deprecated way to select files in Android
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

        // Status Update Setup
        binding.btnUpdateRecord.setOnClickListener(v -> pushUpdateToDatabase());
        
        // File Upload Setup
        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());
        binding.btnUploadFile.setOnClickListener(v -> uploadDocumentToFirebase());

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
            Toast.makeText(this, "Please fill all status fields.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminActivity.this, "Status successfully updated!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdateRecord.setEnabled(true);
                    binding.btnUpdateRecord.setText("Publish Update");
                    Toast.makeText(AdminActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select PDF"));
    }

    private void uploadDocumentToFirebase() {
        String targetUid = binding.etDocTargetUid.getText().toString().trim();
        String docTitle = binding.etDocTitle.getText().toString().trim();

        if (TextUtils.isEmpty(targetUid) || TextUtils.isEmpty(docTitle)) {
            Toast.makeText(this, "UID and Document Title are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please select a PDF file first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lock UI during upload
        binding.btnUploadFile.setEnabled(false);
        binding.uploadProgressBar.setVisibility(View.VISIBLE);
        binding.uploadProgressBar.setIndeterminate(true);

        // Create a unique filename in Firebase Storage: clients/{uid}/{timestamp}.pdf
        String fileName = System.currentTimeMillis() + ".pdf";
        StorageReference fileRef = storageRef.child("clients/" + targetUid + "/" + fileName);

        fileRef.putFile(selectedPdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully, now get the download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveDocumentMetadataToFirestore(targetUid, docTitle, downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    resetUploadUI();
                    Toast.makeText(AdminActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveDocumentMetadataToFirestore(String uid, String title, String fileUrl) {
        // Generate current date string
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> docData = new HashMap<>();
        docData.put("title", title);
        docData.put("date", currentDate);
        docData.put("url", fileUrl);

        db.collection("projects").document(uid).collection("documents")
                .add(docData)
                .addOnSuccessListener(documentReference -> {
                    resetUploadUI();
                    Toast.makeText(AdminActivity.this, "Document pushed to client successfully!", Toast.LENGTH_LONG).show();
                    
                    // Clear inputs
                    binding.etDocTargetUid.setText("");
                    binding.etDocTitle.setText("");
                    selectedPdfUri = null;
                    binding.tvSelectedFileName.setText("No file selected");
                    binding.tvSelectedFileName.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                })
                .addOnFailureListener(e -> {
                    resetUploadUI();
                    Toast.makeText(AdminActivity.this, "Error linking document: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void resetUploadUI() {
        binding.btnUploadFile.setEnabled(true);
        binding.uploadProgressBar.setVisibility(View.GONE);
    }
}
