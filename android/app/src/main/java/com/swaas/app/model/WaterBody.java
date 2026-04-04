package com.swaas.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

/**
 * Represents a water body object stored in Firestore and cached in Room DB.
 */
public class WaterBody {

    @DocumentId
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double ph;
    private double turbidity;      // NTU units
    private double tds;            // mg/L
    private String contaminants;   // free-text or comma-separated list
    private double safetyScore;    // 0–100
    private String safetyLevel;    // "Safe" | "Moderate" | "Unsafe"
    private Timestamp lastUpdated;
    private String contributorId;  // UID of the contributor who last updated

    // Required empty constructor for Firestore deserialization
    public WaterBody() {}

    public WaterBody(String id, String name, double latitude, double longitude,
                     double ph, double turbidity, double tds, String contaminants,
                     double safetyScore, String safetyLevel, Timestamp lastUpdated,
                     String contributorId) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ph = ph;
        this.turbidity = turbidity;
        this.tds = tds;
        this.contaminants = contaminants;
        this.safetyScore = safetyScore;
        this.safetyLevel = safetyLevel;
        this.lastUpdated = lastUpdated;
        this.contributorId = contributorId;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getPh() { return ph; }
    public double getTurbidity() { return turbidity; }
    public double getTds() { return tds; }
    public String getContaminants() { return contaminants; }
    public double getSafetyScore() { return safetyScore; }
    public String getSafetyLevel() { return safetyLevel; }
    public Timestamp getLastUpdated() { return lastUpdated; }
    public String getContributorId() { return contributorId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPh(double ph) { this.ph = ph; }
    public void setTurbidity(double turbidity) { this.turbidity = turbidity; }
    public void setTds(double tds) { this.tds = tds; }
    public void setContaminants(String contaminants) { this.contaminants = contaminants; }
    public void setSafetyScore(double safetyScore) { this.safetyScore = safetyScore; }
    public void setSafetyLevel(String safetyLevel) { this.safetyLevel = safetyLevel; }
    public void setLastUpdated(Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setContributorId(String contributorId) { this.contributorId = contributorId; }
}
