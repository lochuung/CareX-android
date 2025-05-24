package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.ProgressStats;
import hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter.ActivityAdapter;
import hcmute.edu.vn.loclinhvabao.carex.util.ChartUtils;

@AndroidEntryPoint
public class ReportFragment extends Fragment {

    private ReportViewModel viewModel;

    // UI components
    private TextView tvTotalSessions, tvTotalMinutes, tvTotalCalories;
    private TextView tvViewDetails, tvViewAllActivities;
    private RecyclerView rvRecentActivities;
    private CardView cardWeeklyChart;
    private ViewGroup chartContainer;
    private LineChart weeklyChart;
    private ShapeableImageView ivProfile;

    private ActivityAdapter activityAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ViewModel
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();
    }

    private void initUI(View view) {
        // Stats summary
        tvTotalSessions = view.findViewById(R.id.tv_total_sessions);
        tvTotalMinutes = view.findViewById(R.id.tv_total_minutes);
        tvTotalCalories = view.findViewById(R.id.tv_total_calories);

        // View details button
        tvViewDetails = view.findViewById(R.id.tv_view_details);
        tvViewAllActivities = view.findViewById(R.id.tv_view_all_activities);

        // Chart container
        cardWeeklyChart = view.findViewById(R.id.card_weekly_chart);
        chartContainer = view.findViewById(R.id.chart_container);

        // Create and add LineChart
        weeklyChart = new LineChart(requireContext());
        chartContainer.addView(weeklyChart);

        // Recent activities
        rvRecentActivities = view.findViewById(R.id.rv_recent_activities);
        rvRecentActivities.setLayoutManager(new LinearLayoutManager(requireContext()));
        activityAdapter = new ActivityAdapter();
        rvRecentActivities.setAdapter(activityAdapter);

        // Profile image
        ivProfile = view.findViewById(R.id.iv_profile);
    }

    private void setupObservers() {
        // Observe progress stats
        viewModel.getProgressStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                // Update summary stats
                tvTotalSessions.setText(String.valueOf(stats.getTotalSessions()));
                tvTotalMinutes.setText(String.valueOf(stats.getTotalMinutes()));
                tvTotalCalories.setText(String.valueOf(stats.getTotalCalories()));

                // Update chart with weekly progress data
                updateWeeklyChart(stats);
            }
        });

        // Observe recent sessions
        viewModel.getRecentSessions().observe(getViewLifecycleOwner(), sessions -> {
            activityAdapter.setSessions(sessions);
        });
    }

    private void setupClickListeners() {
        // View details click - navigate to ProgressFragment
        tvViewDetails.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    R.id.action_reportFragment_to_progressFragment);
        });

        // View all activities click
        tvViewAllActivities.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                    R.id.action_reportFragment_to_progressFragment);
        });

        // Profile image click - navigate to Settings instead of Profile
        ivProfile.setOnClickListener(v -> {
            // Navigate to Settings tab
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottom_navigation))
                    .setSelectedItemId(R.id.settingsFragment);
        });

        // Set click listener for each activity item
        activityAdapter.setOnSessionClickListener(session -> {
            if (session != null && session.getId() != null) {
                Bundle args = new Bundle();
                args.putString("sessionId", session.getId());
                Navigation.findNavController(requireView()).navigate(
                        R.id.action_progressFragment_to_sessionDetailFragment, args);
            }
        });
    }

    private void updateWeeklyChart(ProgressStats stats) {
        if (stats == null || stats.getWeeklyProgress() == null) {
            // No data, setup empty chart
            setupEmptyChart();
            return;
        }

        // Use ChartUtils to setup chart with data
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        ChartUtils.setupLineChart(weeklyChart, requireContext(),
                stats.getWeeklyProgress(), days);
    }

    private void setupEmptyChart() {
        // Create empty chart with placeholder message
        weeklyChart.clear();
        weeklyChart.setNoDataText("No workout data available");
        weeklyChart.setNoDataTextColor(getResources().getColor(R.color.textSecondary, null));
        weeklyChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshData();
    }
}