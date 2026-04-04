package com.swaas.app.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.swaas.app.R;
import com.swaas.app.databinding.ActivityMainBinding;
import com.swaas.app.ui.fragments.AddWaterBodyFragment;
import com.swaas.app.ui.fragments.MapFragment;
import com.swaas.app.ui.fragments.SearchFragment;
import com.swaas.app.utils.Constants;
import com.swaas.app.viewmodel.AuthViewModel;

/**
 * MainActivity — hosts the bottom navigation with Map, Search, and Contribute fragments.
 */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.loadCurrentUser();

        requestLocationPermission();
        subscribeToFcmTopics();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new MapFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                fragment = new MapFragment();
            } else if (id == R.id.nav_search) {
                fragment = new SearchFragment();
            } else if (id == R.id.nav_contribute) {
                // Role check: only contributors can access this
                authViewModel.currentUser.observe(this, user -> {
                    if (user != null && user.isContributor()) {
                        loadFragment(new AddWaterBodyFragment());
                    } else {
                        android.widget.Toast.makeText(this,
                                "Access restricted to Data Contributors only.",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
            if (fragment != null) loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void subscribeToFcmTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC_ALL);
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC_ALERTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Reload map fragment to trigger location
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MapFragment()).commit();
            }
        }
    }
}
