package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoogleAuthRepository {
    private static final String TAG = "GoogleAuthRepository";
    private static final int RC_SIGN_IN = 9001;

    private final Context appContext;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private final FitnessOptions fitnessOptions;

    private final MutableLiveData<GoogleSignInAccount> googleAccount = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> firebaseUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSignedIn = new MutableLiveData<>(false);

    @Inject
    public GoogleAuthRepository(Context appContext, GoogleFitRepository googleFitRepository) {
        this.appContext = appContext;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.fitnessOptions = googleFitRepository.getFitnessOptions();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(appContext.getString(hcmute.edu.vn.loclinhvabao.carex.R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .addExtension(fitnessOptions)
                .build();

        this.googleSignInClient = GoogleSignIn.getClient(appContext, gso);

        // Initialize current user state
        initializeCurrentUser();
    }

    private void initializeCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseUser.setValue(currentUser);
            isSignedIn.setValue(true);
            
            // Also check for Google account
            GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(appContext);
            if (lastSignedInAccount != null) {
                googleAccount.setValue(lastSignedInAccount);
            }
        } else {
            isSignedIn.setValue(false);
        }
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Intent data) {
        isLoading.setValue(true);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "Google sign in succeeded: " + account.getEmail());
            
            // Store Google account
            googleAccount.setValue(account);
            
            // Authenticate with Firebase
            firebaseAuthWithGoogle(account);
            
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            isLoading.setValue(false);
            handleSignInError(e);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        firebaseUser.setValue(user);
                        isSignedIn.setValue(true);
                        errorMessage.setValue(null);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        errorMessage.setValue("Authentication failed: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        isSignedIn.setValue(false);
                    }
                });
    }

    public void signOut() {
        isLoading.setValue(true);
        
        // Sign out from Firebase
        firebaseAuth.signOut();
        
        // Sign out from Google
        googleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    googleAccount.setValue(null);
                    firebaseUser.setValue(null);
                    isSignedIn.setValue(false);
                    errorMessage.setValue(null);
                    Log.d(TAG, "User signed out successfully");
                });
    }

    public void revokeAccess() {
        isLoading.setValue(true);
        
        // Revoke access and sign out
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    googleAccount.setValue(null);
                    firebaseUser.setValue(null);
                    isSignedIn.setValue(false);
                    errorMessage.setValue(null);
                    Log.d(TAG, "User access revoked successfully");
                });
    }

    public boolean hasGoogleFitPermissions() {
        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(appContext);
        return lastSignedInAccount != null && 
               GoogleSignIn.hasPermissions(lastSignedInAccount, fitnessOptions);
    }

    public void requestGoogleFitPermissions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .addExtension(fitnessOptions)
                .build();
        
        GoogleSignInClient client = GoogleSignIn.getClient(appContext, gso);
        // This should be called from an Activity context to request permissions
        // The Activity should handle the result in onActivityResult
    }

    private void handleSignInError(ApiException e) {
        String errorMsg;
        switch (e.getStatusCode()) {
            case 12501: // SIGN_IN_CANCELLED
                errorMsg = "Sign in was cancelled";
                break;
            case 12502: // SIGN_IN_FAILED
                errorMsg = "Sign in failed. Please try again";
                break;
            case 12500: // SIGN_IN_REQUIRED
                errorMsg = "Sign in is required";
                break;
            default:
                errorMsg = "Sign in failed: " + e.getMessage();
                break;
        }
        errorMessage.setValue(errorMsg);
        Log.e(TAG, "Sign in error: " + errorMsg, e);
    }

    // LiveData getters
    public LiveData<GoogleSignInAccount> getGoogleAccount() {
        return googleAccount;
    }

    public LiveData<FirebaseUser> getFirebaseUser() {
        return firebaseUser;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isSignedIn() {
        return isSignedIn;
    }

    // Utility methods
    public GoogleSignInAccount getCurrentGoogleAccount() {
        return googleAccount.getValue();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseUser.getValue();
    }

    public boolean isUserSignedIn() {
        return Boolean.TRUE.equals(isSignedIn.getValue());
    }

    public String getUserDisplayName() {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null) {
            return user.getDisplayName();
        }
        GoogleSignInAccount account = getCurrentGoogleAccount();
        if (account != null) {
            return account.getDisplayName();
        }
        return null;
    }

    public String getUserEmail() {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null) {
            return user.getEmail();
        }
        GoogleSignInAccount account = getCurrentGoogleAccount();
        if (account != null) {
            return account.getEmail();
        }
        return null;
    }

    public String getUserPhotoUrl() {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user != null && user.getPhotoUrl() != null) {
            return user.getPhotoUrl().toString();
        }
        GoogleSignInAccount account = getCurrentGoogleAccount();
        if (account != null && account.getPhotoUrl() != null) {
            return account.getPhotoUrl().toString();
        }
        return null;
    }
}
