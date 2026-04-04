package com.swaas.app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;
import com.swaas.app.db.AppDatabase;
import com.swaas.app.db.WaterBodyDao;
import com.swaas.app.db.WaterBodyEntity;
import com.swaas.app.model.WaterBody;
import com.swaas.app.network.ApiService;
import com.swaas.app.network.NetworkModule;
import com.swaas.app.utils.Constants;
import com.swaas.app.utils.WaterSafetyAnalyzer;
import com.swaas.app.model.SafetyResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single source of truth for water body data.
 * Fetches from Firestore (online) and falls back to Room DB (offline).
 */
public class WaterBodyRepository {

    private final FirebaseFirestore firestore;
    private final WaterBodyDao waterBodyDao;
    private final ApiService apiService;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public WaterBodyRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.waterBodyDao = AppDatabase.getInstance(context).waterBodyDao();
        this.apiService = NetworkModule.getApiService();
    }

    // ---- Fetch All (Firestore + cache to Room) ----

    public void fetchAllWaterBodies(Consumer<List<WaterBody>> onSuccess,
                                     Consumer<String> onError) {
        firestore.collection(Constants.COLLECTION_WATER_BODIES)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<WaterBody> list = new ArrayList<>();
                    List<WaterBodyEntity> entities = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        WaterBody wb = doc.toObject(WaterBody.class);
                        wb.setId(doc.getId());
                        list.add(wb);
                        entities.add(WaterBodyEntity.fromWaterBody(wb));
                    }
                    onSuccess.accept(list);
                    // Cache to Room on background thread
                    executor.execute(() -> waterBodyDao.insertAll(entities));
                })
                .addOnFailureListener(e -> {
                    onError.accept("Offline mode: " + e.getMessage());
                    // Fall back to Room cache
                    executor.execute(() -> {
                        List<WaterBodyEntity> cached = waterBodyDao.getNearby(
                                -90, 90, -180, 180); // All entries
                        List<WaterBody> offlineList = new ArrayList<>();
                        for (WaterBodyEntity entity : cached) offlineList.add(entity.toWaterBody());
                        onSuccess.accept(offlineList);
                    });
                });
    }

    // ---- Nearby water bodies via backend API ----

    public void fetchNearby(String token, double lat, double lng, double radiusKm,
                             Consumer<List<WaterBody>> onSuccess,
                             Consumer<String> onError) {
        apiService.getNearbyWaterBodies(token, lat, lng, radiusKm)
                .enqueue(new Callback<List<WaterBody>>() {
                    @Override
                    public void onResponse(Call<List<WaterBody>> call, Response<List<WaterBody>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            onSuccess.accept(response.body());
                        } else {
                            onError.accept("API error: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<WaterBody>> call, Throwable t) {
                        onError.accept("Network failure: " + t.getMessage());
                        // Offline fallback with bounding box
                        double delta = radiusKm / 111.0; // ~1 degree = 111 km
                        executor.execute(() -> {
                            List<WaterBodyEntity> cached = waterBodyDao.getNearby(
                                    lat - delta, lat + delta, lng - delta, lng + delta);
                            List<WaterBody> offlineList = new ArrayList<>();
                            for (WaterBodyEntity e : cached) offlineList.add(e.toWaterBody());
                            onSuccess.accept(offlineList);
                        });
                    }
                });
    }

    // ---- Add Water Body (Contributors) ----

    public void addWaterBody(WaterBody waterBody,
                              Consumer<WaterBody> onSuccess,
                              Consumer<String> onError) {
        // Run AI safety analysis before saving
        SafetyResult safety = WaterSafetyAnalyzer.analyze(
                waterBody.getPh(),
                waterBody.getTurbidity(),
                waterBody.getTds(),
                waterBody.getContaminants());
        waterBody.setSafetyScore(safety.getSafetyScore());
        waterBody.setSafetyLevel(safety.getSafetyLevel());
        waterBody.setLastUpdated(Timestamp.now());

        firestore.collection(Constants.COLLECTION_WATER_BODIES)
                .add(waterBody)
                .addOnSuccessListener(docRef -> {
                    waterBody.setId(docRef.getId());
                    onSuccess.accept(waterBody);
                    executor.execute(() ->
                            waterBodyDao.insert(WaterBodyEntity.fromWaterBody(waterBody)));
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    // ---- Update Water Body (Contributors) ----

    public void updateWaterBody(String id, WaterBody waterBody,
                                 Consumer<WaterBody> onSuccess,
                                 Consumer<String> onError) {
        // Re-run safety analysis on update
        SafetyResult safety = WaterSafetyAnalyzer.analyze(
                waterBody.getPh(),
                waterBody.getTurbidity(),
                waterBody.getTds(),
                waterBody.getContaminants());
        waterBody.setSafetyScore(safety.getSafetyScore());
        waterBody.setSafetyLevel(safety.getSafetyLevel());
        waterBody.setLastUpdated(Timestamp.now());

        firestore.collection(Constants.COLLECTION_WATER_BODIES)
                .document(id)
                .set(waterBody)
                .addOnSuccessListener(unused -> {
                    onSuccess.accept(waterBody);
                    executor.execute(() ->
                            waterBodyDao.insert(WaterBodyEntity.fromWaterBody(waterBody)));
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    // ---- Offline LiveData ----

    public LiveData<List<WaterBodyEntity>> getCachedWaterBodies() {
        return waterBodyDao.getAllWaterBodies();
    }

    public LiveData<List<WaterBodyEntity>> searchCached(String query) {
        return waterBodyDao.searchByName(query);
    }
}
