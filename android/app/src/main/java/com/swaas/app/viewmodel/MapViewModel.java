package com.swaas.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.swaas.app.model.WaterBody;
import com.swaas.app.repository.WaterBodyRepository;

import java.util.List;

/**
 * ViewModel for the map screen.
 * Fetches all or nearby water bodies and passes them to the Map Fragment.
 */
public class MapViewModel extends AndroidViewModel {

    private final WaterBodyRepository waterBodyRepo;

    public final MutableLiveData<List<WaterBody>> waterBodies = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<WaterBody> selectedWaterBody = new MutableLiveData<>();

    public MapViewModel(@NonNull Application application) {
        super(application);
        this.waterBodyRepo = new WaterBodyRepository(application);
    }

    public void loadAllWaterBodies() {
        isLoading.setValue(true);
        waterBodyRepo.fetchAllWaterBodies(result -> {
            isLoading.postValue(false);
            waterBodies.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            errorMessage.postValue(error);
        });
    }

    public void loadNearby(String token, double lat, double lng, double radiusKm) {
        isLoading.setValue(true);
        waterBodyRepo.fetchNearby(token, lat, lng, radiusKm, result -> {
            isLoading.postValue(false);
            waterBodies.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            errorMessage.postValue(error);
        });
    }

    public void selectWaterBody(WaterBody waterBody) {
        selectedWaterBody.setValue(waterBody);
    }
}
