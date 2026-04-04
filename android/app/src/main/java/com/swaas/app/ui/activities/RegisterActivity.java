package com.swaas.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.swaas.app.databinding.ActivityRegisterBinding;
import com.swaas.app.model.User;
import com.swaas.app.utils.ValidationUtils;
import com.swaas.app.viewmodel.AuthViewModel;

/**
 * RegisterActivity — new user onboarding with role selection.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
        authViewModel.isLoading.observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!loading);
        });
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        // Determine role (Enforced as normal user by default)
        String role = User.ROLE_USER;

        boolean valid = true;
        if (!ValidationUtils.isRequired(name)) {
            binding.tilName.setError("Full name is required");
            valid = false;
        } else binding.tilName.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Enter a valid email address");
            valid = false;
        } else binding.tilEmail.setError(null);

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else binding.tilPassword.setError(null);

        if (valid) authViewModel.register(email, password, name, role);
    }
}
