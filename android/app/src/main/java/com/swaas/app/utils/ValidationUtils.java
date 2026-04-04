package com.swaas.app.utils;

import android.text.TextUtils;

/**
 * Input validation utilities for the SWAAS app.
 */
public class ValidationUtils {

    public static boolean isValidPh(double ph) {
        return ph >= 0.0 && ph <= 14.0;
    }

    public static boolean isValidPh(String phString) {
        if (TextUtils.isEmpty(phString)) return false;
        try {
            double ph = Double.parseDouble(phString);
            return isValidPh(ph);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidTurbidity(double turbidity) {
        return turbidity >= 0.0;
    }

    public static boolean isValidTds(double tds) {
        return tds >= 0.0;
    }

    public static boolean isRequired(String value) {
        return !TextUtils.isEmpty(value) && !value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidLatitude(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    public static boolean isValidLongitude(double lon) {
        return lon >= -180.0 && lon <= 180.0;
    }
}
