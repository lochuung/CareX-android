package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

/**
 * Manager class for handling camera related operations.
 */
public class CameraManager {
    private static final String TAG = "CameraManager";

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;
    private final ExecutorService executor;

    @Getter
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    @Setter
    private ImageAnalysis.Analyzer analyzer;

    public CameraManager(Context context, LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public boolean isFrontCamera() {
        return cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA;
    }

    public void switchCamera() {
        // Toggle between front and back camera
        cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) ?
                CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

        if (analyzer != null && analyzer instanceof YogaAnalyzer yogaAnalyzer) {
            yogaAnalyzer.setFrontCamera(cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA);
        }

        // Re-bind camera use cases with the new camera selector
        bindCameraUseCases();
    }


    /**
     * Declare and bind preview and analysis use cases
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    public void bindCameraUseCases() {
        previewView.post(
                () -> {
                    ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                            ProcessCameraProvider.getInstance(context);
                    cameraProviderFuture.addListener(
                            () -> {
                                // Camera provider is now guaranteed to be available
                                ProcessCameraProvider cameraProvider;
                                try {
                                    cameraProvider = cameraProviderFuture.get();
                                } catch (ExecutionException | InterruptedException e) {
                                    Log.e(TAG, "Failed to get Camera.", e);
                                    return;
                                }

                                // Set up the view finder use case to display camera preview
                                Preview preview =
                                        new Preview.Builder()
                                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                                .setTargetRotation(
                                                        previewView
                                                                .getDisplay()
                                                                .getRotation())
                                                .build();

                                if (analyzer == null) {
                                    Log.e(TAG, "Analyzer is null. Cannot bind camera use cases.");
                                    return;
                                }

                                // Set up the image analysis use case which will process frames in real time
                                ImageAnalysis imageAnalysis =
                                        new ImageAnalysis.Builder()
                                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                                .setTargetRotation(
                                                        previewView.getDisplay().getRotation()
                                                )
                                                .setBackpressureStrategy(
                                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                                )
                                                .setOutputImageFormat(
                                                        ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                                                )
                                                .build();

                                imageAnalysis.setAnalyzer(executor, analyzer);

                                // Apply declared configs to CameraX using the same lifecycle owner
                                cameraProvider.unbindAll();
                                cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                );

                                // Use the camera object to link our preview use case with the view
                                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                            },
                            ContextCompat.getMainExecutor(context));
                });
    }
}
