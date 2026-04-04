package com.swaas.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.swaas.app.databinding.ActivityLoginBinding;
import com.swaas.app.utils.ValidationUtils;
import com.swaas.app.viewmodel.AuthViewModel;

/**
 * LoginActivity — Email/password login using Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.currentUser.observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        authViewModel.authError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.isLoading.observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (ValidationUtils.isValidEmail(email)) {
                authViewModel.sendPasswordReset(email);
                Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
            } else {
                binding.tilEmail.setError("Enter a valid email first");
            }
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        boolean valid = true;
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (valid) authViewModel.login(email, password);
    }
}
