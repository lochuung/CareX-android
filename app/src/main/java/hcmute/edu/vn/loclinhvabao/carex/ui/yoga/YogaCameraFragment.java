package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.CameraManager;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.YogaAnalyzer;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.observer.PoseRecognitionObserver;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.PermissionHandler;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.TfLiteInitializer;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.UIAnimator;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.YogaCameraUiBinder;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.YogaPoseDialogHelper;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.RecognitionPresenter;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel.YogaCameraViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.tracker.PoseTracker;

@AndroidEntryPoint
public class YogaCameraFragment extends Fragment
        implements PermissionHandler.PermissionCallback,
        TfLiteInitializer.InitializationCallback {

    private static final String TAG = "YogaCameraActivity";
    // Number of recognition results to show in the UI
    private static final int MAX_REPORT = 3;    // Target pose tracking constants
    private static String TARGET_POSE = "cobra"; // Target pose to hold
    private static int TARGET_POSE_DURATION_SECONDS = 45; // How long user should hold the pose
    private static final float POSE_CONFIDENCE_THRESHOLD = 0.5f; // Minimum confidence to count as in pose

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // UI Components
    private YogaCameraUiBinder ui;
    
    // Pose tracker
    private PoseTracker poseTracker;

    // The classifier is created after initialization succeeded
    private YogaPoseClassifier yogaClassifier;

    // Core components
    private CameraManager cameraManager;
    private RecognitionPresenter recognitionPresenter;
    private YogaAnalyzer analyzer;

    // Refactored helper components
    private PermissionHandler permissionHandler;
    private TfLiteInitializer tfLiteInitializer;
    private YogaPoseDialogHelper dialogHelper;

    // Current recognized pose and confidence
    private String currentPoseName = "";
    private float currentConfidence = 0;

    private View root;

    private YogaCameraViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize helper components
        initHelpers();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_yoga_camera, container, false);


        // Initialize UI components
        initUI();

        // Set up UI event listeners
        setupEventListeners();

        // Initialize TensorFlow Lite
        tfLiteInitializer.initialize(this);

        // Check and request camera permission
        permissionHandler.requestCameraPermissionIfNeeded();
        return root;
    }    @SuppressLint({"SetTextI18s", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(YogaCameraViewModel.class);

        // Observe the single uiState object instead of multiple LiveData objects
        viewModel.uiState.observe(getViewLifecycleOwner(), state -> {
            // Update UI based on state
            ui.textTargetPose.setText(TARGET_POSE.toUpperCase());
            ui.textPoseConfidence.setText((int) (state.getConfidence() * 100) + "%");
            ui.textPoseProgress.setText(state.getProgressSeconds() + "/" + TARGET_POSE_DURATION_SECONDS + "s");
            ui.poseProgressBar.setProgress(state.getProgressSeconds());
            
            // Show completion dialog if completed
            if (state.isCompleted()) {
                showPoseChallengeCompletionDialog();
            }
        });
    }@Override
    public void onDestroyView() {
        // Clean up pose tracking
        if (poseTracker != null) {
            poseTracker.stop();
        }

        // Terminate all outstanding analyzing jobs (if there is any)
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "Failed to terminate executor.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Exit was interrupted.", e);
        }

        // Release TFLite resources
        if (yogaClassifier != null) {
            yogaClassifier.close();
        }
        super.onDestroyView();
    }/**
     * Initialize UI components
     */
    private void initUI() {
        // Initialize UI binder with the root view
        ui = new YogaCameraUiBinder(root);
        
        // Set initial visibility states
        ui.setInitialVisibility();

        // Get the target pose and duration from intent first
        if (getArguments() != null) {
            TARGET_POSE = getArguments().getString("pose", TARGET_POSE);
            TARGET_POSE_DURATION_SECONDS = getArguments().getInt("time", 45);
        }

        // Set up pose challenge UI with correct values
        if (ui.textTargetPose != null) {
            ui.textTargetPose.setText(TARGET_POSE.toUpperCase());
        }
        if (ui.textPoseProgress != null) {
            ui.textPoseProgress.setText(String.format(Locale.getDefault(), "0/%d sec", TARGET_POSE_DURATION_SECONDS));
        }
        if (ui.poseProgressBar != null) {
            ui.poseProgressBar.setMax(TARGET_POSE_DURATION_SECONDS);
            ui.poseProgressBar.setProgress(0);
        }

        // Set up the pose status text
        if (ui.textPoseStatus != null) {
            ui.textPoseStatus.setText("WAITING");
            ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Apply animations to UI elements for a smooth entrance
        ui.animateEntrance();

        // Initialize pose tracker
        initPoseTracker();
    }

    /**
     * Initialize helper components
     */
    private void initHelpers() {
        // Initialize permission handler
        permissionHandler = new PermissionHandler(requireContext(), this, this);
        permissionHandler.initialize(this);

        // Initialize TFLite initializer
        tfLiteInitializer = new TfLiteInitializer(requireContext());

        // Initialize dialog helper
        dialogHelper = new YogaPoseDialogHelper(requireContext());
    }    /**
     * Set up UI event listeners
     */
    private void setupEventListeners() {
        // Set up back button
        ui.btnBack.setOnClickListener(v -> NavHostFragment
                .findNavController(this)
                .navigateUp());

        // Set up camera switch button
        ui.cameraSwitchButton.setOnClickListener(v -> {
            // Disable the button to prevent multiple clicks during switching
            v.setEnabled(false);

            // Use CameraManager to switch camera
            if (cameraManager != null) {
                cameraManager.switchCamera();
            }

            // Re-enable camera controls
            v.setEnabled(true);
        });

        // Set up instructions button
        ui.buttonInstructions.setOnClickListener(v ->
                dialogHelper.showInstructionsDialog(currentPoseName, currentConfidence));
    }

    /**
     * Called when TFLite initialization is complete
     */
    @Override
    public void onInitialized(boolean isGpuEnabled) {
        Log.d(TAG, "TFLite in Play Services initialized successfully with " +
                (isGpuEnabled ? "GPU" : "CPU") + " support");

        // Create YogaPoseClassifier AFTER TfLite.initialize() succeeded. This
        // guarantees that all the interactions with TFLite happens after initialization.
        try {
            yogaClassifier = YogaPoseClassifier.create(
                    requireContext(),
                    MAX_REPORT,
                    isGpuEnabled
            );

            // Initialize components after classifier is ready
            setupComponents();

        } catch (Exception e) {
            Log.e(TAG, "YogaPoseClassifier initialization error", e);
        }
    }

    /**
     * Called when TFLite initialization fails
     */
    @Override
    public void onInitializationFailed(Exception exception) {
        Log.e(TAG, "Failed to initialize the classifier.", exception);
    }

    /**
     * Called when camera permission is granted
     */
    @Override
    public void onPermissionGranted() {
        Log.d(TAG, "Camera permission granted");
        if (cameraManager != null) {
            cameraManager.bindCameraUseCases();
        }
    }

    /**
     * Called when camera permission is denied
     */
    @Override
    public void onPermissionDenied() {
        Log.e(TAG, "Camera permission denied");
        // Cannot continue without camera permission
        NavHostFragment.findNavController(this)
                .navigateUp();
    }

    /**
     * Set up the refactored components after classifier initialization.
     */
    @SuppressLint("SetTextI18n")
    private void setupComponents() {
        // Create RecognitionPresenter
        recognitionPresenter = new RecognitionPresenter(
                ui.poseOverlayView,
                yogaClassifier);

        // Set up camera manager
        cameraManager = new CameraManager(
                requireContext(),
                this,
                ui.viewFinder);

        // Set front camera flag to presenter
        recognitionPresenter.setFrontCamera(cameraManager.isFrontCamera());        // Create analyzer with observer pattern
        analyzer = new YogaAnalyzer(
                yogaClassifier,
                cameraManager.isFrontCamera(),
                // Pass the RecognitionPresenter as the observer
                recognitionPresenter
        );

        // Define what happens after pose recognition
        // Run on UI thread to avoid threading issues
        recognitionPresenter.setPostRecognitionCallback(recognitions -> 
            requireActivity().runOnUiThread(() -> processRecognitionResults(recognitions))
        );

        // Connect analyzer to camera manager
        cameraManager.setAnalyzer(analyzer);
        cameraManager.bindCameraUseCases();
    }
    
    /**
     * Process recognition results from the pose analyzer
     */
    @SuppressLint("SetTextI18n")
    private void processRecognitionResults(List<YogaPoseClassifier.Recognition> recognitions) {
        // Skip if no recognitions
        if (recognitions == null || recognitions.isEmpty()) {
            return;
        }
        
        // Get top recognition
        YogaPoseClassifier.Recognition topRecognition = recognitions.get(0);
        currentPoseName = topRecognition.title();
        currentConfidence = topRecognition.confidence();

        // Update the ViewModel
        viewModel.updatePose(currentPoseName, currentConfidence);

        // Track pose using PoseTracker
        poseTracker.update(currentPoseName, currentConfidence);

        // Update confidence status UI
        updateConfidenceDisplay(currentPoseName, currentConfidence);

        // Show pose duration container if confidence is high enough
        if (ui.poseDurationContainer != null) {
            // Show detected pose name and confidence
            if (ui.textDetectedPose != null) {
                ui.textDetectedPose.setText(currentPoseName.toUpperCase());
            }

            if (ui.textPoseConfidence != null) {
                int confidencePercent = (int) (currentConfidence * 100);
                ui.textPoseConfidence.setText(confidencePercent + "%");
            }

            // Always show the pose detection container
            ui.poseDurationContainer.setVisibility(View.VISIBLE);

            // Update progress bar using UIAnimator
            UIAnimator.updateProgressWithConfidence(
                ui.poseProgress, 
                topRecognition.confidence(), 
                ui.cardPrediction
            );
        }
    }

    /**
     * Update the pose progress UI
     */
    private void updatePoseProgress(int elapsedSeconds) {
        if (ui.textPoseProgress != null) {
            ui.textPoseProgress.setText(String.format(Locale.getDefault(),
                    "%d/%ds", elapsedSeconds, TARGET_POSE_DURATION_SECONDS));
        }

        if (ui.poseProgressBar != null) {
            ui.poseProgressBar.setProgress(elapsedSeconds);
        }
        
        // Update the ViewModel
        viewModel.updateProgress(elapsedSeconds);
    }

    /**
     * Called when the user successfully completes the pose challenge
     */    @SuppressLint({"SetTextI18s", "SetTextI18n"})
    private void onPoseChallengeCompleted() {
        // Update ViewModel
        viewModel.markCompleted();

        // Update UI
        if (ui.textPoseProgress != null) {
            ui.textPoseProgress.setText("DONE!");
            ui.textPoseProgress.setTextColor(getResources().getColor(android.R.color.white));
        }

        // Update status
        if (ui.textPoseStatus != null) {
            ui.textPoseStatus.setText("COMPLETED");
            ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light) & 0x33FFFFFF);
        }

        // Show completion icon
        if (ui.imageCompletion != null) {
            ui.imageCompletion.setVisibility(View.VISIBLE);

            // Add a small animation to make the icon more noticeable
            ui.imageCompletion.setAlpha(0f);
            ui.imageCompletion.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start();
        }

        // Show celebration
        showPoseChallengeCompletionDialog();

        // Vibrate to notify user
        VibratorManager vibratorManager = null;
        Vibrator vibrator = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager = (VibratorManager) requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /**
     * Show completion dialog
     */
    private void showPoseChallengeCompletionDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Challenge Complete!")
                .setMessage("Great job holding " + TARGET_POSE.toUpperCase() + " pose for " + TARGET_POSE_DURATION_SECONDS + "s!")
                .setPositiveButton("Continue", null)
                .show();
    }

    /**
     * Show a toast message
     */
    private void showToast(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }    @Override
    public void onResume() {
        super.onResume();

        // Check camera permissions
        if (permissionHandler != null) {
            permissionHandler.requestCameraPermissionIfNeeded();
        }

        // Resume pose tracking if needed
        if (poseTracker != null) {
            poseTracker.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop pose tracking
        if (poseTracker != null) {
            poseTracker.stop();
        }

        if (cameraManager != null) {
            cameraManager.stopCamera();
        }
    }

    /**
     * Initialize pose tracker with callbacks
     */
    private void initPoseTracker() {
        // Create pose tracker with callbacks for each state
        poseTracker = new PoseTracker(
                TARGET_POSE,
                TARGET_POSE_DURATION_SECONDS,
                POSE_CONFIDENCE_THRESHOLD,
                new PoseTracker.Callback() {
                    @Override
                    public void onPoseHoldProgress(int seconds) {
                        // Update UI with progress
                        requireActivity().runOnUiThread(() -> updatePoseProgress(seconds));
                    }

                    @Override
                    public void onPoseCompleted() {
                        // Handle pose completion
                        requireActivity().runOnUiThread(() -> onPoseChallengeCompleted());
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPosePaused() {
                        // Handle pose interruption
                        requireActivity().runOnUiThread(() -> {
                            if (ui.textPoseStatus != null) {
                                ui.textPoseStatus.setText("PAUSED");
                                ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light) & 0x33FFFFFF);
                            }
                            showToast("Pose paused! Return to the pose to continue.");
                        });
                    }

                    @Override
                    public void onPoseStarted() {
                        // Handle pose start
                        requireActivity().runOnUiThread(() -> {
                            if (ui.cardPoseChallenge != null) {
                                UIAnimator.animateCardScale(ui.cardPoseChallenge);
                            }
                            showToast("Started holding " + TARGET_POSE + " pose!");
                        });
                    }
                }
        );
    }

    /**
     * Update the pose confidence display based on current detection
     * 
     * @param poseName The detected pose name
     * @param confidence The confidence level
     */
    @SuppressLint("SetTextI18n")
    private void updateConfidenceDisplay(String poseName, float confidence) {
        if (confidence <= 0 || ui.textPoseStatus == null) {
            return;
        }

        // Check if pose matches the target pose
        if (poseName.equalsIgnoreCase(TARGET_POSE)) {
            int confidencePercent = (int) (confidence * 100);
            if (confidence >= POSE_CONFIDENCE_THRESHOLD) {
                ui.textPoseStatus.setText("HOLDING - " + confidencePercent + "%");
                ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light) & 0x33FFFFFF);
            } else {
                ui.textPoseStatus.setText("ALMOST - " + confidencePercent + "%");
                ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light) & 0x33FFFFFF);
            }
        } else {
            ui.textPoseStatus.setText("WRONG POSE");
            ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light) & 0x33FFFFFF);
        }
        
        // If the pose challenge is completed, always show "COMPLETED"
        if (poseTracker.isCompleted()) {
            ui.textPoseStatus.setText("COMPLETED");
            ui.textPoseStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            ui.textPoseStatus.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light) & 0x33FFFFFF);
        }
    }
}