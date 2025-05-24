package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoogleOauth2Repository {
    private static final String TAG = "GoogleOauth2Repository";

    private final GoogleAuthRepository googleAuthRepository;
    private final GoogleFitRepository googleFitRepository;
    private final MediatorLiveData<Boolean> isFullyAuthenticated = new MediatorLiveData<>();

    @Inject
    public GoogleOauth2Repository(GoogleAuthRepository googleAuthRepository,
                                 GoogleFitRepository googleFitRepository) {
        this.googleAuthRepository = googleAuthRepository;
        this.googleFitRepository = googleFitRepository;

        setupFullAuthenticationObserver();
    }

    private void setupFullAuthenticationObserver() {
        isFullyAuthenticated.addSource(googleAuthRepository.isSignedIn(), isSignedIn -> {
            boolean hasGoogleFit = googleAuthRepository.hasGoogleFitPermissions();
            isFullyAuthenticated.setValue(isSignedIn && hasGoogleFit);
        });
    }

    /**
     * Get Google Sign-In intent
     */
    public Intent getSignInIntent() {
        return googleAuthRepository.getSignInIntent();
    }

    /**
     * Handle sign-in result from activity
     */
    public void handleSignInResult(Intent data) {
        googleAuthRepository.handleSignInResult(data);
    }

    /**
     * Sign out from both Google and Firebase
     */
    public void signOut() {
        googleFitRepository.signOut();
        googleAuthRepository.signOut();
    }

    /**
     * Revoke access completely
     */
    public void revokeAccess() {
        googleFitRepository.unsubscribeFromRealtimeUpdates();
        googleAuthRepository.revokeAccess();
    }

    /**
     * Check if user has both authentication and Google Fit permissions
     */
    public boolean isFullyAuthenticated() {
        return googleAuthRepository.isUserSignedIn() &&
               googleAuthRepository.hasGoogleFitPermissions();
    }

    /**
     * Request Google Fit permissions (if signed in but missing fit permissions)
     */
    public void requestGoogleFitPermissions() {
        if (googleAuthRepository.isUserSignedIn()) {
            googleAuthRepository.requestGoogleFitPermissions();
        } else {
            Log.w(TAG, "User must be signed in before requesting Google Fit permissions");
        }
    }

    // Delegate methods to GoogleAuthRepository
    public LiveData<GoogleSignInAccount> getGoogleAccount() {
        return googleAuthRepository.getGoogleAccount();
    }

    public LiveData<FirebaseUser> getFirebaseUser() {
        return googleAuthRepository.getFirebaseUser();
    }

    public LiveData<Boolean> isLoading() {
        return googleAuthRepository.isLoading();
    }

    public LiveData<String> getErrorMessage() {
        return googleAuthRepository.getErrorMessage();
    }

    public LiveData<Boolean> isSignedIn() {
        return googleAuthRepository.isSignedIn();
    }

    public LiveData<Boolean> isFullyAuthenticatedLiveData() {
        return isFullyAuthenticated;
    }

    // User info methods
    public String getUserDisplayName() {
        return googleAuthRepository.getUserDisplayName();
    }

    public String getUserEmail() {
        return googleAuthRepository.getUserEmail();
    }

    public String getUserPhotoUrl() {
        return googleAuthRepository.getUserPhotoUrl();
    }

    public GoogleSignInAccount getCurrentGoogleAccount() {
        return googleAuthRepository.getCurrentGoogleAccount();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return googleAuthRepository.getCurrentFirebaseUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null) {
            return user.getUid();
        }
        GoogleSignInAccount account = getCurrentGoogleAccount();
        if (account != null) {
            return account.getId();
        }
        return null;
    }
}
