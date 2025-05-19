package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
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
    }

    private void initUI(View view) {
        TextView tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvProgress = view.findViewById(R.id.tv_progress);
        btnCurrentDay = view.findViewById(R.id.btn_current_day);
        rvYogaDays = view.findViewById(R.id.rv_yoga_days);
        
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
        
        // Update progress text
        tvProgress.setText(String.format("Progress: %d/10 days completed", completedDays));
        
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
}
