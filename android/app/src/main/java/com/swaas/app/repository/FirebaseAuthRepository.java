package com.swaas.app.repository;

import androidx.core.util.Consumer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swaas.app.model.User;
import com.swaas.app.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Firebase Authentication and user profile management in Firestore.
 */
public class FirebaseAuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public FirebaseAuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ----- Registration -----

    public void registerUser(String email, String password, String name, String role,
                             Consumer<User> onSuccess,
                             Consumer<String> onError) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        onError.accept("Registration failed: no user created.");
                        return;
                    }
                    String uid = firebaseUser.getUid();
                    User user = new User(uid, name, email, role);

                    // Save user profile in Firestore
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", uid);
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("role", role);

                    firestore.collection(Constants.COLLECTION_USERS)
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> onSuccess.accept(user))
                            .addOnFailureListener(e -> onError.accept(e.getMessage()));
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    // ----- Login -----

    public void loginUser(String email, String password,
                          Consumer<User> onSuccess,
                          Consumer<String> onError) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        onError.accept("Login failed.");
                        return;
                    }
                    fetchUserProfile(firebaseUser.getUid(), onSuccess, onError);
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    // ----- Fetch Profile -----

    public void fetchUserProfile(String uid,
                                  Consumer<User> onSuccess,
                                  Consumer<String> onError) {
        firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        onSuccess.accept(user);
                    } else {
                        onError.accept("User profile not found.");
                    }
                })
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    // ----- Helpers -----

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public void sendPasswordReset(String email,
                                   Consumer<Boolean> onSuccess,
                                   Consumer<String> onError) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> onSuccess.accept(true))
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    /**
     * Get current user's Firebase ID token (JWT) for backend API calls.
     */
    public void getIdToken(Consumer<String> onSuccess,
                            Consumer<String> onError) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            onError.accept("Not authenticated.");
            return;
        }
        user.getIdToken(true)
                .addOnSuccessListener(result -> onSuccess.accept("Bearer " + result.getToken()))
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}
