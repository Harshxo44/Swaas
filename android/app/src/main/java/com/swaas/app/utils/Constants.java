package com.swaas.app.utils;

/**
 * App-wide constants for the SWAAS application.
 */
public class Constants {

    // Firestore Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_WATER_BODIES = "waterBodies";

    // User Roles
    public static final String ROLE_USER = "user";
    public static final String ROLE_CONTRIBUTOR = "contributor";

    // Safety Level Strings
    public static final String LEVEL_SAFE = "Safe";
    public static final String LEVEL_MODERATE = "Moderate";
    public static final String LEVEL_UNSAFE = "Unsafe";

    // Safety Score Thresholds
    public static final double SAFE_THRESHOLD = 80.0;
    public static final double MODERATE_THRESHOLD = 50.0;

    // API Base URL — update with your deployed backend URL
    public static final String API_BASE_URL = "http://10.0.2.2:3000/api/";
    // Production: "https://your-swaas-backend.com/api/"

    // Shared Preferences Keys
    public static final String PREF_NAME = "swaas_prefs";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_ID = "user_id";

    // Map Defaults (OSMdroid uses double zoom levels)
    public static final double DEFAULT_MAP_ZOOM = 14.0;
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;

    // FCM Topics
    public static final String FCM_TOPIC_ALL = "all_users";
    public static final String FCM_TOPIC_ALERTS = "water_alerts";

    // Date Format
    public static final String DATE_FORMAT = "dd MMM yyyy, hh:mm a";

    // Room DB
    public static final String DB_NAME = "swaas_database";
    public static final int DB_VERSION = 1;
}
