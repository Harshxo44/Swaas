package com.swaas.app.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.swaas.app.R;
import com.swaas.app.utils.Constants;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapPickerDialog extends DialogFragment {

    public interface LocationPickerListener {
        void onLocationPicked(double latitude, double longitude);
    }

    private MapView map;
    private LocationPickerListener listener;
    private double initialLat;
    private double initialLng;

    public static MapPickerDialog newInstance(double lat, double lng, LocationPickerListener listener) {
        MapPickerDialog dialog = new MapPickerDialog();
        dialog.initialLat = lat;
        dialog.initialLng = lng;
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map_picker, container, false);

        map = view.findViewById(R.id.picker_map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        if (initialLat != 0 && initialLng != 0) {
            map.getController().setCenter(new GeoPoint(initialLat, initialLng));
        } else {
            // Default center if none provided (e.g., center of the country or 0,0)
            map.getController().setCenter(new GeoPoint(20.5937, 78.9629)); // India
        }

        view.findViewById(R.id.btn_cancel_picker).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_confirm_picker).setOnClickListener(v -> {
            GeoPoint center = (GeoPoint) map.getMapCenter();
            if (listener != null) {
                listener.onLocationPicked(center.getLatitude(), center.getLongitude());
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
