package hcmute.edu.vn.loclinhvabao.carex.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaGoal;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.GoogleOauth2Repository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.UserProfileRepository;
import hcmute.edu.vn.loclinhvabao.carex.util.Constants;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private final UserProfileRepository userProfileRepository;
    private final GoogleOauth2Repository authRepository;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);

    @Inject
    public SettingsViewModel(UserProfileRepository userProfileRepository,
                           GoogleOauth2Repository authRepository) {
        this.userProfileRepository = userProfileRepository;
        this.authRepository = authRepository;

        // Get current user ID from auth repository
        String userId = authRepository.getCurrentUserId();
        if (userId != null) {
            loadUserProfile(userId);
        } else {
            // No authenticated user, create default profile
            String defaultUserId = Constants.CURRENT_USER_ID;
            loadUserProfile(defaultUserId);
        }
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoggedOut() {
        return isLoggedOut;
    }

    public void loadUserProfile(String userId) {
        isLoading.setValue(true);

        // Observe the LiveData from repository
        LiveData<UserProfile> profileLiveData = userProfileRepository.getUserProfile(userId);

        // We need to observe the LiveData to get its value
        profileLiveData.observeForever(profile -> {
            if (profile == null) {
                // Create default profile if not exists
                userProfile.setValue(userProfileRepository.createDefaultProfile(userId, Constants.DEFAULT_USER_NAME));
            } else {
                // Set the observed value
                userProfile.setValue(profile);
            }
            isLoading.setValue(false);

            // Stop observing once we have the value
            profileLiveData.removeObserver(__ -> {});
        });
    }

    public void updateProfile(String name, float height, float weight, int age, YogaGoal goal) {
        UserProfile profile = userProfile.getValue();
        if (profile != null) {
            profile.setName(name);
            profile.setHeight(height);
            profile.setWeight(weight);
            profile.setAge(age);
            profile.setGoal(goal);

            userProfileRepository.updateProfile(profile);
            userProfile.setValue(profile);
        }
    }

    public void updateNotificationSettings(boolean enabled, String time, List<String> days) {
        UserProfile profile = userProfile.getValue();
        if (profile != null) {
            profile.setNotificationsEnabled(enabled);
            profile.setReminderTime(time);
            profile.setReminderDays(days);

            userProfileRepository.updateNotificationSettings(
                    profile.getId(), enabled, time, days);
            userProfile.setValue(profile);
        }
    }

    public void signOut() {
        isLoading.setValue(true);
        
        // Clear user data
        userProfileRepository.clearUserData();
        
        // Sign out from auth providers
        authRepository.signOut();
        
        // Clear current profile
        userProfile.setValue(null);
        
        isLoading.setValue(false);
        isLoggedOut.setValue(true);
    }

    public String getCurrentUserDisplayName() {
        return authRepository.getUserDisplayName();
    }

    public String getCurrentUserEmail() {
        return authRepository.getUserEmail();
    }

    public boolean isUserAuthenticated() {
        return authRepository.isFullyAuthenticated();
    }
}