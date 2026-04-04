package com.swaas.app.utils;

/**
 * Returns human-readable precaution text based on water safety level.
 */
public class PrecautionHelper {

    public static String getPrecaution(String safetyLevel) {
        if (safetyLevel == null) return getDefaultPrecaution();
        switch (safetyLevel) {
            case "Safe":
                return "✅ This water is safe for drinking and daily household use. "
                        + "Regular monitoring is still recommended.";
            case "Moderate":
                return "⚠️ Water quality is marginal. Boil water for at least 1 minute before drinking. "
                        + "Consider using a certified water filter. Not suitable for infants.";
            case "Unsafe":
                return "🚫 DANGER: Do NOT use this water for drinking, cooking, or bathing. "
                        + "Contamination detected. Report to local authorities immediately.";
            default:
                return getDefaultPrecaution();
        }
    }

    public static String getShortPrecaution(String safetyLevel) {
        if (safetyLevel == null) return "Status unknown";
        switch (safetyLevel) {
            case "Safe":     return "Suitable for drinking and daily use";
            case "Moderate": return "Boil before use / filtration recommended";
            case "Unsafe":   return "Do NOT use. Risk of contamination";
            default:         return "Status unknown";
        }
    }

    private static String getDefaultPrecaution() {
        return "⚠️ Safety status unknown. Avoid using this water until tested.";
    }
}
