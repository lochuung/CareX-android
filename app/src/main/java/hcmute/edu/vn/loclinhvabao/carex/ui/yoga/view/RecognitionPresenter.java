package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.Person;
import hcmute.edu.vn.loclinhvabao.carex.ml.YogaPoseClassifier;

/**
 * Presenter class for displaying yoga pose recognition results on UI.
 */
public class RecognitionPresenter {
    private static final String TAG = "RecognitionPresenter";
    private final PoseOverlayView poseOverlayView;
    private final YogaPoseClassifier classifier;
    
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
     * Displays recognition results on screen.
     *
     * @param recognitions List of recognition results
     * @param isFrontCamera Whether front camera is being used (for mirroring pose overlay)
     */
    public void displayResults(List<YogaPoseClassifier.Recognition> recognitions, boolean isFrontCamera) {
        try {
            // Update pose overlay
            Person person = classifier.getLastDetectedPerson();
            if (person != null) {
                poseOverlayView.setKeyPoints(person.getKeyPoints());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reporting recognition", e);
        }
    }
}
