package hcmute.edu.vn.loclinhvabao.carex.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.ui.auth.LoginActivity;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // UI Components - Profile Section
    private ShapeableImageView ivProfilePhoto;
    private TextView tvUserName, tvUserLevel, tvStreak;
    private MaterialButton btnEditProfile;
    private TextView tvHeight, tvWeight, tvBmi, tvAge, tvYogaGoal;

    // UI Components - Notification Settings
    private SwitchMaterial switchReminders;
    private TextView tvReminderTime, tvReminderDays;
    private LinearLayout llReminderTime, llReminderDays;

    // UI Components - App Settings
    private LinearLayout llThemeSetting, llLanguageSetting, llAboutSetting;
    private TextView tvThemeValue, tvLanguageValue;
    private MaterialButton btnSignOut;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void initUI(View view) {
        // Profile section
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserLevel = view.findViewById(R.id.tv_user_level);
        tvStreak = view.findViewById(R.id.tv_streak);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        // Personal info section
        tvHeight = view.findViewById(R.id.tv_height);
        tvWeight = view.findViewById(R.id.tv_weight);
        tvBmi = view.findViewById(R.id.tv_bmi);
        tvAge = view.findViewById(R.id.tv_age);
        tvYogaGoal = view.findViewById(R.id.tv_yoga_goal);

        // Reminder settings section
        switchReminders = view.findViewById(R.id.switch_reminders);
        tvReminderTime = view.findViewById(R.id.tv_reminder_time);
        tvReminderDays = view.findViewById(R.id.tv_reminder_days);
        llReminderTime = view.findViewById(R.id.ll_reminder_time);
        llReminderDays = view.findViewById(R.id.ll_reminder_days);

        // App settings section
        llThemeSetting = view.findViewById(R.id.ll_theme_setting);
        llLanguageSetting = view.findViewById(R.id.ll_language_setting);
        llAboutSetting = view.findViewById(R.id.ll_about_setting);
        tvThemeValue = view.findViewById(R.id.tv_theme_value);
        tvLanguageValue = view.findViewById(R.id.tv_language_value);

        // Sign out button
        btnSignOut = view.findViewById(R.id.btn_sign_out);
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (btnSignOut != null) {
                btnSignOut.setEnabled(!isLoading);
                btnSignOut.setText(isLoading ? "Signing out..." : "Sign Out");
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Show error message
            }
        });
        viewModel.getIsLoggedOut().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                navigateToLogin();
            }
        });
    }

    private void setupClickListeners() {
        // Edit profile button
        btnEditProfile.setOnClickListener(v -> {
            UserProfile profile = viewModel.getUserProfile().getValue();
            if (profile != null) {
                showEditProfileDialog(profile);
            }
        });

        // Reminder settings
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserProfile profile = viewModel.getUserProfile().getValue();
            if (profile != null) {
                viewModel.updateNotificationSettings(
                        isChecked,
                        profile.getReminderTime(),
                        profile.getReminderDays()
                );
            }
        });

        // Reminder settings
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserProfile profile = viewModel.getUserProfile().getValue();
            if (profile != null) {
                viewModel.updateNotificationSettings(
                        isChecked,
                        profile.getReminderTime(),
                        profile.getReminderDays()
                );
            }
        });

        // Reminder time - navigate to reminder settings
        llReminderTime.setOnClickListener(v -> {
            // TODO: Implement reminder settings navigation
        });

        // Reminder days - navigate to reminder settings
        llReminderDays.setOnClickListener(v -> {
            // TODO: Implement reminder settings navigation
        });

        // Theme setting
        llThemeSetting.setOnClickListener(v -> {
            // TODO: Show theme selection dialog
        });

        // Language setting
        llLanguageSetting.setOnClickListener(v -> {
            // TODO: Show language selection dialog
        });

        // About setting
        llAboutSetting.setOnClickListener(v -> {
            // TODO: Navigate to about screen
        });

        // Sign out button
        btnSignOut.setOnClickListener(v -> {
            viewModel.signOut();
        });
    }

    private void updateUI(UserProfile profile) {
        if (profile == null) return;

        // Use authenticated user name if available, otherwise use profile name
        String displayName = viewModel.getCurrentUserDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            tvUserName.setText(displayName);
        } else {
            tvUserName.setText(profile.getName());
        }

        // Show authentication status
        if (viewModel.isUserAuthenticated()) {
            String email = viewModel.getCurrentUserEmail();
            if (email != null) {
                tvUserLevel.setText(email);
            } else {
                tvUserLevel.setText("Authenticated User");
            }
        } else {
            tvUserLevel.setText("Local Profile");
        }

        tvStreak.setText(getString(R.string.current_streak, profile.getCurrentStreak()));

        // Update personal info
        tvHeight.setText(String.format("%.1f cm", profile.getHeight()));
        tvWeight.setText(String.format("%.1f kg", profile.getWeight()));

        // Calculate BMI
        float bmi = profile.getWeight() / ((profile.getHeight() / 100f) * (profile.getHeight() / 100f));
        String bmiCategory = getBmiCategory(bmi);
        tvBmi.setText(String.format("%.1f (%s)", bmi, bmiCategory));

        tvAge.setText(String.valueOf(profile.getAge()));
        tvYogaGoal.setText(profile.getGoal().getDisplayName());

        // Update reminder settings
        switchReminders.setChecked(profile.isNotificationsEnabled());
        tvReminderTime.setText(formatTime(profile.getReminderTime()));
        tvReminderDays.setText(formatDays(profile.getReminderDays()));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String getBmiCategory(float bmi) {
        if (bmi < 18.5f) return "Underweight";
        else if (bmi < 25f) return "Normal";
        else if (bmi < 30f) return "Overweight";
        else return "Obese";
    }

    private String formatTime(String time) {
        // Convert 24h format (HH:mm) to 12h format (hh:mm AM/PM)
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String period = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0) hour = 12;

            return String.format("%02d:%02d %s", hour, minute, period);
        } catch (Exception e) {
            return time;
        }
    }

    private String formatDays(List<String> days) {
        if (days == null || days.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (String day : days) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(day.substring(0, 3)); // First 3 letters of day
        }

        return sb.toString();
    }
    private void showEditProfileDialog(UserProfile profile) {
        EditProfileDialog editDialog = new EditProfileDialog(
                requireContext(),
                profile,
                (name, height, weight, age, goal) -> {
                    viewModel.updateProfile(name, height, weight, age, goal);
                    if (getView() != null) {
                        Snackbar.make(getView(), "Profile updated successfully!", Snackbar.LENGTH_SHORT).show();
                    }
                }
        );

        editDialog.show();
    }
}