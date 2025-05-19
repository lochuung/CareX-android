package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tflite.client.TfLiteInitializationOptions;
import com.google.android.gms.tflite.java.TfLite;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles TensorFlow Lite initialization.
 * Follows the Single Responsibility Principle by encapsulating TFLite initialization logic.
 */
public class TfLiteInitializer {
    private static final String TAG = "TfLiteInitializer";

    private final Context context;
    private Task<Void> initializeTask;
    private static volatile boolean isInitialized = false;

    public interface InitializationCallback {
        void onInitialized(boolean isGpuEnabled);

        void onInitializationFailed(Exception exception);
    }

    public TfLiteInitializer(Context context) {
        this.context = context;
    }

    /**
     * Initialize TensorFlow Lite with GPU support if available, falling back to CPU if not.
     */
    public void initialize(InitializationCallback callback) {
        if (initializeTask != null) {
            // Already initializing or initialized, attach the callback
            attachCallbackToExistingTask(callback);
            return;
        }
        if (isInitialized) {
            // Already initialized, call the callback immediately
            if (callback != null) {
                callback.onInitialized(false);
            }
            return;
        }

        AtomicBoolean isGpuInitialized = new AtomicBoolean(false);

        // Initialize TFLite asynchronously
        TfLiteInitializationOptions options = TfLiteInitializationOptions.builder()
                .setEnableGpuDelegateSupport(true)
                .build();

        initializeTask = TfLite.initialize(context, options)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        isGpuInitialized.set(true);
                        Log.d(TAG, "TFLite initialized with GPU support");
                        return Tasks.forResult(null);
                    } else {
                        // Fallback to initialize interpreter without GPU
                        isGpuInitialized.set(false);
                        Log.e(TAG, "Failed to initialize with GPU. Falling back to CPU", task.getException());
                        return TfLite.initialize(context);
                    }
                })
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "TFLite initialized successfully with " +
                            (isGpuInitialized.get() ? "GPU" : "CPU") + " support");
                    if (callback != null) {
                        callback.onInitialized(isGpuInitialized.get());
                        isInitialized = true;
                    }
                })
                .addOnFailureListener(err -> {
                    Log.e(TAG, "Failed to initialize TFLite", err);
                    if (callback != null) {
                        callback.onInitializationFailed(err);
                    }
                });
    }

    /**
     * Attach a callback to an existing initialization task.
     */
    private void attachCallbackToExistingTask(InitializationCallback callback) {
        if (callback == null) return;

        initializeTask.addOnSuccessListener(unused -> {
            // We don't know if GPU was enabled here since we're attaching to an existing task
            // The best we can do is to indicate success
            callback.onInitialized(false);
        }).addOnFailureListener(callback::onInitializationFailed);
    }

    /**
     * Check if TFLite has been initialized.
     */
    public boolean isInitialized() {
        return initializeTask != null && initializeTask.isComplete() && initializeTask.isSuccessful();
    }
}
