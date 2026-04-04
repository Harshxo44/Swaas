package com.swaas.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.swaas.app.db.WaterBodyEntity;
import com.swaas.app.model.WaterBody;
import com.swaas.app.repository.WaterBodyRepository;

import java.util.List;

/**
 * ViewModel for search functionality and contributor data submission.
 */
public class ContributorViewModel extends AndroidViewModel {

    private final WaterBodyRepository waterBodyRepo;

    public final MutableLiveData<WaterBody> addedWaterBody = new MutableLiveData<>();
    public final MutableLiveData<WaterBody> updatedWaterBody = new MutableLiveData<>();
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ContributorViewModel(@NonNull Application application) {
        super(application);
        this.waterBodyRepo = new WaterBodyRepository(application);
    }

    public void addWaterBody(WaterBody waterBody) {
        isLoading.setValue(true);
        waterBodyRepo.addWaterBody(waterBody, result -> {
            isLoading.postValue(false);
            addedWaterBody.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            errorMessage.postValue(error);
        });
    }

    public void updateWaterBody(String id, WaterBody waterBody) {
        isLoading.setValue(true);
        waterBodyRepo.updateWaterBody(id, waterBody, result -> {
            isLoading.postValue(false);
            updatedWaterBody.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            errorMessage.postValue(error);
        });
    }

    public LiveData<List<WaterBodyEntity>> searchOffline(String query) {
        return waterBodyRepo.searchCached(query);
    }

    public LiveData<List<WaterBodyEntity>> getCachedWaterBodies() {
        return waterBodyRepo.getCachedWaterBodies();
    }
}
