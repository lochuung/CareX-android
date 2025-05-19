package hcmute.edu.vn.loclinhvabao.carex.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.InterpreterApi.Options.TfLiteRuntime;
import org.tensorflow.lite.gpu.GpuDelegateFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;
import lombok.Getter;

/**
 * Helper class used to communicate between our app and the TF yoga pose classification model.
 * This class coordinates the overall pipeline using specialized components:
 * - PoseDetector: For detecting human poses using MoveNet
 * - LandmarkNormalizer: For normalizing detected keypoints
 * - YogaModelInterpreter: For classifying yoga poses based on normalized landmarks
 */
public class YogaPoseClassifier implements Closeable {
    public static final int INPUT_SIZE = 256; // Input size for MoveNet model
    private static final String TAG = "YogaPoseClassifier";

    // Threshold for confidence
    private static final float MIN_KEYPOINT_SCORE = 0.25f;

    private final Context context;
    private final int maxResults;
    
    // Component instances
    private final PoseDetector poseDetector;
    private final LandmarkNormalizer landmarkNormalizer;
    private final YogaModelInterpreter yogaModelInterpreter;

    // Reusing the Recognition record from YogaModelInterpreter
    public static record Recognition(String title, Float confidence) {
    }    /**
     * Factory method to create an instance of {@code YogaPoseClassifier}.
     */
    public static YogaPoseClassifier create(
            Context context,
            int maxResults,
            boolean isGpuInitialized
    ) throws IOException {
        // Use TFLite in Play Services runtime by setting the option to FROM_SYSTEM_ONLY
        InterpreterApi.Options options = new InterpreterApi
                .Options()
                .setRuntime(TfLiteRuntime.FROM_SYSTEM_ONLY);

        if (isGpuInitialized) {
            options.addDelegateFactory(new GpuDelegateFactory());
        }

        // Create component instances
        PoseDetector poseDetector = PoseDetector.create(context, options);
        LandmarkNormalizer landmarkNormalizer = new LandmarkNormalizer();
        YogaModelInterpreter yogaModelInterpreter = YogaModelInterpreter.create(context, maxResults, options);

        return new YogaPoseClassifier(
                context,
                maxResults,
                poseDetector,
                landmarkNormalizer,
                yogaModelInterpreter);
    }

    private YogaPoseClassifier(
            Context context,
            int maxResults,
            PoseDetector poseDetector,
            LandmarkNormalizer landmarkNormalizer,
            YogaModelInterpreter yogaModelInterpreter) {
        this.context = context;
        this.maxResults = maxResults;
        this.poseDetector = poseDetector;
        this.landmarkNormalizer = landmarkNormalizer;
        this.yogaModelInterpreter = yogaModelInterpreter;
    }    /**
     * Runs the pose estimation and yoga classification pipeline.
     */
    public List<Recognition> classifyPose(Bitmap bitmap) {
        // convert bitmap to ByteBuffer
        ByteBuffer imageBuffer = ImageUtils.convertBitmapToByteBuffer(bitmap);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        // Run pose detection
        Person person = poseDetector.detectPose(imageBuffer, imageWidth, imageHeight);

        if (person == null || person.getScore() < MIN_KEYPOINT_SCORE) {
            // Return empty list if no person with enough confidence was detected
            return new ArrayList<>();
        }

        // Convert pose to normalized input for yoga model
        float[] normalizedLandmarks = landmarkNormalizer.normalizeLandmarks(person);
        
        // Validate landmarks to ensure they are valid for the model
        normalizedLandmarks = landmarkNormalizer.validateLandmarks(normalizedLandmarks);

        // Run yoga pose classification
        List<Recognition> recognitions = yogaModelInterpreter.classifyYogaPose(normalizedLandmarks);

        // Store the last detected person for visualization
        lastDetectedPerson = person;

        return recognitions;
    }    @Getter
    private Person lastDetectedPerson = null;

    /**
     * Releases TFLite resources.
     */
    @Override
    public void close() {
        poseDetector.close();
        yogaModelInterpreter.close();
    }
}
