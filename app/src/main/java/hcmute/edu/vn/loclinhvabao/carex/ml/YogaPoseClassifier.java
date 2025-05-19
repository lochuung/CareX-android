/*
 * Copyright 2022 The TensorFlow Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hcmute.edu.vn.loclinhvabao.carex.ml;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.InterpreterApi.Options.TfLiteRuntime;
import org.tensorflow.lite.gpu.GpuDelegateFactory;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.BodyPart;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.KeyPoint;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.util.ImageUtils;
import lombok.Getter;

/**
 * Helper class used to communicate between our app and the TF yoga pose classification model.
 */
public class YogaPoseClassifier implements Closeable {
    public static final int INPUT_SIZE = 256; // Input size for MoveNet model
    private static final String TAG = "YogaPoseClassifier";

    // Expected landmarks count (17 keypoints with x,y for each)
    private static final int EXPECTED_KEYPOINTS = 17;
    private static final int EXPECTED_LANDMARKS = EXPECTED_KEYPOINTS * 2; // 34 values total

    // MoveNet (pose detection) model
    private static final String MOVENET_MODEL_PATH = "movenet_thunder.tflite";

    // Yoga classification model
    private static final String YOGA_MODEL_PATH = "yoga_model.tflite";
    //    private static final String YOGA_CLASS_NAMES_PATH = "yoga_class_names.json";
    private static final String[] YOGA_CLASS_NAMES = {
            "chair", "cobra", "dog", "no_pose", "shoudler_stand", "traingle", "tree", "warrior"
    };

    // Threshold for confidence
    private static final float MIN_KEYPOINT_SCORE = 0.25f;

    private final Context context;
    private final InterpreterApi poseInterpreter;
    private final InterpreterApi yogaInterpreter;
    private final List<String> yogaPoseClasses;
    private final int maxResults;
    private final TensorBuffer yogaOutputBuffer;

    public record Recognition(String title, Float confidence) {
    }

    /**
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

        // Load MoveNet model
        InterpreterApi poseInterpreter =
                InterpreterApi.create(FileUtil.loadMappedFile(context, MOVENET_MODEL_PATH), options);

        // Load Yoga classification model
        InterpreterApi yogaInterpreter =
                InterpreterApi.create(FileUtil.loadMappedFile(context, YOGA_MODEL_PATH), options);

        // Get output tensor info
        int[] outputShape = yogaInterpreter.getOutputTensor(0).shape();
        DataType outputDataType = yogaInterpreter.getOutputTensor(0).dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        // Read class names from JSON file
        List<String> classNames = loadYogaClassNames(context);

        return new YogaPoseClassifier(
                context,
                maxResults,
                poseInterpreter,
                yogaInterpreter,
                classNames,
                outputBuffer);
    }

    private static List<String> loadYogaClassNames(Context context) throws IOException {
        return new ArrayList<>(Arrays.asList(YOGA_CLASS_NAMES));
    }

    private YogaPoseClassifier(
            Context context,
            int maxResults,
            InterpreterApi poseInterpreter,
            InterpreterApi yogaInterpreter,
            List<String> yogaPoseClasses,
            TensorBuffer yogaOutputBuffer) {
        this.context = context;
        this.maxResults = maxResults;
        this.poseInterpreter = poseInterpreter;
        this.yogaInterpreter = yogaInterpreter;
        this.yogaPoseClasses = yogaPoseClasses;
        this.yogaOutputBuffer = yogaOutputBuffer;
    }

    /**
     * Runs the pose estimation and yoga classification pipeline.
     */
    public List<Recognition> classifyPose(Bitmap bitmap) {
        // convert bitmap to ByteBuffer
        ByteBuffer imageBuffer = ImageUtils.convertBitmapToByteBuffer(bitmap);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        // Run pose detection
        Person person = detectPose(imageBuffer, imageWidth, imageHeight);

        if (person == null || person.getScore() < MIN_KEYPOINT_SCORE) {
            // Return empty list if no person with enough confidence was detected
            return new ArrayList<>();
        }

        // Convert pose to normalized input for yoga model
        float[] normalizedLandmarks = normalizeLandmarks(person);
        // (17 keypoints * 2 coordinates per keypoint = 34 values)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(normalizedLandmarks.length * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float value : normalizedLandmarks) {
            inputBuffer.putFloat(value);
        }
        inputBuffer.rewind();

