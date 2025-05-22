package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Date;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.Progress;
import hcmute.edu.vn.loclinhvabao.carex.ui.shared.SharedViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters.YogaDayAdapter;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements YogaDayAdapter.OnDayClickListener {

    private SharedViewModel sharedViewModel;
    private RecyclerView rvYogaDays;
    private YogaDayAdapter adapter;
    private TextView tvProgress;
    private MaterialButton btnCurrentDay;
    
    // Statistics views
    private TextView tvCaloriesValue;
    private TextView tvTimeValue;
    private TextView tvScoreValue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Initialize UI
        initUI(view);
        setupRecyclerView();
        observeViewModel();
        checkForRecentCompletion();
    }

    private void initUI(View view) {
        TextView tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvProgress = view.findViewById(R.id.tv_progress);
        btnCurrentDay = view.findViewById(R.id.btn_current_day);
        rvYogaDays = view.findViewById(R.id.rv_yoga_days);
        
        // Initialize statistics views
        tvCaloriesValue = view.findViewById(R.id.tv_calories_value);
        tvTimeValue = view.findViewById(R.id.tv_time_value);
        tvScoreValue = view.findViewById(R.id.tv_score_value);
        
        btnCurrentDay.setOnClickListener(v -> navigateToCurrentDay());
    }

    private void setupRecyclerView() {
        adapter = new YogaDayAdapter(this);
        rvYogaDays.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvYogaDays.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Observe yoga days
        sharedViewModel.getYogaDays().observe(getViewLifecycleOwner(), yogaDays -> adapter.submitList(yogaDays));
        
        // Observe user progress
        sharedViewModel.getUserProgress().observe(getViewLifecycleOwner(), this::updateProgress);
        
        // Observe daily progress for statistics
        sharedViewModel.getDailyProgress().observe(getViewLifecycleOwner(), dailyProgress -> updateStatistics());
        
        // Observe detailed progress for statistics
        sharedViewModel.getDetailedProgress().observe(getViewLifecycleOwner(), progress -> updateStatistics());
        
        // Initialize statistics
        updateStatistics();
    }

    @SuppressLint("DefaultLocale")
    private void updateProgress(Map<Integer, Boolean> progress) {
        int completedDays = 0;
        int nextDay = 1;
        
        for (Map.Entry<Integer, Boolean> entry : progress.entrySet()) {
            if (entry.getValue()) {
                completedDays++;
            } else {
                if (nextDay == 1 || entry.getKey() < nextDay) {
                    nextDay = entry.getKey();
                }
            }
        }
        
        // Get the total completed days directly from the ViewModel
        int verifiedCompletedDays = sharedViewModel.getCompletedDaysCount();
        
        // Update progress text with percentage
        int progressPercentage = completedDays * 10;
        tvProgress.setText(String.format("Progress: %d/10 days completed (%d%%)", completedDays, progressPercentage));
        
        // Update current day button
        btnCurrentDay.setText(String.format("Continue to Day %d", nextDay));
    }
    
    private void navigateToCurrentDay() {
        // Find the next uncompleted day
        Map<Integer, Boolean> progress = sharedViewModel.getUserProgress().getValue();
        if (progress == null) return;
        
        int nextDay = 1;
        for (Map.Entry<Integer, Boolean> entry : progress.entrySet()) {
            if (!entry.getValue()) {
                if (nextDay == 1 || entry.getKey() < nextDay) {
                    nextDay = entry.getKey();
                }
            }
        }
        
        // Find the YogaDay object for the next day
        List<YogaDay> yogaDays = sharedViewModel.getYogaDays().getValue();
        if (yogaDays == null) return;
        
        for (YogaDay day : yogaDays) {
            if (day.getDayNumber() == nextDay) {
                sharedViewModel.selectYogaDay(day);
                
                // Navigate to exercise detail fragment
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_homeFragment_to_exerciseDetailFragment);
                break;
            }
        }
    }

    @Override
    public void onDayClick(YogaDay day) {
        // Set selected day in ViewModel
        sharedViewModel.selectYogaDay(day);
        
        // Navigate to exercise detail fragment
        Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_exerciseDetailFragment);
    }

    @SuppressLint("DefaultLocale")
    private void updateStatistics() {
        // Get statistics from the ViewModel
        int totalCalories = sharedViewModel.getTotalCaloriesBurned();
        int totalDuration = sharedViewModel.getTotalDuration();
        float avgConfidence = sharedViewModel.getAverageConfidence();
        
        // Format the duration from seconds to minutes
        int minutes = totalDuration / 60;
        
        // Update UI
        tvCaloriesValue.setText(String.valueOf(totalCalories));
        tvTimeValue.setText(String.format("%d min", minutes));
        tvScoreValue.setText(String.format("%.0f%%", avgConfidence * 100));
    }

    /**
     * Check if user has completed a day recently and show congratulation
     */
    private void checkForRecentCompletion() {
        // Get the completed progress
        sharedViewModel.getCompletedProgress().observe(getViewLifecycleOwner(), progressList -> {
            if (progressList != null && !progressList.isEmpty()) {
                // Sort by completion date to get the most recent
                progressList.sort((a, b) -> b.getCompletionDate().compareTo(a.getCompletionDate()));
                
                // Check if the most recent completion was today
                Progress mostRecent = progressList.get(0);
                Date now = new Date();
                long diffInMillis = now.getTime() - mostRecent.getCompletionDate().getTime();
                long diffInHours = diffInMillis / (60 * 60 * 1000);
                
                // If completed in the last 2 hours, show congratulation
                if (diffInHours < 2) {
                    showCongratulationMessage(mostRecent);
                }
            }
        });
    }
    
    /**
     * Show a congratulation message when a day is completed
     */
    @SuppressLint("DefaultLocale")
    private void showCongratulationMessage(Progress progress) {
        // Create dialog to show congratulation
        new AlertDialog.Builder(requireContext())
            .setTitle("Congratulations!")
            .setMessage(String.format(
                "You've completed Day %d!\n\n" +
                "Time: %d minutes\n" +
                "Calories burned: %d\n" +
                "Performance score: %.0f%%", 
                progress.getDayNumber(),
                progress.getDuration() / 60,
                progress.getCalories(),
                progress.getAverageConfidence() * 100
            ))
            .setPositiveButton("Great!", null)
            .setIcon(R.drawable.ic_celebration)
            .show();
    }
}
