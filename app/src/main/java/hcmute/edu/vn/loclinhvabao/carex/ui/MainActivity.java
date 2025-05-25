package hcmute.edu.vn.loclinhvabao.carex.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 123;

    private static final int[] SHOW_BOTTOM_NAVIGATION_IDS = {
            R.id.trainingFragment,
            R.id.discoverFragment,
            R.id.reportFragment,
            R.id.settingsFragment
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền thông báo
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    private void setupNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();        // Configure the top level destinations so that the back button doesn't appear
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.trainingFragment, R.id.homeFragment, R.id.discoverFragment, R.id.reportFragment, R.id.settingsFragment
        ).build();

        hideBottomNavigationView();

        // Connect the navController with the BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setPadding(0, 0, 0, 0); // thêm padding dưới theo hệ thống
            return insets;
        });
    }

    private void hideBottomNavigationView() {
        if (bottomNavigationView == null || navController == null) {
            return;
        }
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            for (int id : SHOW_BOTTOM_NAVIGATION_IDS) {
                if (destination.getId() == id) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    return;
                }
            }
            bottomNavigationView.setVisibility(View.GONE);
        });
    }

    // Xử lý kết quả
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - notifications can be shown
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied - show user message
                Toast.makeText(this, "Notification permission denied. You won't receive workout reminders.",
                        Toast.LENGTH_LONG).show();
                // You can add logic to guide user to settings to enable permission
            }
        }
    }
}