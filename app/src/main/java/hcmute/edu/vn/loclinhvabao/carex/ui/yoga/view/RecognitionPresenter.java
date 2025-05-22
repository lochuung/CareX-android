package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.observer.PoseRecognitionObserver;

/**
 * Presenter class for displaying yoga pose recognition results on UI.
 * Implements PoseRecognitionObserver to receive pose recognition events.
 */
public class RecognitionPresenter implements PoseRecognitionObserver {
    private static final String TAG = "RecognitionPresenter";
    private final PoseOverlayView poseOverlayView;
    private final YogaPoseClassifier classifier;
    private boolean isFrontCamera = false;
    private Consumer<List<YogaPoseClassifier.Recognition>> postRecognitionCallback;
    
    /**
     * Creates a new instance of RecognitionPresenter.
     *
     * @param poseOverlayView View for displaying pose overlay
     * @param classifier YogaPoseClassifier to get detected person data
     */
    public RecognitionPresenter(
            PoseOverlayView poseOverlayView,
            YogaPoseClassifier classifier) {
        this.poseOverlayView = poseOverlayView;
        this.classifier = classifier;
    }
    
    /**
     * Sets whether the front camera is being used for mirroring pose overlay
     * 
     * @param isFrontCamera True if front camera is being used
     */
    public void setFrontCamera(boolean isFrontCamera) {
        this.isFrontCamera = isFrontCamera;
    }
    
    /**
     * Sets the callback to be executed after processing recognitions
     * 
     * @param callback The callback to execute
     */
    public void setPostRecognitionCallback(
            Consumer<List<YogaPoseClassifier.Recognition>> callback) {
        this.postRecognitionCallback = callback;
    }
    
    /**
     * Implements the PoseRecognitionObserver interface method.
     * Called when a pose is recognized by the YogaAnalyzer.
     *
     * @param recognitions List of recognition results
     */
    @Override
    public void onPoseRecognized(List<YogaPoseClassifier.Recognition> recognitions) {
        try {
            // Update pose overlay
            Person person = classifier.getLastDetectedPerson();
            if (person != null) {
                poseOverlayView.setKeyPoints(person.getKeyPoints());
            }
            
            // Execute post-recognition callback if set
            if (postRecognitionCallback != null && recognitions != null) {
                postRecognitionCallback.accept(recognitions);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reporting recognition", e);
        }
    }
    
    /**
     * @deprecated Use onPoseRecognized instead
     * Kept for backward compatibility
     */
    @Deprecated
    public void displayResults(List<YogaPoseClassifier.Recognition> recognitions, boolean isFrontCamera) {
        this.isFrontCamera = isFrontCamera;
        onPoseRecognized(recognitions);
    }
}
