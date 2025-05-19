package hcmute.edu.vn.loclinhvabao.carex.ml;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.BodyPart;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.KeyPoint;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;

/**
 * Class responsible for normalizing landmarks for pose classification.
 */
public class LandmarkNormalizer {
    private static final String TAG = "LandmarkNormalizer";
    
    // Threshold for confidence
    private static final float MIN_KEYPOINT_SCORE = 0.25f;
    
    // Expected landmarks count (17 keypoints with x,y for each)
    private static final int EXPECTED_KEYPOINTS = 17;
    private static final int EXPECTED_LANDMARKS = EXPECTED_KEYPOINTS * 2; // 34 values total
    
    /**
     * Normalize landmarks similar to Python code for model input.
     * 
     * @param person The detected person with keypoints
     * @return An array of normalized landmarks
     */
    @SuppressLint("DefaultLocale")
    public float[] normalizeLandmarks(Person person) {
        if (person == null) {
            Log.e(TAG, "Cannot normalize landmarks: person is null");
            return new float[EXPECTED_LANDMARKS]; // Return zeros
        }
        
        List<KeyPoint> keypoints = person.getKeyPoints();

        // MoveNet outputs 17 keypoints, each with x,y coordinates
        // We need exactly 34 float values (17 keypoints × 2 coordinates)
        float[] landmarks = new float[EXPECTED_LANDMARKS]; // 17 keypoints * 2 values (x,y) per keypoint

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
        PointF hipCenter = calculateHipCenter(person);

        // Calculate shoulder center for torso size estimation
        PointF shoulderCenter = calculateShoulderCenter(person);

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
     * Calculate the hip center point for normalization.
     * 
     * @param person The detected person
     * @return The hip center point
     */
    private PointF calculateHipCenter(Person person) {
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
            for (KeyPoint keypoint : person.getKeyPoints()) {
                if (keypoint != null && keypoint.getScore() >= MIN_KEYPOINT_SCORE) {
                    sumX += keypoint.getCoordinate().x;
                    sumY += keypoint.getCoordinate().y;
                    count++;
                }
            }
            hipCenter.x = count > 0 ? sumX / count : 0.5f;
            hipCenter.y = count > 0 ? sumY / count : 0.5f;
        }
        
        return hipCenter;
    }
    
    /**
     * Calculate the shoulder center point for normalization.
     * 
     * @param person The detected person
     * @return The shoulder center point
     */
    private PointF calculateShoulderCenter(Person person) {
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
            // Default to center of image if no shoulders with sufficient confidence
            shoulderCenter.x = 0.5f;
            shoulderCenter.y = 0.5f;
        }
        
        return shoulderCenter;
    }
    
    /**
     * Validates and fixes normalized landmarks to ensure they have the expected length and no invalid values.
     * 
     * @param normalizedLandmarks The landmarks to validate
     * @return The validated and fixed landmarks
     */
    public float[] validateLandmarks(float[] normalizedLandmarks) {
        // Validate input
        if (normalizedLandmarks == null) {
            Log.e(TAG, "Cannot validate landmarks: normalized landmarks are null");
            return new float[EXPECTED_LANDMARKS]; // Return zeros
        }
        
        // Ensure we have exactly 34 float values (136 bytes)
        // This corresponds to 17 keypoints with x,y coordinates for each
        if (normalizedLandmarks.length != EXPECTED_LANDMARKS) {
            Log.e(TAG, "Normalized landmarks array does not contain the expected 34 values! Current size: " + normalizedLandmarks.length);
            // Ensure we have exactly 34 values for our model
            float[] fixedLandmarks = new float[EXPECTED_LANDMARKS];

            // Copy existing values or pad with zeros
            for (int i = 0; i < EXPECTED_LANDMARKS; i++) {
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
        
        return normalizedLandmarks;
    }
}