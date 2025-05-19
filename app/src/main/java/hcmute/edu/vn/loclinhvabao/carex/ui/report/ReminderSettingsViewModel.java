package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.UserProfileRepository;

@HiltViewModel
public class ReminderSettingsViewModel extends ViewModel {
    private final UserProfileRepository userProfileRepository;

    private final MutableLiveData<Boolean> reminderEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<String> reminderTime = new MutableLiveData<>("08:00");
    private final MutableLiveData<List<String>> reminderDays = new MutableLiveData<>(
            Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"));

    private final MutableLiveData<Boolean> soundEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> vibrationEnabled = new MutableLiveData<>(true);

    private final MutableLiveData<String> userId = new MutableLiveData<>("current_user");

    @Inject
    public ReminderSettingsViewModel(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;

        // Load current settings
        loadSettings("current_user");
    }

    public LiveData<Boolean> getReminderEnabled() {
        return reminderEnabled;
    }

    public LiveData<String> getReminderTime() {
        return reminderTime;
    }

    public LiveData<List<String>> getReminderDays() {
        return reminderDays;
    }

    public LiveData<Boolean> getSoundEnabled() {
        return soundEnabled;
    }

    public LiveData<Boolean> getVibrationEnabled() {
        return vibrationEnabled;
    }

    private void loadSettings(String userId) {
        this.userId.setValue(userId);

        LiveData<UserProfile> profileLiveData = userProfileRepository.getUserProfile(userId);
        UserProfile profile = profileLiveData.getValue();

        if (profile != null) {
            reminderEnabled.setValue(profile.isNotificationsEnabled());
            reminderTime.setValue(profile.getReminderTime());
            reminderDays.setValue(profile.getReminderDays());
        }

        // Load sound and vibration settings (would come from SharedPreferences in a real app)
    }

    public void saveSettings() {
        userProfileRepository.updateNotificationSettings(
                userId.getValue(),
                reminderEnabled.getValue(),
                reminderTime.getValue(),
                reminderDays.getValue()
        );

        // Save sound and vibration settings (would go to SharedPreferences in a real app)
    }

    public void setReminderEnabled(boolean enabled) {
        reminderEnabled.setValue(enabled);
    }

    public void setReminderTime(String time) {
        reminderTime.setValue(time);
    }

    public void setReminderDays(List<String> days) {
        reminderDays.setValue(days);
    }

    public void toggleDay(String day, boolean checked) {
        List<String> currentDays = new ArrayList<>(reminderDays.getValue());

        if (checked && !currentDays.contains(day)) {
            currentDays.add(day);
        } else if (!checked && currentDays.contains(day)) {
            currentDays.remove(day);
        }

        reminderDays.setValue(currentDays);
    }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled.setValue(enabled);
    }

    public void setVibrationEnabled(boolean enabled) {
        vibrationEnabled.setValue(enabled);
    }
}