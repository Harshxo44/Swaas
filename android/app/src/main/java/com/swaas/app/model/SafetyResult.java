package com.swaas.app.model;

/**
 * Represents the result of the AI-based water safety analysis.
 */
public class SafetyResult {

    public static final String LEVEL_SAFE = "Safe";
    public static final String LEVEL_MODERATE = "Moderate";
    public static final String LEVEL_UNSAFE = "Unsafe";

    private double safetyScore;    // 0–100
    private String safetyLevel;    // Safe | Moderate | Unsafe
    private String precaution;     // Human-readable precaution text
    private double phScore;
    private double turbidityScore;
    private double tdsScore;
    private double contaminantScore;

    public SafetyResult(double safetyScore, String safetyLevel, String precaution,
                        double phScore, double turbidityScore, double tdsScore,
                        double contaminantScore) {
        this.safetyScore = safetyScore;
        this.safetyLevel = safetyLevel;
        this.precaution = precaution;
        this.phScore = phScore;
        this.turbidityScore = turbidityScore;
        this.tdsScore = tdsScore;
        this.contaminantScore = contaminantScore;
    }

    // Getters
    public double getSafetyScore() { return safetyScore; }
    public String getSafetyLevel() { return safetyLevel; }
    public String getPrecaution() { return precaution; }
    public double getPhScore() { return phScore; }
    public double getTurbidityScore() { return turbidityScore; }
    public double getTdsScore() { return tdsScore; }
    public double getContaminantScore() { return contaminantScore; }

    public boolean isSafe() { return LEVEL_SAFE.equals(safetyLevel); }
    public boolean isModerate() { return LEVEL_MODERATE.equals(safetyLevel); }
    public boolean isUnsafe() { return LEVEL_UNSAFE.equals(safetyLevel); }
}
