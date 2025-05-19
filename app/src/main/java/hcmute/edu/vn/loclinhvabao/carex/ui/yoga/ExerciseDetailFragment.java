package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
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

import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.shared.SharedViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters.YogaPoseAdapter;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

@AndroidEntryPoint
public class ExerciseDetailFragment extends Fragment implements YogaPoseAdapter.OnPoseClickListener {

    private SharedViewModel sharedViewModel;
    private TextView tvDayTitle;
    private TextView tvDayDescription;
    private TextView tvTotalTime;
    private TextView tvTotalPoses;
    private RecyclerView rvYogaPoses;
    private YogaPoseAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
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
    }

    private void initUI(View view) {
        tvDayTitle = view.findViewById(R.id.tv_day_title);
        tvDayDescription = view.findViewById(R.id.tv_day_description);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        tvTotalPoses = view.findViewById(R.id.tv_total_poses);
        MaterialButton btnStartSession = view.findViewById(R.id.btn_start_session);
        rvYogaPoses = view.findViewById(R.id.rv_yoga_poses);
        ImageView ivBack = view.findViewById(R.id.iv_back);
        
        btnStartSession.setOnClickListener(v -> startYogaSession());
        ivBack.setOnClickListener(v -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void setupRecyclerView() {
        adapter = new YogaPoseAdapter(this);
        rvYogaPoses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvYogaPoses.setAdapter(adapter);
    }

    private void observeViewModel() {
        sharedViewModel.getSelectedYogaDay().observe(getViewLifecycleOwner(), this::updateUI);
    }
    
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateUI(YogaDay yogaDay) {
        if (yogaDay == null) return;
        
        tvDayTitle.setText(yogaDay.getTitle());
        tvDayDescription.setText(yogaDay.getDescription());
        
        int totalSeconds = yogaDay.getTotalDuration();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        tvTotalTime.setText(String.format("Total Time: %d min %02d sec", minutes, seconds));
        
        tvTotalPoses.setText("Total Poses: " + yogaDay.getTotalPoses());
        
        adapter.submitList(yogaDay.getPoses());
    }
    
    private void startYogaSession() {
        // Get the first yoga pose from the selected day
        YogaDay selectedDay = sharedViewModel.getSelectedYogaDay().getValue();
        if (selectedDay != null && !selectedDay.getPoses().isEmpty()) {
            YogaPose firstPose = selectedDay.getPoses().get(0);
            sharedViewModel.selectYogaPose(firstPose);
            
            // Navigate to lesson start fragment
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_exerciseDetailFragment_to_lessonStartFragment);
        }
    }

    @Override
    public void onPoseClick(YogaPose pose) {
        // Set selected pose in ViewModel
        sharedViewModel.selectYogaPose(pose);
        
        // Navigate to lesson start fragment
        Navigation.findNavController(requireView())
                .navigate(R.id.action_exerciseDetailFragment_to_lessonStartFragment);
    }
}
