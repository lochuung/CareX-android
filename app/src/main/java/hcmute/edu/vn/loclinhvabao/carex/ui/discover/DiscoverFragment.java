package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupClickListeners(view);
    }    private void setupClickListeners(View view) {
        // BMI Calculator Card Click
        MaterialCardView bmiCard = view.findViewById(R.id.bmi_calculator_card);
        if (bmiCard != null) {
            bmiCard.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.action_discoverFragment_to_bmiCalculatorFragment)
            );
        }

        // Step Counter Card Click
        MaterialCardView stepCard = view.findViewById(R.id.step_counter_card);
        if (stepCard != null) {
            stepCard.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.action_discoverFragment_to_stepCounterFragment)
            );
        }

        // Diet Recommendation Card Click
        MaterialCardView dietCard = view.findViewById(R.id.diet_recommendation_card);
        if (dietCard != null) {
            dietCard.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.action_discoverFragment_to_dietRecommendationFragment)
            );
        }
    }
}
