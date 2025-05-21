package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.observer;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;

/**
 * Observer interface for pose recognition events
 * Used to decouple pose recognition from UI handling
 */
public interface PoseRecognitionObserver {
    /**
     * Called when a pose is recognized
     * 
     * @param recognitions List of recognized poses with their confidence scores
     */
    void onPoseRecognized(List<YogaPoseClassifier.Recognition> recognitions);
}