        // Run yoga pose classification
        List<Recognition> recognitions = classifyYogaPose(normalizedLandmarks);

        // Store the last detected person for visualization
        lastDetectedPerson = person;

        return recognitions;
    }

    @Getter
    private Person lastDetectedPerson = null;

    /**
     * Runs the MoveNet model to detect pose.
     */
    private Person detectPose(ByteBuffer imageBuffer, int imageWidth, int imageHeight) {
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

    /**
     * Normalize landmarks similar to Python code for model input.
     */
    @SuppressLint("DefaultLocale")
    private float[] normalizeLandmarks(Person person) {
        List<KeyPoint> keypoints = person.getKeyPoints();

        // MoveNet outputs 17 keypoints, each with x,y coordinates
        // We need exactly 34 float values (17 keypoints × 2 coordinates)
        float[] landmarks = new float[34]; // 17 keypoints * 2 values (x,y) per keypoint

        // Initialize all landmarks to zero
        for (int i = 0; i < landmarks.length; i++) {
            landmarks[i] = 0.0f;
        }

        // Extract coordinates of all keypoints
        float[][] rawKeypoints = new float[keypoints.size()][2];
        for (int i = 0; i < keypoints.size(); i++) {
            KeyPoint keypoint = keypoints.get(i);
            rawKeypoints[i][0] = keypoint.getCoordinate().x; // x
            rawKeypoints[i][1] = keypoint.getCoordinate().y; // y
        }

        // Get hip center - essential for normalization
        KeyPoint leftHip = person.getKeyPoint(BodyPart.LEFT_HIP);
        KeyPoint rightHip = person.getKeyPoint(BodyPart.RIGHT_HIP);

        PointF hipCenter = new PointF();
        if (leftHip != null && rightHip != null &&
                leftHip.getScore() >= MIN_KEYPOINT_SCORE &&
                rightHip.getScore() >= MIN_KEYPOINT_SCORE) {
            // Use average of both hips if available with sufficient confidence
            hipCenter.x = (leftHip.getCoordinate().x + rightHip.getCoordinate().x) / 2;
            hipCenter.y = (leftHip.getCoordinate().y + rightHip.getCoordinate().y) / 2;
        } else if (leftHip != null && leftHip.getScore() >= MIN_KEYPOINT_SCORE) {
            // Fall back to left hip if available
            hipCenter = leftHip.getCoordinate();
        } else if (rightHip != null && rightHip.getScore() >= MIN_KEYPOINT_SCORE) {
            // Fall back to right hip if available
            hipCenter = rightHip.getCoordinate();
        } else {
            // If no hips detected with sufficient confidence, use average of all valid keypoints
            float sumX = 0, sumY = 0;
            int count = 0;
            for (KeyPoint keypoint : keypoints) {
                if (keypoint != null && keypoint.getScore() >= MIN_KEYPOINT_SCORE) {
                    sumX += keypoint.getCoordinate().x;
                    sumY += keypoint.getCoordinate().y;
                    count++;
                }
            }
            hipCenter.x = count > 0 ? sumX / count : 0.5f;
            hipCenter.y = count > 0 ? sumY / count : 0.5f;
        }

        // Calculate shoulder center for torso size estimation
        KeyPoint leftShoulder = person.getKeyPoint(BodyPart.LEFT_SHOULDER);
        KeyPoint rightShoulder = person.getKeyPoint(BodyPart.RIGHT_SHOULDER);

        PointF shoulderCenter = new PointF();
        if (leftShoulder != null && rightShoulder != null &&
                leftShoulder.getScore() >= MIN_KEYPOINT_SCORE &&
                rightShoulder.getScore() >= MIN_KEYPOINT_SCORE) {
            // Use average of both shoulders if available with sufficient confidence
            shoulderCenter.x = (leftShoulder.getCoordinate().x + rightShoulder.getCoordinate().x) / 2;
            shoulderCenter.y = (leftShoulder.getCoordinate().y + rightShoulder.getCoordinate().y) / 2;
        } else if (leftShoulder != null && leftShoulder.getScore() >= MIN_KEYPOINT_SCORE) {
            // Fall back to left shoulder if available
            shoulderCenter = leftShoulder.getCoordinate();
        } else if (rightShoulder != null && rightShoulder.getScore() >= MIN_KEYPOINT_SCORE) {
            // Fall back to right shoulder if available
            shoulderCenter = rightShoulder.getCoordinate();
        } else {
            // Default to hip center if no shoulders with sufficient confidence
            shoulderCenter = hipCenter;
        }

        // Calculate torso size as distance between shoulder and hip center
        float torsoSize = (float) Math.sqrt(
                Math.pow(shoulderCenter.x - hipCenter.x, 2) +
                        Math.pow(shoulderCenter.y - hipCenter.y, 2));

        // Center and normalize all keypoints
        float[][] centeredKeypoints = new float[keypoints.size()][2];
        float maxDistance = 0;

        // First, center all coordinates around hip center
        for (int i = 0; i < keypoints.size(); i++) {
            KeyPoint keypoint = keypoints.get(i);
            if (keypoint != null) {
                centeredKeypoints[i][0] = keypoint.getCoordinate().x - hipCenter.x;
                centeredKeypoints[i][1] = keypoint.getCoordinate().y - hipCenter.y;

                // Calculate max distance for normalization scale
                float distance = (float) Math.sqrt(
                        Math.pow(centeredKeypoints[i][0], 2) +
                                Math.pow(centeredKeypoints[i][1], 2));

                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }

        // Use the larger of torso size and max distance for normalization,
        // similar to the Python implementation
        float poseSize = Math.max(torsoSize * 2.5f, maxDistance);

        if (poseSize <= 0) {
            poseSize = 1.0f; // Avoid division by zero
        }

        // Normalize and flatten landmarks for model input
        for (int i = 0; i < keypoints.size(); i++) {
            KeyPoint keypoint = keypoints.get(i);
            if (keypoint != null && keypoint.getScore() >= MIN_KEYPOINT_SCORE) {
                // Following Python's normalization pattern:
                // landmarks array has format [x1, y1, x2, y2, ...] for all keypoints
                landmarks[i * 2] = centeredKeypoints[i][0] / poseSize;     // normalized x
                landmarks[i * 2 + 1] = centeredKeypoints[i][1] / poseSize; // normalized y
            }
            // If keypoint is missing or low confidence, we keep the zeros initialized earlier
        }

        // Log the landmarks for debugging
        StringBuilder sb = new StringBuilder("Normalized landmarks: ");
        for (int i = 0; i < Math.min(10, landmarks.length); i += 2) {
            sb.append(String.format("[%.2f,%.2f] ", landmarks[i], landmarks[i + 1]));
        }
        sb.append("...");
        Log.d(TAG, sb.toString());

        return landmarks;
    }

    /**
     * Classify yoga pose based on normalized landmarks.
     */
    private List<Recognition> classifyYogaPose(float[] normalizedLandmarks) {
        // Validate input
        if (normalizedLandmarks == null) {
            Log.e(TAG, "Cannot classify pose: normalized landmarks are null");
            return new ArrayList<>();
        }

        // Prepare input tensor
        int[] inputShape = yogaInterpreter.getInputTensor(0).shape();
        DataType inputType = yogaInterpreter.getInputTensor(0).dataType();

        // Log input shape for debugging
        Log.d(TAG, "Yoga model input shape: " + Arrays.toString(inputShape));
        Log.d(TAG, "Normalized landmarks length: " + normalizedLandmarks.length);

        // Ensure we have exactly 34 float values (136 bytes)
        // This corresponds to 17 keypoints with x,y coordinates for each
        if (normalizedLandmarks.length != 34) {
            Log.e(TAG, "Normalized landmarks array does not contain the expected 34 values! Current size: " + normalizedLandmarks.length);
            // Ensure we have exactly 34 values for our model
            float[] fixedLandmarks = new float[34];

            // Copy existing values or pad with zeros
            for (int i = 0; i < 34; i++) {
                if (i < normalizedLandmarks.length) {
                    fixedLandmarks[i] = normalizedLandmarks[i];
                } else {
                    fixedLandmarks[i] = 0.0f;
                }
            }
            normalizedLandmarks = fixedLandmarks;
        }

        // Validate the normalized landmarks to check for any NaN or Infinity values
        for (int i = 0; i < normalizedLandmarks.length; i++) {
            if (Float.isNaN(normalizedLandmarks[i]) || Float.isInfinite(normalizedLandmarks[i])) {
                Log.e(TAG, "Invalid value detected in normalized landmarks at index " + i + ": " + normalizedLandmarks[i]);
                normalizedLandmarks[i] = 0.0f; // Replace invalid values with 0
            }
        }

        // Create a 1-dimensional tensor for the model input (34 floats)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(34 * 4); // 34 floats * 4 bytes per float = 136 bytes
        inputBuffer.order(ByteOrder.nativeOrder());

        // Fill the input buffer with all 34 values
        for (float value : normalizedLandmarks) {
            inputBuffer.putFloat(value);
        }

        inputBuffer.rewind();

        try {
            // Check if model input is quantized
            boolean isQuantizedInput = inputType == DataType.UINT8;

            // For quantized models, convert float input to uint8
            if (isQuantizedInput) {
                Log.d(TAG, "Yoga model uses quantized input");
                // Get quantization parameters
                float scale = 0;
                int zeroPoint = 0;

                if (yogaInterpreter.getInputTensor(0).quantizationParams() != null) {
                    scale = yogaInterpreter.getInputTensor(0).quantizationParams().getScale();
                    zeroPoint = yogaInterpreter.getInputTensor(0).quantizationParams().getZeroPoint();
                    Log.d(TAG, "Quantization scale: " + scale + ", zero point: " + zeroPoint);
                } else {
                    // Default quantization for [-1,1] range to [0,255]
                    scale = 1 / 128.0f;
                    zeroPoint = 128;
                    Log.d(TAG, "Using default quantization parameters");
                }

                // Apply quantization to the input values
                if (scale != 0) {
                    byte[] quantizedValues = new byte[normalizedLandmarks.length];
                    for (int i = 0; i < normalizedLandmarks.length; i++) {
                        // Clamp values to prevent overflow
                        float clampedValue = Math.max(-1.0f, Math.min(1.0f, normalizedLandmarks[i]));
                        quantizedValues[i] = (byte) Math.round(clampedValue / scale + zeroPoint);
                    }

                    // Create a new uint8 buffer
                    ByteBuffer quantizedBuffer = ByteBuffer.allocateDirect(quantizedValues.length);
                    quantizedBuffer.order(ByteOrder.nativeOrder());
                    quantizedBuffer.put(quantizedValues);
                    quantizedBuffer.rewind();

                    // Run inference with quantized input
                    yogaInterpreter.run(quantizedBuffer, yogaOutputBuffer.getBuffer().rewind());
                } else {
                    // Fallback to float if quantization fails
                    yogaInterpreter.run(inputBuffer, yogaOutputBuffer.getBuffer().rewind());
                }
            } else {
                // For float models, use the float input buffer directly
                yogaInterpreter.run(inputBuffer, yogaOutputBuffer.getBuffer().rewind());
            }

            // Get results
            float[] outputs = yogaOutputBuffer.getFloatArray();

            // Validate outputs for debugging
            boolean hasValidOutput = false;
            for (float output : outputs) {
                if (!Float.isNaN(output) && !Float.isInfinite(output)) {
                    hasValidOutput = true;
                    break;
                }
            }

            if (!hasValidOutput) {
                Log.e(TAG, "Yoga model produced invalid outputs: " + Arrays.toString(outputs));
                return new ArrayList<>();
            }

            Log.d(TAG, "Yoga model outputs: " + Arrays.toString(outputs));

            // Create recognition list
            Map<String, Float> resultMap = new HashMap<>();
            for (int i = 0; i < yogaPoseClasses.size() && i < outputs.length; i++) {
                resultMap.put(yogaPoseClasses.get(i), outputs[i]);
            }

            // Sort and get top results
            return getTopKProbability(resultMap, maxResults);
        } catch (Exception e) {
            Log.e(TAG, "Error running yoga classification", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the top {@code maxResults} results.
     */
    private static List<Recognition> getTopKProbability(
            Map<String, Float> labelProb, int maxResults) {
        // Create a list of recognitions
        List<Recognition> recognitions = new ArrayList<>();

        // Convert to list and sort by probability (high to low)
        List<Map.Entry<String, Float>> entries = new ArrayList<>(labelProb.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take top results
        int resultSize = Math.min(entries.size(), maxResults);
        for (int i = 0; i < resultSize; i++) {
            Map.Entry<String, Float> entry = entries.get(i);
            recognitions.add(new Recognition(entry.getKey(), entry.getValue()));
        }

        return recognitions;
    }

    /**
     * Releases TFLite resources.
     */
    @Override
    public void close() {
        if (poseInterpreter != null) {
            poseInterpreter.close();
        }
        if (yogaInterpreter != null) {
            yogaInterpreter.close();
        }
    }
}
