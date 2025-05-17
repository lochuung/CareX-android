package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.RadarChart;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter.PoseAdapter;
import hcmute.edu.vn.loclinhvabao.carex.util.DateUtils;

@AndroidEntryPoint
public class SessionDetailFragment extends Fragment {

    private SessionDetailViewModel viewModel;

    // UI Components
    private ImageView ivBack;
    private TextView tvSessionName, tvSessionDate;
    private TextView tvDuration, tvCalories, tvPoses;
    private TextView tvOverallScore, tvScoreLabel;
    private ViewGroup radarChartContainer;
    private RadarChart radarChart;
    private RecyclerView rvPoses;
    private TextView tvNotes;
    private MaterialButton btnShare;

    private PoseAdapter poseAdapter;
    private String sessionId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get session ID from arguments
        if (getArguments() != null) {
            sessionId = getArguments().getString("sessionId");
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SessionDetailViewModel.class);

        // Initialize UI components
        initUI(view);

        // Set up observers
        setupObservers();

        // Set up click listeners
        setupClickListeners();

        // Load session data
        if (sessionId != null) {
            viewModel.loadSession(sessionId);
        } else {
            Toast.makeText(requireContext(), "Session ID not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        }
    }

    private void initUI(View view) {
        // Back button
        ivBack = view.findViewById(R.id.iv_back);

        // Session header
        tvSessionName = view.findViewById(R.id.tv_session_name);
        tvSessionDate = view.findViewById(R.id.tv_session_date);
        tvDuration = view.findViewById(R.id.tv_duration);
        tvCalories = view.findViewById(R.id.tv_calories);
        tvPoses = view.findViewById(R.id.tv_poses);

        // Session score
        tvOverallScore = view.findViewById(R.id.tv_overall_score);
        tvScoreLabel = view.findViewById(R.id.tv_score_label);

        // Radar chart
        radarChartContainer = view.findViewById(R.id.radar_chart_container);
        radarChart = new RadarChart(requireContext());
        radarChartContainer.addView(radarChart);

        // Poses list
        rvPoses = view.findViewById(R.id.rv_poses);
        rvPoses.setLayoutManager(new LinearLayoutManager(requireContext()));
        poseAdapter = new PoseAdapter();
        rvPoses.setAdapter(poseAdapter);

        // Notes
        tvNotes = view.findViewById(R.id.tv_notes);

        // Share button
        btnShare = view.findViewById(R.id.btn_share);
    }

    private void setupObservers() {
        viewModel.getSession().observe(getViewLifecycleOwner(), this::updateUI);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show loading indicator if needed
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        // Share button
        btnShare.setOnClickListener(v -> {
            viewModel.shareSession();
        });
    }

    private void updateUI(YogaSession session) {
        if (session == null) return;

        // Update session header
        tvSessionName.setText(session.getTitle());
        tvSessionDate.setText(DateUtils.formatDateTime(session.getDate()));
        tvDuration.setText(formatDuration(session.getDuration()));
        tvCalories.setText(String.valueOf(session.getCalories()));
        tvPoses.setText(String.valueOf(session.getPoses().size()));

        // Update session score
        float score = session.getOverallScore();
        tvOverallScore.setText(String.format("%.1f", score));

        // Set score label based on score value
        String scoreLabel;
        if (score >= 8.5f) {
            scoreLabel = getString(R.string.excellent);
        } else if (score >= 7.0f) {
            scoreLabel = getString(R.string.good);
        } else if (score >= 5.0f) {
            scoreLabel = getString(R.string.average);
        } else {
            scoreLabel = getString(R.string.need_improvement);
        }
        tvScoreLabel.setText(scoreLabel);

        // Update poses list
        poseAdapter.setPoses(session.getPoses());

        // Update notes
        tvNotes.setText(session.getNotes());

        // Update radar chart
        // This would be implemented with actual data in a real app
        setupRadarChart();
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, mins, 0);
        } else {
            return String.format("%02d:%02d", mins, 0);
        }
    }

    private void setupRadarChart() {
        // In a real app, this would be configured with actual pose category scores
        // For now, just display an empty chart
        radarChart.clear();
        radarChart.setNoDataText("No pose data available");
        radarChart.setNoDataTextColor(getResources().getColor(R.color.textSecondary, null));
        radarChart.invalidate();
    }
}