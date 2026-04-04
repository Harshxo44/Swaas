package com.swaas.app.ui.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.swaas.app.databinding.FragmentAddWaterBodyBinding;
import com.swaas.app.model.WaterBody;
import com.swaas.app.utils.ValidationUtils;
import com.swaas.app.viewmodel.ContributorViewModel;

/**
 * Fragment for Contributors to add new water body data.
 * Includes on-device safety analysis before submission.
 */
public class AddWaterBodyFragment extends Fragment {

    private FragmentAddWaterBodyBinding binding;
    private ContributorViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0, currentLng = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWaterBodyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContributorViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        fetchCurrentLocation();
        setupObservers();

        binding.btnSubmit.setOnClickListener(v -> attemptSubmit());
        binding.btnUseGps.setOnClickListener(v -> fetchCurrentLocation());
    }

    private void fetchCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    binding.etLatitude.setText(String.valueOf(currentLat));
                    binding.etLongitude.setText(String.valueOf(currentLng));
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        viewModel.addedWaterBody.observe(getViewLifecycleOwner(), wb -> {
            if (wb != null) {
                Toast.makeText(requireContext(),
                        "✅ Water body added successfully! Safety: " + wb.getSafetyLevel(),
                        Toast.LENGTH_LONG).show();
                clearForm();
            }
        });
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
    }

    private void attemptSubmit() {
        String name = binding.etName.getText().toString().trim();
        String phStr = binding.etPh.getText().toString().trim();
        String turbStr = binding.etTurbidity.getText().toString().trim();
        String tdsStr = binding.etTds.getText().toString().trim();
        String contaminants = binding.etContaminants.getText().toString().trim();
        String latStr = binding.etLatitude.getText().toString().trim();
        String lngStr = binding.etLongitude.getText().toString().trim();

        boolean valid = true;

        if (!ValidationUtils.isRequired(name)) {
            binding.tilName.setError("Water body name is required");
            valid = false;
        } else binding.tilName.setError(null);

        if (!ValidationUtils.isValidPh(phStr)) {
            binding.tilPh.setError("pH must be between 0 and 14");
            valid = false;
        } else binding.tilPh.setError(null);

        if (!ValidationUtils.isRequired(turbStr)) {
            binding.tilTurbidity.setError("Turbidity is required");
            valid = false;
        } else binding.tilTurbidity.setError(null);

        if (!ValidationUtils.isRequired(tdsStr)) {
            binding.tilTds.setError("TDS is required");
            valid = false;
        } else binding.tilTds.setError(null);

        if (!valid) return;

        double lat = latStr.isEmpty() ? currentLat : Double.parseDouble(latStr);
        double lng = lngStr.isEmpty() ? currentLng : Double.parseDouble(lngStr);

        WaterBody waterBody = new WaterBody();
        waterBody.setName(name);
        waterBody.setLatitude(lat);
        waterBody.setLongitude(lng);
        waterBody.setPh(Double.parseDouble(phStr));
        waterBody.setTurbidity(Double.parseDouble(turbStr));
        waterBody.setTds(Double.parseDouble(tdsStr));
        waterBody.setContaminants(contaminants);
        waterBody.setContributorId(FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "");
        waterBody.setLastUpdated(Timestamp.now());

        viewModel.addWaterBody(waterBody);
    }

    private void clearForm() {
        binding.etName.setText("");
        binding.etPh.setText("");
        binding.etTurbidity.setText("");
        binding.etTds.setText("");
        binding.etContaminants.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
