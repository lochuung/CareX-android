package hcmute.edu.vn.loclinhvabao.carex.ui.training;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;

@AndroidEntryPoint
public class TrainingFragment extends Fragment {    private CardView cardYoga, cardMeditation, cardHatha, cardVinyasa, cardPower;
    private MaterialButton btnStartWorkout, btnStartYogaSession;
    private CardView cardRecentWorkout1, cardRecentWorkout2;
    private TextView tvViewAll;@Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_training, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize the UI components
        initUI(view);
    }

    private void initUI(View view) {
        // Initialize CardViews for yoga categories
        cardYoga = view.findViewById(R.id.card_yoga);
        cardMeditation = view.findViewById(R.id.card_meditation);
        cardHatha = view.findViewById(R.id.card_hatha);
        cardVinyasa = view.findViewById(R.id.card_vinyasa);
        cardPower = view.findViewById(R.id.card_power);
          // Initialize workout start buttons
        btnStartWorkout = view.findViewById(R.id.btn_start_workout);
        btnStartYogaSession = view.findViewById(R.id.btn_start_yoga_session);
        
        // Initialize recent workout cards
        cardRecentWorkout1 = view.findViewById(R.id.card_recent_workout_1);
        cardRecentWorkout2 = view.findViewById(R.id.card_recent_workout_2);
        
        // Initialize view all text
        tvViewAll = view.findViewById(R.id.tv_view_all);
        
        // Set click listeners
        setClickListeners();
    }
      private void setClickListeners() {
        // Set click listener for yoga category cards
        cardYoga.setOnClickListener(v -> navigateToYogaProgram());
        cardMeditation.setOnClickListener(v -> showYogaSessionDetails(R.string.meditation));
        cardHatha.setOnClickListener(v -> showYogaSessionDetails(R.string.hatha_yoga));
        cardVinyasa.setOnClickListener(v -> showYogaSessionDetails(R.string.vinyasa_flow));
        cardPower.setOnClickListener(v -> showYogaSessionDetails(R.string.power_yoga));
          // Set click listener for start workout buttons
        btnStartWorkout.setOnClickListener(v -> startYogaSession());
        btnStartYogaSession.setOnClickListener(v -> navigateToYogaProgram());
        
        // Set click listeners for recent workout cards
        cardRecentWorkout1.setOnClickListener(v -> navigateToYogaProgram());
        cardRecentWorkout2.setOnClickListener(v -> navigateToYogaProgram());
        
        // Set click listener for view all text
        tvViewAll.setOnClickListener(v -> navigateToYogaProgram());
    }
    
    private void showYogaSessionDetails(int yogaStyleResId) {
        String yogaStyle = getString(yogaStyleResId);
        Toast.makeText(requireContext(), 
                "Opening " + yogaStyle + " sessions", 
                Toast.LENGTH_SHORT).show();
        navigateToYogaProgram();
    }
    
    private void startYogaSession() {
        Toast.makeText(requireContext(), 
                "Starting yoga session", 
                Toast.LENGTH_SHORT).show();
        navigateToYogaProgram();
    }
    
    private void navigateToYogaProgram() {
        // Navigate to the 10-day yoga program (HomeFragment)
        Navigation.findNavController(requireView())
                .navigate(R.id.action_trainingFragment_to_homeFragment);
    }
    
    private void showSessionDetails(String sessionName) {
        Toast.makeText(requireContext(), 
                "Opening " + sessionName + " details", 
                Toast.LENGTH_SHORT).show();
        navigateToYogaProgram();
    }
    
    private void viewAllSessions() {
        Toast.makeText(requireContext(), 
                "Viewing all yoga sessions", 
                Toast.LENGTH_SHORT).show();
        navigateToYogaProgram();
    }
}
