package com.trackiq.ClientPortalPro.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.trackiq.ClientPortalPro.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar back button
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSendResetLink.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.etResetEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etResetEmail.setError("Email is required.");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSendResetLink.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSendResetLink.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                        finish(); // Return to login screen
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
