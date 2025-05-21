package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.constant.PoseConstants;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.observer.PoseRecognitionObserver;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Image analyzer for yoga pose detection
 * Uses PoseRecognitionObserver pattern for callback instead of lambda
 */
public class YogaAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "YogaAnalyzer";

    private final YogaPoseClassifier classifier;
    private final PoseRecognitionObserver observer;
    private boolean isFrontCamera;

    @Getter
    private Bitmap bitmapBuffer;
    @Getter
    @Setter
    private int imageRotationDegrees = 0;
    @Setter
    @Getter
    private boolean pauseAnalysis = false;

    /**
     * Creates a new YogaAnalyzer
     * 
     * @param classifier The classifier instance for yoga pose detection
     * @param isFrontCamera Whether front camera is being used
     * @param observer The observer to receive pose recognition events
     */
    public YogaAnalyzer(YogaPoseClassifier classifier,
                       boolean isFrontCamera,
                       PoseRecognitionObserver observer) {
        this.classifier = classifier;
        this.isFrontCamera = isFrontCamera;
        this.observer = observer;
    }

    public boolean isFrontCamera() {
        return isFrontCamera;
    }

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        try (image) {
            if (bitmapBuffer == null) {
                // The image rotation and RGB image buffer are initialized only after the analyzer has
                // started running
                imageRotationDegrees = image.getImageInfo().getRotationDegrees();
                bitmapBuffer =
                        Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            }

            // Early exit: image analysis is in paused state, or TFLite initialization has not finished
            if (pauseAnalysis || classifier == null) {
                image.close();
                return;
            }

            // Copy out RGB bits to our shared buffer
            bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());

            // Process image data based on camera
            Bitmap processedBitmap = !isFrontCamera ? bitmapBuffer
                    : ImageUtils.flipHorizontally(bitmapBuffer);

            // Handle rotation if needed
            if (imageRotationDegrees != 0) {
                Bitmap rotatedBitmap = ImageUtils.rotate(processedBitmap, imageRotationDegrees);

                // Clean up intermediate bitmap if needed
                if (processedBitmap != bitmapBuffer) {
                    processedBitmap.recycle();
                }
                processedBitmap = rotatedBitmap;
            }            // Resize bitmap to the input size expected by the TensorFlow model
            Bitmap resizedBitmap = ImageUtils.resize(
                    processedBitmap,
                    PoseConstants.INPUT_SIZE,
                    PoseConstants.INPUT_SIZE);
                    
            // Perform the yoga pose classification for the current frame
            List<YogaPoseClassifier.Recognition> recognitions = classifier.classifyPose(resizedBitmap);

            // Clean up the processed bitmap if it's different from bitmapBuffer
            if (processedBitmap != bitmapBuffer) {
                processedBitmap.recycle();
            }
            if (resizedBitmap != processedBitmap) {
                resizedBitmap.recycle();
            }

            // Notify observer of recognition results
            if (observer != null) {
                observer.onPoseRecognized(recognitions);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing image", e);
        }
    }
}