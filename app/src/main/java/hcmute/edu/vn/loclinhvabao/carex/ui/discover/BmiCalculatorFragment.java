package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;

@AndroidEntryPoint
public class BmiCalculatorFragment extends Fragment {

    // UI Components
    private EditText etHeight, etWeight;
    private MaterialButton btnCalculate, btnReset;
    private TextView tvBmiResult, tvBmiCategory, tvBmiDescription;
    private LinearLayout categoryLayout;
    private View bmiCategoryIndicator;
    private ImageView ivBack, bmiMarker;
    private LinearLayout bmiScaleBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bmi_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
        setupClickListeners();
        hideResultElements();
    }

    private void initUI(View view) {
        // Input fields
        etHeight = view.findViewById(R.id.et_height);
        etWeight = view.findViewById(R.id.et_weight);

        // Buttons
        btnCalculate = view.findViewById(R.id.btn_calculate);
        btnReset = view.findViewById(R.id.btn_reset);
        ivBack = view.findViewById(R.id.iv_back);

        // Result display
        tvBmiResult = view.findViewById(R.id.tv_bmi_result);
        tvBmiCategory = view.findViewById(R.id.tv_bmi_category);
        tvBmiDescription = view.findViewById(R.id.tv_bmi_description);
        categoryLayout = view.findViewById(R.id.category_layout);
        bmiCategoryIndicator = view.findViewById(R.id.bmi_category_indicator);
        bmiMarker = view.findViewById(R.id.bmi_marker);
        bmiScaleBar = view.findViewById(R.id.bmi_scale_bar);
    }    private void setupClickListeners() {
        btnCalculate.setOnClickListener(v -> calculateBMI());
        btnReset.setOnClickListener(v -> resetForm());
        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void calculateBMI() {
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
            Snackbar.make(requireView(), "Please enter both height and weight", Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            // Validate ranges
            if (height <= 0 || height > 300) {
                Snackbar.make(requireView(), "Please enter a valid height (1-300 cm)", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (weight <= 0 || weight > 500) {
                Snackbar.make(requireView(), "Please enter a valid weight (1-500 kg)", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Calculate BMI
            float heightInMeters = height / 100f;
            float bmi = weight / (heightInMeters * heightInMeters);

            // Display results
            displayBMIResult(bmi);

        } catch (NumberFormatException e) {
            Snackbar.make(requireView(), "Please enter valid numbers", Snackbar.LENGTH_SHORT).show();
        }
    }    @SuppressLint("DefaultLocale")
    private void displayBMIResult(float bmi) {
        // Show result elements
        showResultElements();

        // Display BMI value
        tvBmiResult.setText(String.format("%.1f", bmi));

        // Position BMI marker on scale
        positionBMIMarker(bmi);

        // Determine category and set appropriate styling
        String category;
        String description;
        int color;

        if (bmi < 18.5f) {
            category = "Underweight";
            description = "You may need to gain weight. Consider consulting with a healthcare professional for a personalized plan.";
            color = getResources().getColor(R.color.bmi_underweight, null);
        } else if (bmi < 25f) {
            category = "Normal Weight";
            description = "Congratulations! You have a healthy weight. Maintain your current lifestyle with regular exercise and balanced diet.";
            color = getResources().getColor(R.color.bmi_normal, null);
        } else if (bmi < 30f) {
            category = "Overweight";
            description = "Consider adopting a healthier lifestyle with regular exercise and a balanced diet to achieve a healthier weight.";
            color = getResources().getColor(R.color.bmi_overweight, null);
        } else {
            category = "Obese";
            description = "It's recommended to consult with a healthcare professional for a comprehensive weight management plan.";
            color = getResources().getColor(R.color.bmi_obese, null);
        }

        tvBmiCategory.setText(category);
        tvBmiDescription.setText(description);
        bmiCategoryIndicator.setBackgroundColor(color);
        tvBmiCategory.setTextColor(color);
    }    private void positionBMIMarker(float bmi) {
        // Clamp BMI to scale range (15-40)
        float clampedBmi = Math.max(15f, Math.min(40f, bmi));
        
        // Calculate position based on weighted segments
        float percentage = calculateWeightedBMIPosition(clampedBmi);
        
        // Post to ensure the scale bar has been laid out
        bmiScaleBar.post(() -> {
            int scaleWidth = bmiScaleBar.getWidth();
            if (scaleWidth > 0) {
                // Calculate marker position
                int markerPosition = (int) (scaleWidth * percentage);
                
                // Adjust for marker width to center it
                int markerWidth = bmiMarker.getWidth();
                markerPosition -= markerWidth / 2;
                
                // Set margin to position the marker
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) bmiMarker.getLayoutParams();
                params.leftMargin = markerPosition;
                bmiMarker.setLayoutParams(params);
                
                // Show the marker
                bmiMarker.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Calculate BMI marker position based on weighted segments of the BMI scale.
     * The BMI scale has the following weighted segments:
     * - Severely Underweight (15-16): weight 1
     * - Underweight (16-18.5): weight 2.5
     * - Normal Weight (18.5-25): weight 6.5
     * - Overweight (25-30): weight 5
     * - Obese Class I (30-35): weight 5
     * - Obese Class II+ (35-40+): weight 5
     * Total weight: 25
     */
    private float calculateWeightedBMIPosition(float bmi) {
        float cumulativeWeight = 0f;
        final float totalWeight = 25f; // Sum of all segment weights
        
        if (bmi <= 15f) {
            return 0f;
        } else if (bmi <= 16f) {
            // Severely Underweight segment (15-16): weight 1
            float segmentProgress = (bmi - 15f) / 1f; // 1 BMI unit range
            return (cumulativeWeight + segmentProgress * 1f) / totalWeight;
        } else if (bmi <= 18.5f) {
            // Underweight segment (16-18.5): weight 2.5
            cumulativeWeight += 1f; // Previous segment weight
            float segmentProgress = (bmi - 16f) / 2.5f; // 2.5 BMI unit range
            return (cumulativeWeight + segmentProgress * 2.5f) / totalWeight;
        } else if (bmi <= 25f) {
            // Normal Weight segment (18.5-25): weight 6.5
            cumulativeWeight += 1f + 2.5f; // Previous segments weight
            float segmentProgress = (bmi - 18.5f) / 6.5f; // 6.5 BMI unit range
            return (cumulativeWeight + segmentProgress * 6.5f) / totalWeight;
        } else if (bmi <= 30f) {
            // Overweight segment (25-30): weight 5
            cumulativeWeight += 1f + 2.5f + 6.5f; // Previous segments weight
            float segmentProgress = (bmi - 25f) / 5f; // 5 BMI unit range
            return (cumulativeWeight + segmentProgress * 5f) / totalWeight;
        } else if (bmi <= 35f) {
            // Obese Class I segment (30-35): weight 5
            cumulativeWeight += 1f + 2.5f + 6.5f + 5f; // Previous segments weight
            float segmentProgress = (bmi - 30f) / 5f; // 5 BMI unit range
            return (cumulativeWeight + segmentProgress * 5f) / totalWeight;
        } else {
            // Obese Class II+ segment (35-40+): weight 5
            cumulativeWeight += 1f + 2.5f + 6.5f + 5f + 5f; // Previous segments weight
            float segmentProgress = Math.min(1f, (bmi - 35f) / 5f); // 5 BMI unit range, capped at 1
            return (cumulativeWeight + segmentProgress * 5f) / totalWeight;
        }
    }

    private void showResultElements() {
        categoryLayout.setVisibility(View.VISIBLE);
        tvBmiDescription.setVisibility(View.VISIBLE);
        bmiMarker.setVisibility(View.VISIBLE);
    }

    private void hideResultElements() {
        categoryLayout.setVisibility(View.GONE);
        tvBmiDescription.setVisibility(View.GONE);
        bmiMarker.setVisibility(View.GONE);
    }    private void resetForm() {
        etHeight.setText("");
        etWeight.setText("");
        tvBmiResult.setText("--");
        hideResultElements();
        etHeight.requestFocus();
    }
}
