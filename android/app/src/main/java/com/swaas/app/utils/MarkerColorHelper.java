package com.swaas.app.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

/**
 * Maps water safety level to marker colors for OSMdroid and UI components.
 * No Google Maps dependency — uses standard Android Drawables.
 */
public class MarkerColorHelper {

    /** Returns a colored circular Drawable for OSMdroid map markers. */
    public static Drawable getOsmMarkerIcon(Context context, String safetyLevel) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setSize(dp(context, 28), dp(context, 28));
        circle.setStroke(dp(context, 3), 0xFFFFFFFF); // White border

        switch (safetyLevel == null ? "" : safetyLevel) {
            case "Safe":
                circle.setColor(0xFF2E7D32); // Dark green
                break;
            case "Moderate":
                circle.setColor(0xFFF57F17); // Amber
                break;
            case "Unsafe":
                circle.setColor(0xFFC62828); // Dark red
                break;
            default:
                circle.setColor(0xFF607D8B); // Blue-grey (unknown)
        }
        return circle;
    }

    /** Returns the safety text color (for TextViews, badges, progress bars). */
    public static int getSafetyColor(String safetyLevel) {
        if (safetyLevel == null) return android.graphics.Color.GRAY;
        switch (safetyLevel) {
            case "Safe":     return 0xFF2E7D32;
            case "Moderate": return 0xFFF57F17;
            case "Unsafe":   return 0xFFC62828;
            default:         return android.graphics.Color.GRAY;
        }
    }

    /** Returns the soft background color for safety badges and card tints. */
    public static int getSafetyBackgroundColor(String safetyLevel) {
        if (safetyLevel == null) return 0xFFF5F5F5;
        switch (safetyLevel) {
            case "Safe":     return 0xFFE8F5E9;
            case "Moderate": return 0xFFFFF8E1;
            case "Unsafe":   return 0xFFFFEBEE;
            default:         return 0xFFF5F5F5;
        }
    }

    private static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
