package hcmute.edu.vn.loclinhvabao.carex.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaGoal;

public class EditProfileDialog {

    public interface OnProfileUpdateListener {
        void onProfileUpdated(String name, float height, float weight, int age, YogaGoal goal);
    }

    private final Context context;
    private final UserProfile currentProfile;
    private final OnProfileUpdateListener listener;

    // UI Components
    private TextInputEditText etName, etHeight, etWeight, etAge;
    private AutoCompleteTextView spinnerYogaGoal;
    private TextView tvBmiValue;
    private MaterialButton btnSave, btnCancel;

    private Dialog dialog;

    public EditProfileDialog(@NonNull Context context, @NonNull UserProfile currentProfile,
                             @NonNull OnProfileUpdateListener listener) {
        this.context = context;
        this.currentProfile = currentProfile;
        this.listener = listener;

        createDialog();
    }

    private void createDialog() {
        // Inflate custom layout
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_profile, null);

        // Initialize UI components
        initUI(dialogView);

        // Setup dropdown for yoga goals
        setupYogaGoalDropdown();

        // Populate fields with current data
        populateFields();

        // Setup BMI calculation
        setupBMICalculation();

        // Setup click listeners
        setupClickListeners();

        // Create dialog
        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();
    }

    private void initUI(View view) {
        etName = view.findViewById(R.id.et_name);
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);
        etAge = view.findViewById(R.id.et_age);
        spinnerYogaGoal = view.findViewById(R.id.spinner_yoga_goal);
        tvBmiValue = view.findViewById(R.id.tv_bmi_value);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupYogaGoalDropdown() {
        // Create array of yoga goal display names
        String[] yogaGoals = new String[YogaGoal.values().length];
        for (int i = 0; i < YogaGoal.values().length; i++) {
            yogaGoals[i] = YogaGoal.values()[i].getDisplayName();
        }

        // Create and set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                yogaGoals
        );
        spinnerYogaGoal.setAdapter(adapter);
    }

    private void populateFields() {
        etName.setText(currentProfile.getName());
        etHeight.setText(String.valueOf(currentProfile.getHeight()));
        etWeight.setText(String.valueOf(currentProfile.getWeight()));
        etAge.setText(String.valueOf(currentProfile.getAge()));
        spinnerYogaGoal.setText(currentProfile.getGoal().getDisplayName(), false);

        // Calculate and display current BMI
        calculateAndDisplayBMI();
    }

    private void setupBMICalculation() {
        TextWatcher bmiCalculator = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateAndDisplayBMI();
            }
        };

        etHeight.addTextChangedListener(bmiCalculator);
        etWeight.addTextChangedListener(bmiCalculator);
    }

    private void calculateAndDisplayBMI() {
        try {
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
                float height = Float.parseFloat(heightStr);
                float weight = Float.parseFloat(weightStr);

                if (height > 0 && weight > 0) {
                    float heightInMeters = height / 100f;
                    float bmi = weight / (heightInMeters * heightInMeters);

                    String bmiCategory = getBmiCategory(bmi);
                    tvBmiValue.setText(String.format("%.1f (%s)", bmi, bmiCategory));
                } else {
                    tvBmiValue.setText("--");
                }
            } else {
                tvBmiValue.setText("--");
            }
        } catch (NumberFormatException e) {
            tvBmiValue.setText("--");
        }
    }

    private String getBmiCategory(float bmi) {
        if (bmi < 18.5f) {
            return "Underweight";
        } else if (bmi < 25f) {
            return "Normal";
        } else if (bmi < 30f) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (validateAndSave()) {
                dialog.dismiss();
            }
        });
    }

    private boolean validateAndSave() {
        // Get input values
        String name = etName.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String yogaGoalStr = spinnerYogaGoal.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (heightStr.isEmpty()) {
            etHeight.setError("Height is required");
            etHeight.requestFocus();
            return false;
        }

        if (weightStr.isEmpty()) {
            etWeight.setError("Weight is required");
            etWeight.requestFocus();
            return false;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        if (yogaGoalStr.isEmpty()) {
            spinnerYogaGoal.setError("Please select a yoga goal");
            spinnerYogaGoal.requestFocus();
            return false;
        }

        try {
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);
            int age = Integer.parseInt(ageStr);

            // Validate ranges
            if (height <= 0 || height > 300) {
                etHeight.setError("Height must be between 1-300 cm");
                etHeight.requestFocus();
                return false;
            }

            if (weight <= 0 || weight > 500) {
                etWeight.setError("Weight must be between 1-500 kg");
                etWeight.requestFocus();
                return false;
            }

            if (age <= 0 || age > 150) {
                etAge.setError("Age must be between 1-150 years");
                etAge.requestFocus();
                return false;
            }

            // Get selected yoga goal
            YogaGoal selectedGoal = YogaGoal.fromDisplayName(yogaGoalStr);

            // Call listener to update profile
            listener.onProfileUpdated(name, height, weight, age, selectedGoal);

            return true;

        } catch (NumberFormatException e) {
            Snackbar.make(dialog.findViewById(android.R.id.content),
                    "Please enter valid numbers", Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}