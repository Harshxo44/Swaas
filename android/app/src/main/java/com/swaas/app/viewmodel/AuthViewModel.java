package com.swaas.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.swaas.app.model.User;
import com.swaas.app.repository.FirebaseAuthRepository;

/**
 * ViewModel handling authentication state.
 */
public class AuthViewModel extends AndroidViewModel {

    private final FirebaseAuthRepository authRepo;

    public final MutableLiveData<User> currentUser = new MutableLiveData<>();
    public final MutableLiveData<String> authError = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> passwordResetSent = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepo = new FirebaseAuthRepository();
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        authRepo.loginUser(email, password, result -> {
            isLoading.postValue(false);
            currentUser.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            authError.postValue(error);
        });
    }

    public void register(String email, String password, String name, String role) {
        isLoading.setValue(true);
        authRepo.registerUser(email, password, name, role, result -> {
            isLoading.postValue(false);
            currentUser.postValue(result);
        }, error -> {
            isLoading.postValue(false);
            authError.postValue(error);
        });
    }

    public void sendPasswordReset(String email) {
        authRepo.sendPasswordReset(email,
                success -> passwordResetSent.postValue(true),
                error -> authError.postValue(error));
    }

    public void logout() {
        authRepo.logout();
        currentUser.setValue(null);
    }

    public boolean isLoggedIn() {
        return authRepo.isLoggedIn();
    }

    public void loadCurrentUser() {
        if (authRepo.getCurrentFirebaseUser() != null) {
            authRepo.fetchUserProfile(
                    authRepo.getCurrentFirebaseUser().getUid(),
                    result -> currentUser.postValue(result),
                    error -> authError.postValue(error));
        }
    }
}
