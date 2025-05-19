package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.YogaCameraActivity;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize UI components
        // intent to YogaCameraActivity
        // {time:45, pose: "cabra"}

        Intent intent = new Intent(getActivity(), YogaCameraActivity.class);
        intent.putExtra("time", 45);
        intent.putExtra("pose", "cabra");
        startActivity(intent);
    }
}
