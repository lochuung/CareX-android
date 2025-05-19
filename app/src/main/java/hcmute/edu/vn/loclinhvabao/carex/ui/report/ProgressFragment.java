package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.DailyProgress;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter.SessionHistoryAdapter;
import hcmute.edu.vn.loclinhvabao.carex.util.ChartUtils;

@AndroidEntryPoint
public class ProgressFragment extends Fragment {

    private ProgressViewModel viewModel;

    // UI Components
    private ImageView ivBack;
    private ChipGroup chipGroupTime;
    private Chip chipWeek, chipMonth, chipYear, chipAll;
    private ViewGroup mainChartContainer, pieChartContainer;
    private TextView tvAvgSessions, tvAvgDuration, tvAvgCalories;
    private TextView tvFavoriteYoga, tvConsistentDay;
    private RecyclerView rvSessionHistory;

    private LineChart lineChart;
    private PieChart pieChart;
    private SessionHistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set up charts
        setupCharts();

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void initUI(View view) {
        // Back button
        ivBack = view.findViewById(R.id.iv_back);

        // Time period chips
        chipGroupTime = view.findViewById(R.id.chip_group_time);
        chipWeek = view.findViewById(R.id.chip_week);
        chipMonth = view.findViewById(R.id.chip_month);
        chipYear = view.findViewById(R.id.chip_year);
        chipAll = view.findViewById(R.id.chip_all);

        // Chart containers
        mainChartContainer = view.findViewById(R.id.main_chart_container);
        pieChartContainer = view.findViewById(R.id.pie_chart_container);

        // Stats text views
        tvAvgSessions = view.findViewById(R.id.tv_avg_sessions);
        tvAvgDuration = view.findViewById(R.id.tv_avg_duration);
        tvAvgCalories = view.findViewById(R.id.tv_avg_calories);
        tvFavoriteYoga = view.findViewById(R.id.tv_favorite_yoga);
        tvConsistentDay = view.findViewById(R.id.tv_consistent_day);

        // Session history recycler view
        rvSessionHistory = view.findViewById(R.id.rv_session_history);
        rvSessionHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SessionHistoryAdapter(session -> {
            // Navigate to session detail
            if (session != null && session.getId() != null) {
                Bundle args = new Bundle();
                args.putString("sessionId", session.getId());
                Navigation.findNavController(view).navigate(
                        R.id.action_progressFragment_to_sessionDetailFragment, args);
            }
        });
        rvSessionHistory.setAdapter(adapter);
    }

    private void setupCharts() {
        // Initialize and configure Line Chart
        lineChart = new LineChart(requireContext());
        mainChartContainer.addView(lineChart);

        // Initialize and configure Pie Chart
        pieChart = new PieChart(requireContext());
        pieChartContainer.addView(pieChart);
    }

    private void setupObservers() {
        // Observe filtered sessions for the selected time range
        viewModel.getFilteredSessions().observe(getViewLifecycleOwner(), sessions -> {
            updateSessionHistory(sessions);
            updateCharts(sessions);
        });
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Time period chips
        chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_week) {
                    viewModel.setTimeRange("week");
                } else if (checkedId == R.id.chip_month) {
                    viewModel.setTimeRange("month");
                } else if (checkedId == R.id.chip_year) {
                    viewModel.setTimeRange("year");
                } else if (checkedId == R.id.chip_all) {
                    viewModel.setTimeRange("all");
                }
            }
        });
    }

    private void updateSessionHistory(List<YogaSession> sessions) {
        adapter.setSessions(sessions);

        // Update stats
        if (sessions != null && !sessions.isEmpty()) {
            // Calculate average sessions per week
            float avgSessionsPerWeek = calculateAvgSessionsPerWeek(sessions);
            tvAvgSessions.setText(String.format("%.1f", avgSessionsPerWeek));

            // Calculate average duration per session
            int totalDuration = 0;
            for (YogaSession session : sessions) {
                totalDuration += session.getDuration();
            }
            float avgDuration = (float) totalDuration / sessions.size();
            tvAvgDuration.setText(String.format("%.0f min", avgDuration));

            // Calculate average calories per session
            int totalCalories = 0;
            for (YogaSession session : sessions) {
                totalCalories += session.getCalories();
            }
            float avgCalories = (float) totalCalories / sessions.size();
            tvAvgCalories.setText(String.format("%.0f", avgCalories));

            // Find favorite yoga style
            Map<String, Integer> styleCount = new HashMap<>();
            for (YogaSession session : sessions) {
                String type = session.getType();
                styleCount.put(type, styleCount.getOrDefault(type, 0) + 1);
            }
            String favoriteStyle = "";
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : styleCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    favoriteStyle = entry.getKey();
                }
            }
            tvFavoriteYoga.setText(favoriteStyle);

            // For demonstration, set most consistent day
            tvConsistentDay.setText("Monday"); // This should be calculated from actual data
        } else {
            // No sessions, set default values
            tvAvgSessions.setText("0");
            tvAvgDuration.setText("0 min");
            tvAvgCalories.setText("0");
            tvFavoriteYoga.setText("-");
            tvConsistentDay.setText("-");
        }
    }

    private void updateCharts(List<YogaSession> sessions) {
        // Update line chart
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // For demonstration, create sample data
        List<DailyProgress> progressList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DailyProgress progress = new DailyProgress();
            progress.setDuration(i * 10 + 20); // Sample durations
            progressList.add(progress);
        }

        ChartUtils.setupLineChart(lineChart, requireContext(), progressList, days);

        // Update pie chart
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("Yoga", 120);
        distribution.put("Meditation", 90);
        distribution.put("Hatha", 60);
        distribution.put("Vinyasa", 45);
        distribution.put("Power", 30);

        ChartUtils.setupPieChart(pieChart, requireContext(), distribution);
    }

    private float calculateAvgSessionsPerWeek(List<YogaSession> sessions) {
        // In a real implementation, this would calculate based on the actual dates
        // For now, return a sample value
        return 4.2f;
    }
}