package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.util.NotificationUtils;

@AndroidEntryPoint
public class ReminderSettingsFragment extends Fragment {

    private ReminderSettingsViewModel viewModel;

    // UI Components
    private ImageView ivBack;
    private SwitchMaterial switchEnableReminders;
    private TimePicker timePicker;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private SwitchMaterial switchSound, switchVibration;
    private MaterialButton btnSaveSettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ReminderSettingsViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void initUI(View view) {
        // Back button
        ivBack = view.findViewById(R.id.iv_back);

        // Reminder settings
        switchEnableReminders = view.findViewById(R.id.switch_enable_reminders);
        timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        // Day checkboxes
        cbMonday = view.findViewById(R.id.cb_monday);
        cbTuesday = view.findViewById(R.id.cb_tuesday);
        cbWednesday = view.findViewById(R.id.cb_wednesday);
        cbThursday = view.findViewById(R.id.cb_thursday);
        cbFriday = view.findViewById(R.id.cb_friday);
        cbSaturday = view.findViewById(R.id.cb_saturday);
        cbSunday = view.findViewById(R.id.cb_sunday);

        // Notification preferences
        switchSound = view.findViewById(R.id.switch_sound);
        switchVibration = view.findViewById(R.id.switch_vibration);

        // Save button
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
    }

    private void setupObservers() {
        viewModel.getReminderEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchEnableReminders.setChecked(enabled);
            updateUIState(enabled);
        });

        viewModel.getReminderTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null && !time.isEmpty()) {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                // Set time picker
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                } else {
                    timePicker.setCurrentHour(hour);
                    timePicker.setCurrentMinute(minute);
                }
            }
        });

        viewModel.getReminderDays().observe(getViewLifecycleOwner(), days -> {
            if (days != null) {
                cbMonday.setChecked(days.contains("MONDAY"));
                cbTuesday.setChecked(days.contains("TUESDAY"));
                cbWednesday.setChecked(days.contains("WEDNESDAY"));
                cbThursday.setChecked(days.contains("THURSDAY"));
                cbFriday.setChecked(days.contains("FRIDAY"));
                cbSaturday.setChecked(days.contains("SATURDAY"));
                cbSunday.setChecked(days.contains("SUNDAY"));
            }
        });

        viewModel.getSoundEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchSound.setChecked(enabled);
        });

        viewModel.getVibrationEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchVibration.setChecked(enabled);
        });
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Enable reminders switch
        switchEnableReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setReminderEnabled(isChecked);
            updateUIState(isChecked);
        });

        // Sound switch
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSoundEnabled(isChecked);
        });

        // Vibration switch
        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setVibrationEnabled(isChecked);
        });

        // Day checkboxes
        cbMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("MONDAY", isChecked);
        });

        cbTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("TUESDAY", isChecked);
        });

        cbWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("WEDNESDAY", isChecked);
        });

        cbThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("THURSDAY", isChecked);
        });

        cbFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("FRIDAY", isChecked);
        });

        cbSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("SATURDAY", isChecked);
        });

        cbSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDay("SUNDAY", isChecked);
        });

        // Save button
        btnSaveSettings.setOnClickListener(v -> {
            saveSettings();
        });
    }

    private void updateUIState(boolean enabled) {
        // Enable/disable UI based on reminder status
        timePicker.setEnabled(enabled);
        cbMonday.setEnabled(enabled);
        cbTuesday.setEnabled(enabled);
        cbWednesday.setEnabled(enabled);
        cbThursday.setEnabled(enabled);
        cbFriday.setEnabled(enabled);
        cbSaturday.setEnabled(enabled);
        cbSunday.setEnabled(enabled);
        switchSound.setEnabled(enabled);
        switchVibration.setEnabled(enabled);
    }

    private void saveSettings() {
        // Get time from picker
        int hour, minute;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        String time = String.format("%02d:%02d", hour, minute);
        viewModel.setReminderTime(time);

        // Get selected days
        List<String> days = new ArrayList<>();
        if (cbMonday.isChecked()) days.add("MONDAY");
        if (cbTuesday.isChecked()) days.add("TUESDAY");
        if (cbWednesday.isChecked()) days.add("WEDNESDAY");
        if (cbThursday.isChecked()) days.add("THURSDAY");
        if (cbFriday.isChecked()) days.add("FRIDAY");
        if (cbSaturday.isChecked()) days.add("SATURDAY");
        if (cbSunday.isChecked()) days.add("SUNDAY");
        viewModel.setReminderDays(days);

        // Save settings
        viewModel.saveSettings();

        // Schedule reminders
        if (viewModel.getReminderEnabled().getValue()) {
            NotificationUtils.scheduleReminders(
                    requireContext(),
                    time,
                    days
            );
        }

        // Show confirmation and navigate back
        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }
}