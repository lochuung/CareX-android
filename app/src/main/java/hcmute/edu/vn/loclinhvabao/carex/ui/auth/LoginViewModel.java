package hcmute.edu.vn.loclinhvabao.carex.ui.auth;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.GoogleOauth2Repository;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final GoogleOauth2Repository authRepository;

    @Inject
    public LoginViewModel(GoogleOauth2Repository authRepository) {
        this.authRepository = authRepository;
    }

    public Intent getSignInIntent() {
        return authRepository.getSignInIntent();
    }

    public void handleSignInResult(Intent data) {
        authRepository.handleSignInResult(data);
    }

    public boolean isUserAlreadySignedIn() {
        return authRepository.isFullyAuthenticated();
    }

    public LiveData<Boolean> isLoading() {
        return authRepository.isLoading();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }

    public LiveData<Boolean> isFullyAuthenticated() {
        return authRepository.isFullyAuthenticatedLiveData();
    }

    public void signOut() {
        authRepository.signOut();
    }

    public String getCurrentUserId() {
        return authRepository.getCurrentUserId();
    }

    public String getCurrentUserName() {
        return authRepository.getUserDisplayName();
    }

    public String getCurrentUserEmail() {
        return authRepository.getUserEmail();
    }
}
