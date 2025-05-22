package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.databinding.FragmentYogaProgramBinding;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters.YogaDayAdapter;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.fragments.YogaDayDetailFragment;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel.YogaViewModel;

/**
 * Fragment to display the yoga program with days list
 */
@AndroidEntryPoint
public class YogaProgramFragment extends Fragment implements YogaDayAdapter.OnDayClickListener {

    private FragmentYogaProgramBinding binding;
    private YogaViewModel viewModel;
    private YogaDayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentYogaProgramBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(YogaViewModel.class);
          // Set up RecyclerView
        adapter = new YogaDayAdapter(this);
        binding.recyclerViewYogaDays.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewYogaDays.setAdapter(adapter);
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe yoga days
        viewModel.getAvailableDays().observe(getViewLifecycleOwner(), yogaDays -> {
            adapter.submitList(yogaDays);
            binding.emptyStateView.setVisibility(yogaDays.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }    @Override
    public void onDayClick(YogaDay day) {
        // Handle day selection
        viewModel.selectDay(day);
        
        // Navigate to day detail screen
        navigateToDayDetail();
    }
    
    private void navigateToDayDetail() {
        // Create and show the YogaDayDetailFragment
        YogaDayDetailFragment detailFragment = new YogaDayDetailFragment();
        
        // Use parent fragment manager for transaction
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
