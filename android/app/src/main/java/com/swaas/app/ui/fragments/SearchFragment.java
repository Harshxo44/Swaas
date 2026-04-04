package com.swaas.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.swaas.app.databinding.FragmentSearchBinding;
import com.swaas.app.db.WaterBodyEntity;
import com.swaas.app.ui.adapters.WaterBodyAdapter;
import com.swaas.app.viewmodel.ContributorViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchFragment — search and filter water bodies from the local Room cache.
 */
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ContributorViewModel viewModel;
    private WaterBodyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContributorViewModel.class);

        adapter = new WaterBodyAdapter(new ArrayList<>(), this::onWaterBodyClicked);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // Load all cached data initially
        viewModel.getCachedWaterBodies().observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                adapter.updateList(entities);
                binding.tvEmpty.setVisibility(entities.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // Search input listener
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.getCachedWaterBodies().observe(getViewLifecycleOwner(),
                            entities -> adapter.updateList(entities));
                } else {
                    viewModel.searchOffline(newText).observe(getViewLifecycleOwner(),
                            entities -> {
                                adapter.updateList(entities);
                                binding.tvEmpty.setVisibility(
                                        entities.isEmpty() ? View.VISIBLE : View.GONE);
                            });
                }
                return true;
            }
        });

        // Safety level filter chips
        binding.chipAll.setOnClickListener(v -> filterByLevel(null));
        binding.chipSafe.setOnClickListener(v -> filterByLevel("Safe"));
        binding.chipModerate.setOnClickListener(v -> filterByLevel("Moderate"));
        binding.chipUnsafe.setOnClickListener(v -> filterByLevel("Unsafe"));
    }

    private void filterByLevel(String level) {
        if (level == null) {
            viewModel.getCachedWaterBodies().observe(getViewLifecycleOwner(),
                    entities -> adapter.updateList(entities));
        } else {
            // Filter in-memory for simplicity
            List<WaterBodyEntity> current = adapter.getCurrentList();
            List<WaterBodyEntity> filtered = new ArrayList<>();
            for (WaterBodyEntity e : current) {
                if (level.equals(e.getSafetyLevel())) filtered.add(e);
            }
            adapter.updateList(filtered);
        }
    }

    private void onWaterBodyClicked(WaterBodyEntity entity) {
        WaterBodyDetailBottomSheet sheet =
                WaterBodyDetailBottomSheet.newInstance(entity.toWaterBody());
        sheet.show(getParentFragmentManager(), "Detail");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
