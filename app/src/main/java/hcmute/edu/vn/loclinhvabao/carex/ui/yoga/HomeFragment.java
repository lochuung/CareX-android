package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
public class HomeFragment extends Fragment implements YogaDayAdapter.OnDayClickListener {    private SharedViewModel sharedViewModel;    private RecyclerView rvYogaDays;
    private YogaDayAdapter adapter;
    private TextView tvProgress;
    private ProgressBar progressOverall;
    
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
        // Removed checkForRecentCompletion() - now handled via ViewModel events
    }    private void initUI(View view) {
        TextView tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvProgress = view.findViewById(R.id.tv_progress);        
        progressOverall = view.findViewById(R.id.progress_overall);
        rvYogaDays = view.findViewById(R.id.rv_yoga_days);
        
        // Initialize statistics views
        tvCaloriesValue = view.findViewById(R.id.tv_calories_value);
        tvTimeValue = view.findViewById(R.id.tv_time_value);
        tvScoreValue = view.findViewById(R.id.tv_score_value);
        
        // Initialize reset button
        Button btnResetProgress = view.findViewById(R.id.btn_reset_progress);
        btnResetProgress.setOnClickListener(v -> showResetConfirmationDialog());
    }

    private void setupRecyclerView() {
        adapter = new YogaDayAdapter(this);
        rvYogaDays.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvYogaDays.setAdapter(adapter);
    }    private void observeViewModel() {
        // Observe yoga days
        sharedViewModel.getYogaDays().observe(getViewLifecycleOwner(), yogaDays -> adapter.submitList(yogaDays));
        
        // Observe user progress
        sharedViewModel.getUserProgress().observe(getViewLifecycleOwner(), this::updateProgress);
        
        // Observe daily progress for statistics
        sharedViewModel.getDailyProgress().observe(getViewLifecycleOwner(), dailyProgress -> updateStatistics());
        
        // Observe detailed progress for statistics
        sharedViewModel.getDetailedProgress().observe(getViewLifecycleOwner(), progress -> updateStatistics());
        
        // Observe completion events (only show congratulation when actually completing, not on fragment creation)
        sharedViewModel.getDayCompletionEvent().observe(getViewLifecycleOwner(), completionData -> {
            if (completionData != null) {
                showCongratulationMessage(completionData);
                // Clear the event after showing
                sharedViewModel.clearCompletionEvent();
            }
        });
        
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
        int verifiedCompletedDays = sharedViewModel.getCompletedDaysCount();        // Update progress text with percentage
        int progressPercentage = completedDays * 10;
        tvProgress.setText(String.format("Progress: %d/10 days completed (%d%%)", completedDays, progressPercentage));
        
        // Update progress bar
        progressOverall.setProgress(progressPercentage);
        progressOverall.setMax(100);
    }    
    @Override
    public void onDayClick(YogaDay day) {
        // Set selected day in ViewModel
        sharedViewModel.selectYogaDay(day);
        
        // Navigate to exercise detail fragment
        Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_exerciseDetailFragment);
    }    @SuppressLint("DefaultLocale")
    private void updateStatistics() {
        // Get statistics from the ViewModel asynchronously
        new Thread(() -> {
            int totalCalories = sharedViewModel.getTotalCaloriesBurned();
            int totalDuration = sharedViewModel.getTotalDuration();
            float avgConfidence = sharedViewModel.getAverageConfidence();
            
            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Format the duration from seconds to minutes and seconds
                    int minutes = totalDuration / 60;
                    int seconds = totalDuration % 60;
                    
                    // Update UI
                    tvCaloriesValue.setText(String.valueOf(totalCalories));
                    tvTimeValue.setText(String.format("%d:%02d", minutes, seconds));
                    tvScoreValue.setText(String.format("%.0f%%", avgConfidence * 100));
                });
            }
        }).start();
    }/**
     * Show a congratulation message when a day is completed
     * Now receives completion data directly from ViewModel event
     */
    @SuppressLint("DefaultLocale")
    private void showCongratulationMessage(Progress progress) {
        // Create dialog to show congratulation
        int totalSeconds = progress.getDuration();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Congratulations!")
            .setMessage(String.format(
                "You've completed Day %d!\n\n" +
                "Time: %d:%02d\n" +
                "Calories burned: %d\n" +
                "Performance score: %.0f%%", 
                progress.getDayNumber(),
                minutes,
                seconds,
                progress.getCalories(),
                progress.getAverageConfidence() * 100
            ))
            .setPositiveButton("Great!", null)
            .setIcon(R.drawable.ic_celebration)
            .show();
    }

    /**
     * Show confirmation dialog before resetting progress
     */
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Reset Progress")
            .setMessage("Are you sure you want to reset all your progress? This action cannot be undone.")
            .setPositiveButton("Reset", (dialog, which) -> {
                // Reset all progress via ViewModel
                sharedViewModel.resetAllProgress();
                
                // Show confirmation message
                new AlertDialog.Builder(requireContext())
                    .setTitle("Progress Reset")
                    .setMessage("Your progress has been successfully reset. You can start the 10-day yoga challenge again!")
                    .setPositiveButton("OK", null)
                    .show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
