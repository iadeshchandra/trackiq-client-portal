package com.trackiq.ClientPortalPro.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.trackiq.ClientPortalPro.admin.AdminActivity;
import com.trackiq.ClientPortalPro.dashboard.DashboardActivity;
import com.trackiq.ClientPortalPro.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    
    // Set your sovereign admin email here
    private static final String ADMIN_EMAIL = "admin@sanatanibandhan.com"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            routeUser(currentUser.getEmail());
        }
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required.");
            return;
        }

        setLoadingState(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        routeUser(user != null ? user.getEmail() : "");
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed. Check your credentials.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void routeUser(String email) {
        Intent intent;
        if (email != null && email.equalsIgnoreCase(ADMIN_EMAIL)) {
            // Route the leader to the control center
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else {
            // Route general users to their dashboard
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        startActivity(intent);
        finish(); 
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.etEmail.setEnabled(false);
            binding.etPassword.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.etEmail.setEnabled(true);
            binding.etPassword.setEnabled(true);
        }
    }
}
