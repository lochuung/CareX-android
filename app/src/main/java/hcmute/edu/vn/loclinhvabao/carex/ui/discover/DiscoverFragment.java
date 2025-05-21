package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.discover.DiscoverFragmentDirections;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Navigate to YogaCameraFragment using SafeArgs
        DiscoverFragmentDirections.ActionDiscoverFragmentToYogaCameraFragment action =
                DiscoverFragmentDirections.actionDiscoverFragmentToYogaCameraFragment()
                        .setPose("shoulder_stand")
                        .setTime(45);

        Navigation.findNavController(view).navigate(action);
    }
}
