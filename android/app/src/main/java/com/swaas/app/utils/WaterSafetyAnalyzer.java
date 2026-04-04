package com.swaas.app.utils;

/**
 * AI-based water safety analyzer using WHO standard ranges and a weighted scoring system.
 *
 * WHO Drinking Water Standards (used for scoring):
 *  - pH:         6.5 – 8.5 (ideal: 7.0)
 *  - Turbidity:  < 1 NTU (ideal), max 5 NTU acceptable
 *  - TDS:        < 500 mg/L (max 1000 mg/L)
 *  - Contaminants: presence/absence flag
 *
 * Weights: pH (25%), Turbidity (30%), TDS (25%), Contaminants (20%)
 */
public class WaterSafetyAnalyzer {

    // WHO standard reference ranges
    private static final double PH_MIN = 6.5;
    private static final double PH_MAX = 8.5;
    private static final double PH_IDEAL = 7.0;

    private static final double TURBIDITY_IDEAL = 1.0;  // NTU
    private static final double TURBIDITY_MAX = 5.0;    // NTU

    private static final double TDS_IDEAL = 300.0;  // mg/L
    private static final double TDS_MAX = 500.0;    // mg/L (WHO)
    private static final double TDS_DANGER = 1000.0; // mg/L

    // Weights (must sum to 100)
    private static final double WEIGHT_PH = 25.0;
    private static final double WEIGHT_TURBIDITY = 30.0;
    private static final double WEIGHT_TDS = 25.0;
    private static final double WEIGHT_CONTAMINANTS = 20.0;

    /**
     * Analyzes water safety parameters and returns a SafetyResult.
     *
     * @param ph           pH value (0–14)
     * @param turbidityNTU Turbidity in NTU
     * @param tdsMgL       Total Dissolved Solids in mg/L
     * @param contaminants Contaminant info string (empty = none detected)
     * @return SafetyResult with score 0–100 and level
     */
    public static com.swaas.app.model.SafetyResult analyze(
            double ph, double turbidityNTU, double tdsMgL, String contaminants) {

        double phScore = calculatePhScore(ph);
        double turbidityScore = calculateTurbidityScore(turbidityNTU);
        double tdsScore = calculateTdsScore(tdsMgL);
        double contaminantScore = calculateContaminantScore(contaminants);

        double totalScore = (phScore * WEIGHT_PH / 100.0)
                + (turbidityScore * WEIGHT_TURBIDITY / 100.0)
                + (tdsScore * WEIGHT_TDS / 100.0)
                + (contaminantScore * WEIGHT_CONTAMINANTS / 100.0);

        String safetyLevel;
        String precaution;

        if (totalScore >= 80) {
            safetyLevel = com.swaas.app.model.SafetyResult.LEVEL_SAFE;
            precaution = "✅ Suitable for drinking and daily use.";
        } else if (totalScore >= 50) {
            safetyLevel = com.swaas.app.model.SafetyResult.LEVEL_MODERATE;
            precaution = "⚠️ Boil before use. Filtration recommended.";
        } else {
            safetyLevel = com.swaas.app.model.SafetyResult.LEVEL_UNSAFE;
            precaution = "🚫 Do NOT use. High risk of contamination.";
        }

        return new com.swaas.app.model.SafetyResult(
                Math.round(totalScore * 10.0) / 10.0,
                safetyLevel,
                precaution,
                phScore,
                turbidityScore,
                tdsScore,
                contaminantScore
        );
    }

    /** Score pH on a 0–100 scale based on WHO range 6.5–8.5. */
    private static double calculatePhScore(double ph) {
        if (ph < 0 || ph > 14) return 0;
        if (ph >= PH_MIN && ph <= PH_MAX) {
            // Full score within range; penalty grows as it moves away from ideal
            double deviation = Math.abs(ph - PH_IDEAL);
            double maxDeviation = (PH_MAX - PH_MIN) / 2.0; // 1.0
            return 100.0 - (deviation / maxDeviation) * 20.0; // Max 20% penalty within range
        }
        // Outside acceptable range — score drops steeply
        double distanceFromRange = (ph < PH_MIN) ? (PH_MIN - ph) : (ph - PH_MAX);
        return Math.max(0, 60.0 - distanceFromRange * 20.0);
    }

    /** Score turbidity on a 0–100 scale. Lower is better. */
    private static double calculateTurbidityScore(double ntu) {
        if (ntu < 0) return 0;
        if (ntu <= TURBIDITY_IDEAL) return 100.0;
        if (ntu <= TURBIDITY_MAX) {
            // Linear decline from 100 to 50 between 1 and 5 NTU
            return 100.0 - ((ntu - TURBIDITY_IDEAL) / (TURBIDITY_MAX - TURBIDITY_IDEAL)) * 50.0;
        }
        // Beyond 5 NTU — steep penalty
        return Math.max(0, 50.0 - (ntu - TURBIDITY_MAX) * 5.0);
    }

    /** Score TDS on a 0–100 scale (WHO max 500 mg/L). */
    private static double calculateTdsScore(double tds) {
        if (tds < 0) return 0;
        if (tds <= TDS_IDEAL) return 100.0;
        if (tds <= TDS_MAX) {
            return 100.0 - ((tds - TDS_IDEAL) / (TDS_MAX - TDS_IDEAL)) * 25.0;
        }
        if (tds <= TDS_DANGER) {
            return Math.max(0, 75.0 - ((tds - TDS_MAX) / (TDS_DANGER - TDS_MAX)) * 75.0);
        }
        return 0;
    }

    /** Score based on presence/absence of contaminant keywords. */
    private static double calculateContaminantScore(String contaminants) {
        if (contaminants == null || contaminants.trim().isEmpty()) return 100.0;
        String lower = contaminants.toLowerCase();
        // High-risk keywords
        String[] highRisk = {"arsenic", "lead", "mercury", "cyanide", "e.coli", "cholera",
                "pesticide", "nitrate", "fluoride", "bacteria"};
        // Moderate-risk keywords
        String[] moderateRisk = {"iron", "manganese", "chlorine", "sulfate", "sediment"};

        int highRiskCount = 0, moderateRiskCount = 0;
        for (String k : highRisk) if (lower.contains(k)) highRiskCount++;
        for (String k : moderateRisk) if (lower.contains(k)) moderateRiskCount++;

        if (highRiskCount > 0) return Math.max(0, 30.0 - highRiskCount * 10.0);
        if (moderateRiskCount > 0) return Math.max(0, 70.0 - moderateRiskCount * 15.0);
        // Non-empty but no known keyword — slight penalty
        return 80.0;
    }
}
