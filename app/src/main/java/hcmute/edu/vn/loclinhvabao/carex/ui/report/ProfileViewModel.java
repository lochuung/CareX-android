package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaGoal;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.UserProfileRepository;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final UserProfileRepository userProfileRepository;

    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;

        // Default user ID (in a real app, get this from Firebase Auth or similar)
        String userId = "current_user";
        loadUserProfile(userId);
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

    public void loadUserProfile(String userId) {
        isLoading.setValue(true);

        // Observe the LiveData from repository
        LiveData<UserProfile> profileLiveData = userProfileRepository.getUserProfile(userId);
        if (profileLiveData.getValue() == null) {
            // Create default profile if not exists
            userProfile.setValue(userProfileRepository.createDefaultProfile(userId, "Default User"));
            isLoading.setValue(false);
        } else {
            // Set the observed value
            userProfile.setValue(profileLiveData.getValue());
            isLoading.setValue(false);
        }
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
}