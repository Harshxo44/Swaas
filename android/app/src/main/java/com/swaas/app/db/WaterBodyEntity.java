package com.swaas.app.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Room DB entity for offline caching of water body data.
 */
@Entity(tableName = "water_bodies")
public class WaterBodyEntity {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double ph;
    private double turbidity;
    private double tds;
    private String contaminants;
    private double safetyScore;
    private String safetyLevel;
    private long lastUpdatedMillis;  // Stored as Unix timestamp for Room compatibility
    private String contributorId;
    private long cachedAtMillis;     // When this entry was last cached locally

    public WaterBodyEntity() {}

    // Getters
    @NonNull public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getPh() { return ph; }
    public double getTurbidity() { return turbidity; }
    public double getTds() { return tds; }
    public String getContaminants() { return contaminants; }
    public double getSafetyScore() { return safetyScore; }
    public String getSafetyLevel() { return safetyLevel; }
    public long getLastUpdatedMillis() { return lastUpdatedMillis; }
    public String getContributorId() { return contributorId; }
    public long getCachedAtMillis() { return cachedAtMillis; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPh(double ph) { this.ph = ph; }
    public void setTurbidity(double turbidity) { this.turbidity = turbidity; }
    public void setTds(double tds) { this.tds = tds; }
    public void setContaminants(String contaminants) { this.contaminants = contaminants; }
    public void setSafetyScore(double safetyScore) { this.safetyScore = safetyScore; }
    public void setSafetyLevel(String safetyLevel) { this.safetyLevel = safetyLevel; }
    public void setLastUpdatedMillis(long millis) { this.lastUpdatedMillis = millis; }
    public void setContributorId(String contributorId) { this.contributorId = contributorId; }
    public void setCachedAtMillis(long millis) { this.cachedAtMillis = millis; }

    /** Convert from Firestore WaterBody model → Room Entity */
    public static WaterBodyEntity fromWaterBody(com.swaas.app.model.WaterBody wb) {
        WaterBodyEntity entity = new WaterBodyEntity();
        entity.setId(wb.getId());
        entity.setName(wb.getName());
        entity.setLatitude(wb.getLatitude());
        entity.setLongitude(wb.getLongitude());
        entity.setPh(wb.getPh());
        entity.setTurbidity(wb.getTurbidity());
        entity.setTds(wb.getTds());
        entity.setContaminants(wb.getContaminants());
        entity.setSafetyScore(wb.getSafetyScore());
        entity.setSafetyLevel(wb.getSafetyLevel());
        entity.setLastUpdatedMillis(wb.getLastUpdated() != null
                ? wb.getLastUpdated().toDate().getTime()
                : System.currentTimeMillis());
        entity.setContributorId(wb.getContributorId());
        entity.setCachedAtMillis(System.currentTimeMillis());
        return entity;
    }

    /** Convert from Room Entity → WaterBody model */
    public com.swaas.app.model.WaterBody toWaterBody() {
        com.swaas.app.model.WaterBody wb = new com.swaas.app.model.WaterBody();
        wb.setId(id);
        wb.setName(name);
        wb.setLatitude(latitude);
        wb.setLongitude(longitude);
        wb.setPh(ph);
        wb.setTurbidity(turbidity);
        wb.setTds(tds);
        wb.setContaminants(contaminants);
        wb.setSafetyScore(safetyScore);
        wb.setSafetyLevel(safetyLevel);
        wb.setLastUpdated(new com.google.firebase.Timestamp(
                new java.util.Date(lastUpdatedMillis)));
        wb.setContributorId(contributorId);
        return wb;
    }
}
