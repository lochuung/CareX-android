package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tflite.client.TfLiteInitializationOptions;
import com.google.android.gms.tflite.java.TfLite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.CameraManager;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera.YogaAnalyzer;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.PoseOverlayView;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.RecognitionPresenter;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;

@AndroidEntryPoint
public class YogaCameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    // Number of recognition results to show in the UI
    private static final int MAX_REPORT = 3;
    private static final String PERMISSION = Manifest.permission.CAMERA;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private PreviewView viewFinder;
    private TextView textPrediction;
    private ImageButton cameraSwitchButton;
    private PoseOverlayView poseOverlayView;

    // Initialize TFLite once
    private Task<Void> initializeTask;
    // The classifier is create after initialization succeeded
    private YogaPoseClassifier yogaClassifier;

    // Components for refactored architecture
    private CameraManager cameraManager;
    private RecognitionPresenter recognitionPresenter;
    private YogaAnalyzer classificationAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_camera);

        // Initialize UI components
        viewFinder = findViewById(R.id.view_finder);
        textPrediction = findViewById(R.id.text_prediction);
        cameraSwitchButton = findViewById(R.id.camera_switch_button);
        poseOverlayView = findViewById(R.id.pose_overlay);

        AtomicBoolean isGpuInitialized = new AtomicBoolean(false);

        if (initializeTask == null) {
            // Initialize TFLite asynchronously
            TfLiteInitializationOptions options = TfLiteInitializationOptions.builder()
                    .setEnableGpuDelegateSupport(true)
                    .build();

            initializeTask = TfLite.initialize(this, options)
                    .continueWithTask(task -> {
                        if (task.isSuccessful()) {
                            isGpuInitialized.set(true);
                            Log.d(TAG, "TFLite initialized with GPU support");
                            return Tasks.forResult(null);
                        } else {
                            // Fallback to initialize interpreter without GPU
                            isGpuInitialized.set(false);
                            Log.e(TAG, "Failed to initialize with GPU. Falling back to CPU", task.getException());
                            return TfLite.initialize(YogaCameraActivity.this);
                        }
                    })
                    .addOnSuccessListener(unused -> {
                        startInitialization(isGpuInitialized.get());
                    })
                    .addOnFailureListener(err -> {
                        Log.e(TAG, "Failed to initialize the classifier.", err);
                    });
        }

        // Request for permission
        requestPermissionLauncher = requestPermission();

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
    }

    private void startInitialization(boolean isGpuInitialized) {
        Log.d(TAG, "TFLite in Play Services initialized successfully.");
        // Create YogaPoseClassifier AFTER TfLite.initialize() succeeded. This
        // guarantees that all the interactions with TFLite happens after initialization.
        try {
            yogaClassifier = YogaPoseClassifier.create(
                    YogaCameraActivity.this,
                    MAX_REPORT,
                    isGpuInitialized
            );

            // Initialize components after classifier is ready
            setupComponents();

        } catch (Exception e) {
            Log.e(TAG, "YogaPoseClassifier initialization error", e);
        }
    }

    /**
     * Set up the refactored components after classifier initialization.
     */
    private void setupComponents() {
        // Create RecognitionPresenter
        recognitionPresenter = new RecognitionPresenter(
                textPrediction,
                poseOverlayView,
                yogaClassifier);

        cameraManager = new CameraManager(
                this,
                this,
                viewFinder);

        classificationAnalyzer = new YogaAnalyzer(
                yogaClassifier,
                poseOverlayView,
                cameraManager.isFrontCamera(),
                (recognitions -> {
                    // Update the UI with the recognition results
                    recognitionPresenter.displayResults(recognitions,
                            cameraManager.isFrontCamera());
                })

        );

        cameraManager.setAnalyzer(classificationAnalyzer);

        cameraManager.bindCameraUseCases();

        recognitionPresenter = new RecognitionPresenter(
                textPrediction,
                poseOverlayView,
                yogaClassifier
        );
    }

    @Override
    protected void onDestroy() {
        // Terminate all outstanding analyzing jobs (if there is any)
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "Failed to terminate.");
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

    @Override
    protected void onResume() {
        super.onResume();

        // Request permissions each time the app resumes, since they can be revoked at any time
        if (!hasPermission(this)) {
            requestPermissionLauncher.launch(PERMISSION);
        } else if (cameraManager != null) {
            cameraManager.bindCameraUseCases();
        }
    }
    /**
     * Registers request permission callback.
     */
    private ActivityResultLauncher<String> requestPermission() {
        return registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        if (cameraManager != null) {
                            cameraManager.bindCameraUseCases();
                        }
                    } else {
                        finish(); // If we don't have the required permissions, we can't run
                    }
                });
    }

    /**
     * Convenience method used to check if all permissions required by this app are granted.
     */
    private boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }
}