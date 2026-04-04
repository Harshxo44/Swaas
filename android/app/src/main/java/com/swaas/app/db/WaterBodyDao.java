package com.swaas.app.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Room DAO for water body offline cache operations.
 */
@Dao
public interface WaterBodyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WaterBodyEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WaterBodyEntity> entities);

    @Update
    void update(WaterBodyEntity entity);

    @Query("SELECT * FROM water_bodies ORDER BY safetyScore ASC")
    LiveData<List<WaterBodyEntity>> getAllWaterBodies();

    @Query("SELECT * FROM water_bodies WHERE id = :id LIMIT 1")
    WaterBodyEntity getById(String id);

    @Query("SELECT * FROM water_bodies WHERE safetyLevel = :level ORDER BY name ASC")
    LiveData<List<WaterBodyEntity>> getByLevel(String level);

    @Query("SELECT * FROM water_bodies WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<WaterBodyEntity>> searchByName(String query);

    /**
     * Get water bodies within a rough bounding box (for offline nearby search).
     * Note: For accurate distance calc, filter further in code.
     */
    @Query("SELECT * FROM water_bodies WHERE " +
            "latitude BETWEEN :minLat AND :maxLat AND " +
            "longitude BETWEEN :minLon AND :maxLon")
    List<WaterBodyEntity> getNearby(double minLat, double maxLat, double minLon, double maxLon);

    @Query("DELETE FROM water_bodies WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM water_bodies")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM water_bodies")
    int getCount();
}
