package com.swaas.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.swaas.app.databinding.BottomSheetWaterBodyBinding;
import com.swaas.app.model.WaterBody;
import com.swaas.app.utils.MarkerColorHelper;
import com.swaas.app.utils.PrecautionHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Bottom sheet dialog showing full details of a water body.
 */
public class WaterBodyDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_WATER_BODY_ID = "water_body_id";
    private static final String ARG_NAME = "name";
    private static final String ARG_PH = "ph";
    private static final String ARG_TURBIDITY = "turbidity";
    private static final String ARG_TDS = "tds";
    private static final String ARG_CONTAMINANTS = "contaminants";
    private static final String ARG_SAFETY_SCORE = "safety_score";
    private static final String ARG_SAFETY_LEVEL = "safety_level";
    private static final String ARG_LAST_UPDATED = "last_updated";

    private BottomSheetWaterBodyBinding binding;

    public static WaterBodyDetailBottomSheet newInstance(WaterBody waterBody) {
        WaterBodyDetailBottomSheet sheet = new WaterBodyDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_WATER_BODY_ID, waterBody.getId());
        args.putString(ARG_NAME, waterBody.getName());
        args.putDouble(ARG_PH, waterBody.getPh());
        args.putDouble(ARG_TURBIDITY, waterBody.getTurbidity());
        args.putDouble(ARG_TDS, waterBody.getTds());
        args.putString(ARG_CONTAMINANTS, waterBody.getContaminants());
        args.putDouble(ARG_SAFETY_SCORE, waterBody.getSafetyScore());
        args.putString(ARG_SAFETY_LEVEL, waterBody.getSafetyLevel());
        if (waterBody.getLastUpdated() != null) {
            args.putLong(ARG_LAST_UPDATED, waterBody.getLastUpdated().toDate().getTime());
        }
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetWaterBodyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) return;

        String name = args.getString(ARG_NAME, "Unknown");
        double ph = args.getDouble(ARG_PH, 0);
        double turbidity = args.getDouble(ARG_TURBIDITY, 0);
        double tds = args.getDouble(ARG_TDS, 0);
        String contaminants = args.getString(ARG_CONTAMINANTS, "None detected");
        double safetyScore = args.getDouble(ARG_SAFETY_SCORE, 0);
        String safetyLevel = args.getString(ARG_SAFETY_LEVEL, "Unknown");
        long lastUpdatedMs = args.getLong(ARG_LAST_UPDATED, 0);

        binding.tvName.setText(name);
        binding.tvPh.setText(String.format(Locale.getDefault(), "pH: %.2f", ph));
        binding.tvTurbidity.setText(String.format(Locale.getDefault(), "Turbidity: %.1f NTU", turbidity));
        binding.tvTds.setText(String.format(Locale.getDefault(), "TDS: %.0f mg/L", tds));
        binding.tvContaminants.setText("Contaminants: " + (contaminants != null && !contaminants.isEmpty()
                ? contaminants : "None detected"));
        binding.tvSafetyScore.setText(String.format(Locale.getDefault(), "Safety Score: %.0f / 100", safetyScore));
        binding.tvSafetyLevel.setText(safetyLevel);
        binding.tvSafetyLevel.setTextColor(MarkerColorHelper.getSafetyColor(safetyLevel));
        binding.cardRoot.setCardBackgroundColor(MarkerColorHelper.getSafetyBackgroundColor(safetyLevel));
        binding.tvPrecaution.setText(PrecautionHelper.getPrecaution(safetyLevel));

        if (lastUpdatedMs > 0) {
            String formatted = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(new java.util.Date(lastUpdatedMs));
            binding.tvLastUpdated.setText("Last updated: " + formatted);
        }

        // Progress bar for safety score visualization
        binding.progressSafety.setProgress((int) safetyScore);
        int progressColor = MarkerColorHelper.getSafetyColor(safetyLevel);
        binding.progressSafety.setIndicatorColor(progressColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
