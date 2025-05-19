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
    
    private final TextView textPrediction;
    private final PoseOverlayView poseOverlayView;
    private final YogaPoseClassifier classifier;
    
    /**
     * Creates a new instance of RecognitionPresenter.
     *
     * @param textPrediction TextView for displaying prediction results
     * @param poseOverlayView View for displaying pose overlay
     * @param classifier YogaPoseClassifier to get detected person data
     */
    public RecognitionPresenter(
            TextView textPrediction,
            PoseOverlayView poseOverlayView,
            YogaPoseClassifier classifier) {
        this.textPrediction = textPrediction;
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
            // Early exit: if recognition is empty
            if (recognitions.isEmpty()) {
                textPrediction.setText("No pose detected");
                textPrediction.setVisibility(View.VISIBLE);
                return;
            }

            // Update the text and UI
            StringBuilder text = new StringBuilder();
            for (YogaPoseClassifier.Recognition recognition : recognitions) {
                text.append(
                        String.format(
                                Locale.getDefault(),
                                "%.2f %s\n",
                                recognition.confidence(),
                                recognition.title()));
            }
            textPrediction.setText(text);

            // Make sure all UI elements are visible
            textPrediction.setVisibility(View.VISIBLE);

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
