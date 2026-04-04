package com.swaas.app.model;

/**
 * Represents a registered user in the SWAAS system.
 * Stored in Firestore under /users/{userId}
 */
public class User {

    private String userId;
    private String name;
    private String email;
    private String role;         // "user" | "contributor"
    private String fcmToken;     // Firebase Cloud Messaging token for push notifications

    // Required empty constructor for Firestore
    public User() {}

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static final String ROLE_USER = "user";
    public static final String ROLE_CONTRIBUTOR = "contributor";

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFcmToken() { return fcmToken; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public boolean isContributor() {
        return ROLE_CONTRIBUTOR.equals(role);
    }
}
