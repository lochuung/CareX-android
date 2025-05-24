package hcmute.edu.vn.loclinhvabao.carex.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.UserProfileRepository;
import hcmute.edu.vn.loclinhvabao.carex.ui.MainActivity;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // TODO: Add Google icon drawable (ic_google) to res/drawable folder

    private LoginViewModel viewModel;
    private MaterialButton btnGoogleSignIn;
    private CircularProgressIndicator progressIndicator;

    @Inject
    UserProfileRepository userProfileRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initUI();
        setupObservers();
        checkExistingAuth();
    }

    private void initUI() {
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        progressIndicator = findViewById(R.id.progress_indicator);

        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void setupObservers() {
        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressIndicator.setVisibility(View.VISIBLE);
                btnGoogleSignIn.setEnabled(false);
            } else {
                progressIndicator.setVisibility(View.GONE);
                btnGoogleSignIn.setEnabled(true);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isFullyAuthenticated().observe(this, isAuthenticated -> {
            if (isAuthenticated) {
                syncUserDataAndNavigate();
            }
        });
    }

    private void checkExistingAuth() {
        if (viewModel.isUserAlreadySignedIn()) {
            navigateToMain();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = viewModel.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            viewModel.handleSignInResult(data);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void syncUserDataAndNavigate() {
        // Get user info from Firebase/Google
        String userId = viewModel.getCurrentUserId();
        String userName = viewModel.getCurrentUserName();
        String userEmail = viewModel.getCurrentUserEmail();

        if (userId != null && userName != null) {
            // Sync user profile data
            userProfileRepository.syncUserProfile(userId, userName, userEmail);
        }

        navigateToMain();
    }
}
