package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.util.List;
import java.util.function.Consumer;

import hcmute.edu.vn.loclinhvabao.carex.constant.PoseConstants;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.PoseOverlayView;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;
import lombok.Getter;
import lombok.Setter;

public class YogaAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "ClassificationAnalyzer";

    private final YogaPoseClassifier classifier;
    private final PoseOverlayView overlayView;
    private boolean isFrontCamera;
    private final Consumer<List<YogaPoseClassifier.Recognition>> onResult;

    @Getter
    private Bitmap bitmapBuffer;
    @Getter
    @Setter
    private int imageRotationDegrees = 0;
    @Setter
    @Getter
    private boolean pauseAnalysis = false;

    public YogaAnalyzer(YogaPoseClassifier classifier,
                        PoseOverlayView overlayView,
                        boolean isFrontCamera,
                        Consumer<List<YogaPoseClassifier.Recognition>> onResult) {
        this.classifier = classifier;
        this.overlayView = overlayView;
        this.isFrontCamera = isFrontCamera;
        this.onResult = onResult;
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
            }

            // Resize bitmap to the input size expected by the TensorFlow model
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

            onResult.accept(recognitions);

            Person person = classifier.getLastDetectedPerson();
            if (person != null) {
                overlayView.setKeyPoints(person.getKeyPoints());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing image", e);
        }
    }


}