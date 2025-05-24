package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;

@AndroidEntryPoint
public class StepCounterFragment extends Fragment {    // UI Components
    private TextView tvCurrentSteps, tvGoalProgress, tvCalories, tvDistance, tvMotivation;
    private ProgressBar progressSteps;
    private RecyclerView rvStepHistory;

    // Demo data
    private int currentSteps = 8542;
    private int stepGoal = 10000;
    private StepHistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_counter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
        setupClickListeners();
        setupRecyclerView();
        updateStepData();
        loadStepHistory();
    }    private void initUI(View view) {
        // Step display
        tvCurrentSteps = view.findViewById(R.id.tv_current_steps);
        tvGoalProgress = view.findViewById(R.id.tv_goal_progress);
        progressSteps = view.findViewById(R.id.progress_steps);

        // Statistics
        tvCalories = view.findViewById(R.id.tv_calories);
        tvDistance = view.findViewById(R.id.tv_distance);
        tvMotivation = view.findViewById(R.id.tv_motivation);

        // RecyclerView
        rvStepHistory = view.findViewById(R.id.rv_step_history);
    }    private void setupClickListeners() {
        ImageView btnBack = getView().findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        MaterialButton btnSetGoal = getView().findViewById(R.id.btn_set_goal);
        btnSetGoal.setOnClickListener(v -> {
            // Demo: Change goal
            stepGoal = stepGoal == 10000 ? 12000 : 10000;
            updateStepData();
        });
    }

    private void setupRecyclerView() {
        adapter = new StepHistoryAdapter();
        rvStepHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStepHistory.setAdapter(adapter);
    }    @SuppressLint("DefaultLocale")
    private void updateStepData() {
        // Update current steps
        tvCurrentSteps.setText(String.format("%,d", currentSteps));

        // Update progress
        int progressPercentage = Math.min(100, (currentSteps * 100) / stepGoal);
        progressSteps.setProgress(progressPercentage);
        tvGoalProgress.setText(String.format("%d%% of %,d goal", progressPercentage, stepGoal));

        // Calculate and display statistics
        float caloriesBurned = currentSteps * 0.04f; // Approximate: 0.04 calories per step
        float distanceKm = currentSteps * 0.0008f; // Approximate: 0.8 meters per step
        
        tvCalories.setText(String.format("%.0f", caloriesBurned));
        tvDistance.setText(String.format("%.1f", distanceKm));

        // Update motivational message
        updateMotivationalMessage(progressPercentage);
    }

    private void updateMotivationalMessage(int progressPercentage) {
        String message;
        if (progressPercentage >= 100) {
            message = "🎉 Congratulations! You've reached your daily goal!";
        } else if (progressPercentage >= 75) {
            message = "💪 Almost there! You're doing great!";
        } else if (progressPercentage >= 50) {
            message = "🚶 Keep going! You're halfway to your goal!";
        } else if (progressPercentage >= 25) {
            message = "⭐ Good start! Every step counts!";
        } else {
            message = "🌟 Start your journey to better health!";
        }
        tvMotivation.setText(message);
    }

    private void loadStepHistory() {
        List<StepHistoryItem> historyItems = generateDemoHistory();
        adapter.setHistoryItems(historyItems);
    }

    private List<StepHistoryItem> generateDemoHistory() {
        List<StepHistoryItem> items = new ArrayList<>();
        Random random = new Random();
        Calendar calendar = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            Date date = calendar.getTime();
            
            int steps = 5000 + random.nextInt(8000); // 5000-13000 steps
            float calories = steps * 0.04f;
            float distance = steps * 0.0008f;
            
            items.add(new StepHistoryItem(date, steps, calories, distance));
            
            calendar.add(Calendar.DAY_OF_YEAR, i); // Reset for next iteration
        }
        
        return items;
    }

    // Inner class for step history data
    public static class StepHistoryItem {
        private Date date;
        private int steps;
        private float calories;
        private float distance;

        public StepHistoryItem(Date date, int steps, float calories, float distance) {
            this.date = date;
            this.steps = steps;
            this.calories = calories;
            this.distance = distance;
        }

        // Getters
        public Date getDate() { return date; }
        public int getSteps() { return steps; }
        public float getCalories() { return calories; }
        public float getDistance() { return distance; }
    }

    // RecyclerView Adapter
    public static class StepHistoryAdapter extends RecyclerView.Adapter<StepHistoryAdapter.ViewHolder> {
        private List<StepHistoryItem> historyItems = new ArrayList<>();

        public void setHistoryItems(List<StepHistoryItem> items) {
            this.historyItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_step_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(historyItems.get(position));
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvDate, tvSteps, tvCalories, tvDistance;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvSteps = itemView.findViewById(R.id.tv_steps);
                tvCalories = itemView.findViewById(R.id.tv_calories);
                tvDistance = itemView.findViewById(R.id.tv_distance);
            }

            @SuppressLint("DefaultLocale")
            public void bind(StepHistoryItem item) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                tvDate.setText(dateFormat.format(item.getDate()));
                tvSteps.setText(String.format("%,d steps", item.getSteps()));
                tvCalories.setText(String.format("%.0f kcal", item.getCalories()));
                tvDistance.setText(String.format("%.2f km", item.getDistance()));
            }
        }
    }
}
