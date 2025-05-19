package hcmute.edu.vn.loclinhvabao.carex.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;

/**
 * Class responsible for running pose detection using the MoveNet model.
 */
public class PoseDetector implements Closeable {
    private static final String TAG = "PoseDetector";
    
    // MoveNet model path
    private static final String MOVENET_MODEL_PATH = "movenet_thunder.tflite";
    
    // Threshold for confidence
    private static final float MIN_KEYPOINT_SCORE = 0.25f;
    
    private final InterpreterApi poseInterpreter;
    
    /**
     * Creates a PoseDetector instance.
     *
     * @param context The application context
     * @param options The interpreter options to use
     * @return A new PoseDetector instance
     * @throws IOException If the model cannot be loaded
     */
    public static PoseDetector create(Context context, InterpreterApi.Options options) throws IOException {
        // Load MoveNet model
        InterpreterApi poseInterpreter =
                InterpreterApi.create(FileUtil.loadMappedFile(context, MOVENET_MODEL_PATH), options);
        
        return new PoseDetector(poseInterpreter);
    }
    
    private PoseDetector(InterpreterApi poseInterpreter) {
        this.poseInterpreter = poseInterpreter;
    }
    
    /**
     * Runs the MoveNet model to detect pose.
     *
     * @param bitmap The input image
     * @return A Person object with detected keypoints or null if detection failed
     */
    public Person detectPose(Bitmap bitmap) {
        // Convert bitmap to ByteBuffer
        ByteBuffer imageBuffer = ImageUtils.convertBitmapToByteBuffer(bitmap);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        
        return detectPose(imageBuffer, imageWidth, imageHeight);
    }
    
    /**
     * Runs the MoveNet model to detect pose from a ByteBuffer.
     *
     * @param imageBuffer The input image as a ByteBuffer
     * @param imageWidth The width of the image
     * @param imageHeight The height of the image
     * @return A Person object with detected keypoints or null if detection failed
     */
    public Person detectPose(ByteBuffer imageBuffer, int imageWidth, int imageHeight) {
        try {
            // Get input and output tensors for pose model
            int[] inputShape = poseInterpreter.getInputTensor(0).shape();
            DataType inputType = poseInterpreter.getInputTensor(0).dataType();

            // Log the input shape for debugging
            Log.d(TAG, "MoveNet input shape: " + Arrays.toString(inputShape));
            Log.d(TAG, "ImageBuffer capacity: " + imageBuffer.capacity() + " bytes");

            // Check if model input is quantized (uint8) or float
            boolean isQuantizedInput = inputType == DataType.UINT8;
            Log.d(TAG, "MoveNet input is quantized: " + isQuantizedInput);

            // Resize and prepare the input image for MoveNet
            TensorBuffer inputTensor = TensorBuffer.createFixedSize(inputShape, inputType);

            // Apply appropriate preprocessing based on model's expected input type
            if (isQuantizedInput) {
                // For quantized models (uint8), the image is already in the right format
                inputTensor.loadBuffer(imageBuffer);
            } else {
                // For float models, ensure values are between 0-1
                // We assume imageBuffer already contains values in the correct range
                inputTensor.loadBuffer(imageBuffer);
            }

            // Prepare output for MoveNet (keypoints with scores)
            // Output shape is [1, 1, 17, 3] where 3 values per keypoint are: [y, x, confidence]
            float[][][][] output = new float[1][1][17][3];
            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, output);

            // Run inference
            poseInterpreter.runForMultipleInputsOutputs(new Object[]{inputTensor.getBuffer()}, outputMap);

            // Log first few keypoints for debugging
            StringBuilder sb = new StringBuilder("First 3 keypoints: ");
            for (int i = 0; i < 3 && i < output[0][0].length; i++) {
                sb.append(String.format("[y=%.2f, x=%.2f, conf=%.2f] ",
                        output[0][0][i][0], output[0][0][i][1], output[0][0][i][2]));
            }
            Log.d(TAG, sb.toString());

            // Debug: Calculate statistical information about the keypoints
            if (Math.random() < 0.05) {  // Only log occasionally (~5% of frames)
                float totalConfidence = 0;
                int validKeypoints = 0;

                for (int i = 0; i < output[0][0].length; i++) {
                    if (output[0][0][i][2] >= MIN_KEYPOINT_SCORE) {
                        totalConfidence += output[0][0][i][2];
                        validKeypoints++;
                    }
                }

                float avgConfidence = validKeypoints > 0 ? totalConfidence / validKeypoints : 0;
                Log.d(TAG, String.format("Keypoint stats: %d/%d valid, avg confidence: %.2f",
                        validKeypoints, output[0][0].length, avgConfidence));
            }

            // Convert output to Person object
            Person person = Person.fromKeyPointsWithScores(
                    output[0],
                    imageHeight,
                    imageWidth,
                    MIN_KEYPOINT_SCORE);

            return person;
        } catch (Exception e) {
            Log.e(TAG, "Error in pose detection", e);
            return null;
        }
    }
    
    @Override
    public void close() {
        if (poseInterpreter != null) {
            poseInterpreter.close();
        }
    }
}