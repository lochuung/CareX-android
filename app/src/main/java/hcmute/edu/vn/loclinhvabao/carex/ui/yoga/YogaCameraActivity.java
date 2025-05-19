package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.CameraManager;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.YogaAnalyzer;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.session.YogaSessionManager;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.PermissionHandler;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.TfLiteInitializer;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.UIAnimator;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.YogaPoseDialogHelper;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.PoseOverlayView;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.RecognitionPresenter;

@AndroidEntryPoint
public class YogaCameraActivity extends AppCompatActivity 
        implements PermissionHandler.PermissionCallback, 
                   YogaSessionManager.SessionStateListener,
                   TfLiteInitializer.InitializationCallback {

    private static final String TAG = "YogaCameraActivity";
    // Number of recognition results to show in the UI
    private static final int MAX_REPORT = 3;
    
    // Target pose tracking constants
    private static String TARGET_POSE = "cobra"; // Target pose to hold
    private static int TARGET_POSE_DURATION_SECONDS = 45; // How long user should hold the pose
    private static final float POSE_CONFIDENCE_THRESHOLD = 0.5f; // Minimum confidence to count as in pose
    
    // Pose tracking variables
    private long poseEnteredTimestamp = 0;
    private long currentPoseHoldTime = 0;
    private boolean isInTargetPose = false;
    private boolean hasCompletedPoseChallenge = false;
    private final Handler poseTrackingHandler = new Handler();
    private Runnable poseTrackingRunnable;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // UI Components
    private PreviewView viewFinder;
    private TextView textTimer;
    private TextView textTargetPose;
    private TextView textPoseProgress;
    private ProgressBar poseProgressBar;
    private ImageButton cameraSwitchButton;
    private ImageButton buttonInstructions;
    private MaterialButton buttonToggleSession;
    private PoseOverlayView poseOverlayView;
    private CardView cardPrediction;
    private CardView cardTimer;
    private CardView cardPoseChallenge;
    private ProgressBar timerProgress;
    
    // The classifier is created after initialization succeeded
    private YogaPoseClassifier yogaClassifier;

    // Core components 
    private CameraManager cameraManager;
    private RecognitionPresenter recognitionPresenter;
    private YogaAnalyzer analyzer;
    
    // Refactored helper components
    private PermissionHandler permissionHandler;
    private YogaSessionManager sessionManager;
    private TfLiteInitializer tfLiteInitializer;
    private YogaPoseDialogHelper dialogHelper;
    
    // Current recognized pose and confidence
    private String currentPoseName = "";
    private float currentConfidence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_camera);

        // Initialize UI components
        initUI();
        
        // Initialize helper components
        initHelpers();
        
        // Set up UI event listeners
        setupEventListeners();
        
        // Initialize TensorFlow Lite
        tfLiteInitializer.initialize(this);
        
        // Check and request camera permission
        permissionHandler.requestCameraPermissionIfNeeded();
    }

    @Override
    protected void onDestroy() {
        // Clean up session manager
        if (sessionManager != null) {
            sessionManager.onDestroy();
        }

        // Clean up pose tracking
        poseTrackingHandler.removeCallbacks(poseTrackingRunnable);

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
        super.onDestroy();
    }
    
    /**
     * Initialize UI components
     */
    private void initUI() {
        viewFinder = findViewById(R.id.view_finder);
        textTimer = findViewById(R.id.text_timer);
        cameraSwitchButton = findViewById(R.id.camera_switch_button);
        buttonInstructions = findViewById(R.id.button_instructions);
        buttonToggleSession = findViewById(R.id.button_toggle_session);
        poseOverlayView = findViewById(R.id.pose_overlay);
        cardPrediction = findViewById(R.id.card_prediction);
        cardTimer = findViewById(R.id.card_timer);
        timerProgress = findViewById(R.id.timer_progress);
        
        // Initialize pose challenge UI elements
        cardPoseChallenge = findViewById(R.id.card_pose_challenge);
        textTargetPose = findViewById(R.id.text_target_pose);
        textPoseProgress = findViewById(R.id.text_pose_progress);
        poseProgressBar = findViewById(R.id.pose_hold_progress);
        
        // Set up pose challenge UI
        if (textTargetPose != null) {
            textTargetPose.setText(TARGET_POSE);
        }
        if (textPoseProgress != null) {
            textPoseProgress.setText(String.format(Locale.getDefault(), "0/%d sec", TARGET_POSE_DURATION_SECONDS));
        }
        if (poseProgressBar != null) {
            poseProgressBar.setMax(TARGET_POSE_DURATION_SECONDS);
            poseProgressBar.setProgress(0);
        }
        
        // Apply animations to UI elements for a smooth entrance
        UIAnimator.animateEntranceElements(viewFinder, cardTimer, cardPrediction, 
                findViewById(R.id.button_panel));
        
        // Setup pose tracking runnable
        setupPoseTracking();

        TARGET_POSE = getIntent().getStringExtra("pose");
        TARGET_POSE_DURATION_SECONDS = getIntent().getIntExtra("time", 45);
    }
    
    /**
     * Initialize helper components
     */
    private void initHelpers() {
        // Initialize permission handler
        permissionHandler = new PermissionHandler(this, this);
        
        // Initialize session manager
        sessionManager = new YogaSessionManager(
                this, 
                textTimer, 
                timerProgress, 
                buttonToggleSession, 
                cardTimer,
                this);
        
        // Initialize TFLite initializer
        tfLiteInitializer = new TfLiteInitializer(this);
        
        // Initialize dialog helper
        dialogHelper = new YogaPoseDialogHelper(this);
    }
    
    /**
     * Set up UI event listeners
     */
    private void setupEventListeners() {
        // Set up back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // If session is active, show confirmation dialog
            if (sessionManager.isSessionActive()) {
                new AlertDialog.Builder(this)
                    .setTitle("End Session")
                    .setMessage("Do you want to end your current yoga session?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
            } else {
                finish();
            }
        });

        // Set up camera switch button
        cameraSwitchButton.setOnClickListener(v -> {
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
        buttonInstructions.setOnClickListener(v -> 
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
                    YogaCameraActivity.this,
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
        finish();
    }
    
    /**
     * Called when yoga session state changes
     */
    @Override
    public void onSessionStateChanged(boolean isActive) {
        Log.d(TAG, "Session state changed: " + (isActive ? "active" : "inactive"));
        // Update UI based on session state if needed
        buttonToggleSession.setEnabled(true);
    }

    /**
     * Set up the refactored components after classifier initialization.
     */
    private void setupComponents() {
        // Create RecognitionPresenter
        recognitionPresenter = new RecognitionPresenter(
                poseOverlayView,
                yogaClassifier);

        cameraManager = new CameraManager(
                this,
                this,
                viewFinder);

        analyzer = new YogaAnalyzer(
                yogaClassifier,
                poseOverlayView,
                cameraManager.isFrontCamera(),
                (recognitions -> {
                    // Run on UI thread to avoid threading issues
                    runOnUiThread(() -> {
                        // Update the UI with the recognition results
                        recognitionPresenter.displayResults(recognitions,
                                cameraManager.isFrontCamera());
                        
                        // Store current recognition information for instructions dialog
                        if (recognitions != null && !recognitions.isEmpty()) {
                            YogaPoseClassifier.Recognition topRecognition = recognitions.get(0);
                            currentPoseName = topRecognition.title();
                            currentConfidence = topRecognition.confidence();
                            
                            // Track pose for target pose challenge
                            handlePoseDetection(currentPoseName, currentConfidence);
                            
                            // Show pose duration container if confidence is high enough
                            View poseDurationContainer = findViewById(R.id.pose_duration_container);
                            if (poseDurationContainer != null) {
                                if (topRecognition.confidence() > 0.7f) {
                                    poseDurationContainer.setVisibility(View.VISIBLE);
                                    
                                    // Update progress bar using UIAnimator
                                    ProgressBar poseProgress = findViewById(R.id.pose_progress);
                                    UIAnimator.updateProgressWithConfidence(poseProgress, topRecognition.confidence(), cardPrediction);
                                } else {
                                    poseDurationContainer.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                })
        );

        cameraManager.setAnalyzer(analyzer);
        cameraManager.bindCameraUseCases();
    }
    
    /**
     * Set up pose tracking functionality
     */
    private void setupPoseTracking() {
        poseTrackingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isInTargetPose && !hasCompletedPoseChallenge) {
                    // Calculate elapsed time
                    long elapsedMillis = System.currentTimeMillis() - poseEnteredTimestamp;
                    int elapsedSeconds = (int) (elapsedMillis / 1000);
                    
                    // Update progress
                    currentPoseHoldTime = elapsedMillis;
                    
                    // Update UI
                    runOnUiThread(() -> updatePoseProgress(elapsedSeconds));
                    
                    // Log for debugging
                    Log.d(TAG, "Pose held for " + elapsedSeconds + " seconds");
                    
                    // Check if target reached
                    if (elapsedSeconds >= TARGET_POSE_DURATION_SECONDS) {
                        runOnUiThread(() -> onPoseChallengeCompleted());
                    } else {
                        // Continue tracking
                        poseTrackingHandler.postDelayed(this, 100);
                    }
                }
            }
        };
    }
    
    /**
     * Update the pose progress UI
     */
    private void updatePoseProgress(int elapsedSeconds) {
        if (textPoseProgress != null) {
            textPoseProgress.setText(String.format(Locale.getDefault(), 
                    "%d/%d sec", elapsedSeconds, TARGET_POSE_DURATION_SECONDS));
        }
        
        if (poseProgressBar != null) {
            poseProgressBar.setProgress(elapsedSeconds);
        }
    }
    
    /**
     * Handle pose detection and tracking
     */
    private void handlePoseDetection(String poseName, float confidence) {
        boolean isPoseDetected = poseName.equalsIgnoreCase(TARGET_POSE) && confidence >= POSE_CONFIDENCE_THRESHOLD;
        
        Log.d(TAG, String.format("Handling pose: %s (conf: %.2f) - Target: %s - In pose: %b - Completed: %b", 
                poseName, confidence, TARGET_POSE, isInTargetPose, hasCompletedPoseChallenge));
        
        // If already completed, don't do anything
        if (hasCompletedPoseChallenge) {
            return;
        }
        
        // Handle entering the pose
        if (isPoseDetected && !isInTargetPose) {
            isInTargetPose = true;
            poseEnteredTimestamp = System.currentTimeMillis();
            showToast("Started holding " + TARGET_POSE + " pose!");
            Log.d(TAG, "ENTERED TARGET POSE: " + TARGET_POSE);
            
            // Make pose challenge card more prominent
            if (cardPoseChallenge != null) {
                UIAnimator.animateCardScale(cardPoseChallenge);
            }
            
            // Start tracking
            poseTrackingHandler.post(poseTrackingRunnable);
        } 
        // Handle exiting the pose before completion
        else if (!isPoseDetected && isInTargetPose) {
            isInTargetPose = false;
            poseTrackingHandler.removeCallbacks(poseTrackingRunnable);
            Log.d(TAG, "EXITED TARGET POSE: " + TARGET_POSE);
            
            // Reset progress if held for less than half the target time
            int elapsedSeconds = (int) (currentPoseHoldTime / 1000);
            if (elapsedSeconds < TARGET_POSE_DURATION_SECONDS / 2) {
                updatePoseProgress(0);
                showToast("Lost " + TARGET_POSE + " pose. Starting over!");
            } else {
                // Keep progress but pause timer
                showToast("Paused at " + elapsedSeconds + " seconds. Return to the pose to continue!");
            }
        }
        // Debug pose detection state
        else if (isPoseDetected && isInTargetPose) {
            // Already in pose and still detected - timer is running
            Log.d(TAG, "HOLDING TARGET POSE: " + TARGET_POSE + " for " + (currentPoseHoldTime / 1000) + " seconds");
        }
    }
    
    /**
     * Called when the user successfully completes the pose challenge
     */
    private void onPoseChallengeCompleted() {
        hasCompletedPoseChallenge = true;
        
        // Update UI
        if (textPoseProgress != null) {
            textPoseProgress.setText("COMPLETED!");
        }
        
        // Show celebration
        showPoseChallengeCompletionDialog();
        
        // Vibrate to notify user
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }
    
    /**
     * Show completion dialog
     */
    private void showPoseChallengeCompletionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Challenge Complete!")
            .setMessage("Congratulations! You held the " + TARGET_POSE + 
                    " pose for " + TARGET_POSE_DURATION_SECONDS + " seconds.")
            .setPositiveButton("Awesome!", null)
            .show();
    }
    
    /**
     * Show a toast message
     */
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check camera permissions
        if (permissionHandler != null) {
            permissionHandler.requestCameraPermissionIfNeeded();
        }
        
        // Resume session manager if needed
        if (sessionManager != null) {
            sessionManager.onResume();
        }
        
        // Resume pose tracking if needed
        if (isInTargetPose && !hasCompletedPoseChallenge) {
            poseTrackingHandler.post(poseTrackingRunnable);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause session manager if needed
        if (sessionManager != null) {
            sessionManager.onPause();
        }
        
        // Pause pose tracking
        poseTrackingHandler.removeCallbacks(poseTrackingRunnable);
    }

}