package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.tracker;

import android.os.Handler;
import android.util.Log;

/**
 * Class responsible for tracking yoga pose detection, timing, and state management
 */
public class PoseTracker {
    private static final String TAG = "PoseTracker";
    
    // Callback interface for pose tracking events
    public interface Callback {
        void onPoseHoldProgress(int seconds);
        void onPoseCompleted();
        void onPosePaused();
        void onPoseStarted();
    }
    
    // Pose tracking constants
    private final String targetPose;
    private final int requiredDurationSeconds;
    private final float confidenceThreshold;
    
    // Pose tracking variables
    private long poseEnteredTimestamp = 0;
    private long currentPoseHoldTime = 0;
    private boolean isInTargetPose = false;
    private boolean hasCompletedPoseChallenge = false;
    private final Handler poseTrackingHandler = new Handler();
    private final Runnable poseTrackingRunnable;
    
    // Callback
    private final Callback callback;
    
    /**
     * Create a new PoseTracker instance
     * 
     * @param targetPose The target pose to track
     * @param requiredDurationSeconds How long the pose should be held for completion
     * @param confidenceThreshold Minimum confidence to consider as valid pose
     * @param callback Callback for pose tracking events
     */
    public PoseTracker(String targetPose, int requiredDurationSeconds, 
                     float confidenceThreshold, Callback callback) {
        this.targetPose = targetPose;
        this.requiredDurationSeconds = requiredDurationSeconds;
        this.confidenceThreshold = confidenceThreshold;
        this.callback = callback;
        
        // Set up pose tracking runnable
        this.poseTrackingRunnable = this::trackPoseHoldDuration;
    }
    
    /**
     * Update the pose tracker with new pose detection data
     * 
     * @param poseName The detected pose name
     * @param confidence The confidence score for the detected pose
     */
    public void update(String poseName, float confidence) {
        boolean isPoseDetected = poseName.equalsIgnoreCase(targetPose) && confidence >= confidenceThreshold;
        
        Log.d(TAG, String.format("Handling pose: %s (conf: %.2f) - Target: %s - In pose: %b - Completed: %b",
                poseName, confidence, targetPose, isInTargetPose, hasCompletedPoseChallenge));
        
        // If already completed, don't do anything
        if (hasCompletedPoseChallenge) {
            return;
        }
        
        // Handle entering the pose
        if (isPoseDetected && !isInTargetPose) {
            isInTargetPose = true;
            poseEnteredTimestamp = System.currentTimeMillis();
            Log.d(TAG, "ENTERED TARGET POSE: " + targetPose);
            
            // Notify callback
            callback.onPoseStarted();
            
            // Start tracking
            poseTrackingHandler.post(poseTrackingRunnable);
        }
        // Handle exiting the pose before completion
        else if (!isPoseDetected && isInTargetPose) {
            isInTargetPose = false;
            poseTrackingHandler.removeCallbacks(poseTrackingRunnable);
            Log.d(TAG, "EXITED TARGET POSE: " + targetPose);
            
            // Notify callback
            callback.onPosePaused();
            
            // We could add logic here to reset progress if held for less than a threshold
            // or keep current progress depending on requirements
        }
    }
    
    /**
     * Get the current target pose name
     * 
     * @return The target pose name
     */
    public String getTargetPose() {
        return targetPose;
    }
    
    /**
     * Check if pose challenge has been completed
     * 
     * @return true if pose has been completed, false otherwise
     */
    public boolean isCompleted() {
        return hasCompletedPoseChallenge;
    }
    
    /**
     * Stop all pose tracking
     */
    public void stop() {
        poseTrackingHandler.removeCallbacks(poseTrackingRunnable);
        isInTargetPose = false;
    }
    
    /**
     * Resume pose tracking if it was in progress
     */
    public void resume() {
        if (isInTargetPose && !hasCompletedPoseChallenge) {
            poseTrackingHandler.post(poseTrackingRunnable);
        }
    }
    
    /**
     * Runnable that tracks pose hold duration
     */
    private void trackPoseHoldDuration() {
        if (isInTargetPose && !hasCompletedPoseChallenge) {
            // Calculate elapsed time
            long elapsedMillis = System.currentTimeMillis() - poseEnteredTimestamp;
            int elapsedSeconds = (int) (elapsedMillis / 1000);
            
            // Update progress
            currentPoseHoldTime = elapsedMillis;
            
            // Notify callback of progress
            callback.onPoseHoldProgress(elapsedSeconds);
            
            // Log for debugging
            Log.d(TAG, "Pose held for " + elapsedSeconds + " seconds");
            
            // Check if target reached
            if (elapsedSeconds >= requiredDurationSeconds) {
                hasCompletedPoseChallenge = true;
                callback.onPoseCompleted();
            } else {
                // Continue tracking
                poseTrackingHandler.postDelayed(poseTrackingRunnable, 100);
            }
        }
    }
}
