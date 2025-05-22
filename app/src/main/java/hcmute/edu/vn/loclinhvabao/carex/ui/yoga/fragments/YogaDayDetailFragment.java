package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.databinding.FragmentExerciseDetailBinding;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters.YogaPoseAdapter;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel.YogaViewModel;

/**
 * Fragment to display yoga day details with poses list
 */
@AndroidEntryPoint
public class YogaDayDetailFragment extends Fragment implements YogaPoseAdapter.OnPoseClickListener {

    private FragmentExerciseDetailBinding binding;
    private YogaViewModel viewModel;
    private YogaPoseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(YogaViewModel.class);

        // Set up RecyclerView
        adapter = new YogaPoseAdapter(this);
        binding.rvYogaPoses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvYogaPoses.setAdapter(adapter);

        // Set up navigation
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Set up start button
        binding.btnStartSession.setOnClickListener(v -> startYogaSession());

        // Observe selected day
        viewModel.getSelectedDay().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(YogaDay day) {
        if (day == null) {
            requireActivity().onBackPressed();
            return;
        }
        // Update header
        binding.tvDayTitle.setText(day.getTitle());
        binding.tvDayDescription.setText(day.getDescription());

        // Format duration
        int totalSeconds = day.getTotalDuration();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        binding.tvTotalTime.setText(
                String.format("Total Time: %d min %02d sec", minutes, seconds));

        // Update poses list
        binding.tvTotalPoses.setText(
                String.format("Total Poses: %d", day.getPoses().size()));
        adapter.submitList(day.getPoses());

        // Focus area and difficulty aren't in the current layout
        // We might need to add these fields to the UI in a future update
    }

    private void startYogaSession() {
        YogaDay selectedDay = viewModel.getSelectedDay().getValue();
        if (selectedDay == null) return;

        // In a real app, this would navigate to the yoga session screen
        Toast.makeText(requireContext(),
                "Starting yoga session: " + selectedDay.getTitle(),
                Toast.LENGTH_SHORT).show();

        // For this example, we'll simulate completing the session
        simulateCompletedSession(selectedDay);
    }

    private void simulateCompletedSession(YogaDay day) {
        // Simulate a completed yoga session with sample metrics
        int duration = day.getTotalDuration();
        int calories = (int) (duration * 0.15); // Simple calorie calculation
        float averageConfidence = 0.85f; // Sample confidence score

        // Mark the day as completed
        viewModel.completeDay(
                day,
                duration / 60, // Convert to minutes
                calories,
                averageConfidence,
                day.getPoses());

        // Show completion toast
        Toast.makeText(requireContext(),
                "Great job! You've completed " + day.getTitle(),
                Toast.LENGTH_LONG).show();

        // Navigate back to program list
        requireActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPoseClick(YogaPose pose) {
        // Handle pose click
        Toast.makeText(requireContext(),
                "Pose: " + pose.getEnglishName(),
                Toast.LENGTH_SHORT).show();

        // In a real app, you might navigate to a pose detail screen or show a dialog
    }
}
