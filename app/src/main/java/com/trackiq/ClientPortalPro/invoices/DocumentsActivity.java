package com.trackiq.ClientPortalPro.invoices;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.trackiq.ClientPortalPro.databinding.ActivityDocumentsBinding;

import java.util.ArrayList;
import java.util.List;

public class DocumentsActivity extends AppCompatActivity {

    private static final String TAG = "DocumentsActivity";
    private ActivityDocumentsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentAdapter adapter;
    private List<Document> documentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar back button
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        documentList = new ArrayList<>();
        adapter = new DocumentAdapter(documentList);
        
        binding.rvDocuments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDocuments.setAdapter(adapter);

        loadDocuments();
    }

    private void loadDocuments() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Querying a sub-collection named "documents" inside the user's project document
        db.collection("projects").document(currentUser.getUid()).collection("documents")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Document doc = document.toObject(Document.class);
                            documentList.add(doc);
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (documentList.isEmpty()) {
                            binding.tvEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to load documents.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
