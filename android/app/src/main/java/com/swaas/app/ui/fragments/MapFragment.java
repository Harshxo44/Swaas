package com.swaas.app.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.swaas.app.databinding.FragmentMapBinding;
import com.swaas.app.model.WaterBody;
import com.swaas.app.utils.Constants;
import com.swaas.app.utils.MarkerColorHelper;
import com.swaas.app.viewmodel.MapViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.List;

/**
 * MapFragment — Shows OpenStreetMap via OSMdroid with colored water body markers.
 * Green = Safe, Yellow = Moderate, Red = Unsafe.
 * Completely FREE — no API key required.
 */
public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapViewModel mapViewModel;
    private MapView osmMapView;
    private FusedLocationProviderClient fusedLocationClient;
    private MyLocationNewOverlay locationOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialise OSMdroid configuration (required)
        Configuration.getInstance().setUserAgentValue(
                requireContext().getPackageName());

        osmMapView = binding.osmMapView;
        osmMapView.setTileSource(TileSourceFactory.MAPNIK); // OpenStreetMap tiles
        osmMapView.setMultiTouchControls(true);
        osmMapView.getController().setZoom(Constants.DEFAULT_MAP_ZOOM);

        // Disable OSMdroid copyright overlap (shows attribution bottom-left by default)
        setupOverlays();
        moveToCurrentLocation();
        setupObservers();

        mapViewModel.loadAllWaterBodies();
    }

    private void setupOverlays() {
        // My Location overlay (blue dot)
        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), osmMapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        osmMapView.getOverlays().add(locationOverlay);

        // Compass overlay
        CompassOverlay compassOverlay = new CompassOverlay(
                requireContext(), osmMapView);
        compassOverlay.enableCompass();
        osmMapView.getOverlays().add(compassOverlay);
    }

    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint start = new GeoPoint(location.getLatitude(), location.getLongitude());
                osmMapView.getController().animateTo(start);
                osmMapView.getController().setZoom(Constants.DEFAULT_MAP_ZOOM);
            }
        });
    }

    private void setupObservers() {
        mapViewModel.waterBodies.observe(getViewLifecycleOwner(), this::plotMarkers);

        mapViewModel.isLoading.observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        mapViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.tvOfflineNotice.setVisibility(View.VISIBLE);
                binding.tvOfflineNotice.setText("⚠️ Offline mode: showing cached data");
            }
        });
    }

    private void plotMarkers(List<WaterBody> waterBodies) {
        if (osmMapView == null || waterBodies == null) return;

        // Remove old markers (keep location + compass overlays)
        osmMapView.getOverlays().removeIf(o -> o instanceof Marker);

        for (WaterBody wb : waterBodies) {
            Marker marker = new Marker(osmMapView);
            marker.setPosition(new GeoPoint(wb.getLatitude(), wb.getLongitude()));
            marker.setTitle(wb.getName());
            marker.setSubDescription(wb.getSafetyLevel() + " — Score: " + (int) wb.getSafetyScore());

            // Color-coded marker icon
            Drawable markerIcon = MarkerColorHelper.getOsmMarkerIcon(
                    requireContext(), wb.getSafetyLevel());
            marker.setIcon(markerIcon);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Tap → WaterBodyDetailBottomSheet
            marker.setOnMarkerClickListener((m, map) -> {
                mapViewModel.selectWaterBody(wb);
                WaterBodyDetailBottomSheet sheet =
                        WaterBodyDetailBottomSheet.newInstance(wb);
                sheet.show(getParentFragmentManager(), "WaterBodyDetail");
                return true;
            });

            osmMapView.getOverlays().add(marker);
        }

        osmMapView.invalidate(); // Redraw map
    }

    @Override
    public void onResume() {
        super.onResume();
        if (osmMapView != null) osmMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (osmMapView != null) osmMapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (osmMapView != null) osmMapView.onDetach();
        binding = null;
    }
}
