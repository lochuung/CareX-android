package hcmute.edu.vn.loclinhvabao.carex.constant;

public class PoseConstants {
    public static final int INPUT_SIZE = 256; // Input size for MoveNet model
    public static final int NUM_KEYPOINTS = 17; // Number of keypoints in the pose model
    public static final int NUM_COORDINATES = 2; // Number of coordinates (x, y) for each keypoint
    public static final int NUM_POSE_CLASSES = 5; // Number of pose classes (e.g., yoga poses)
    public static final float CONFIDENCE_THRESHOLD = 0.5f; // Confidence threshold for pose detection
}
