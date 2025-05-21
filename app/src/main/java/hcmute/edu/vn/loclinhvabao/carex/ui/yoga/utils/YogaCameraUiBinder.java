package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view.PoseOverlayView;

/**
 * Class responsible for binding UI elements for the YogaCameraFragment
 */
public class YogaCameraUiBinder {
    // Camera related
    public final PreviewView viewFinder;
    public final ImageButton cameraSwitchButton;
    
    // General UI
    public final TextView textTimer;
    public final ImageButton buttonInstructions;
    public final PoseOverlayView poseOverlayView;
    public final CardView cardPrediction;
    public final CardView cardTimer;
    public final ProgressBar timerProgress;
    public final ImageButton btnBack;
    
    // Pose challenge UI
    public final CardView cardPoseChallenge;
    public final TextView textTargetPose;
    public final TextView textPoseProgress;
    public final ProgressBar poseProgressBar;
    public final View poseDurationContainer;
    public final TextView textDetectedPose;
    public final TextView textPoseConfidence;
    public final TextView textPoseStatus;
    public final ProgressBar poseProgress;
    public final ImageView imageCompletion;
    
    /**
     * Constructor initializes all UI elements from the root view
     * @param root The root view of the fragment
     */
    public YogaCameraUiBinder(View root) {
        // Camera related
        viewFinder = root.findViewById(R.id.view_finder);
        cameraSwitchButton = root.findViewById(R.id.camera_switch_button);
        
        // General UI
        textTimer = root.findViewById(R.id.text_timer);
        buttonInstructions = root.findViewById(R.id.button_instructions);
        poseOverlayView = root.findViewById(R.id.pose_overlay);
        cardPrediction = root.findViewById(R.id.card_prediction);
        cardTimer = root.findViewById(R.id.card_timer);
        timerProgress = root.findViewById(R.id.timer_progress);
        btnBack = root.findViewById(R.id.btn_back);
        
        // Pose challenge UI
        cardPoseChallenge = root.findViewById(R.id.card_pose_challenge);
        textTargetPose = root.findViewById(R.id.text_target_pose);
        textPoseProgress = root.findViewById(R.id.text_pose_progress);
        poseProgressBar = root.findViewById(R.id.pose_hold_progress);
        poseDurationContainer = root.findViewById(R.id.pose_duration_container);
        textDetectedPose = root.findViewById(R.id.text_detected_pose);
        textPoseConfidence = root.findViewById(R.id.text_pose_confidence);
        poseProgress = root.findViewById(R.id.pose_progress);
        textPoseStatus = root.findViewById(R.id.text_pose_status);
        imageCompletion = root.findViewById(R.id.image_completion);
    }
    
    /**
     * Applies animations to UI elements for a smooth entrance
     */
    public void animateEntrance() {
        UIAnimator.animateEntranceElements(
            viewFinder, 
            cardTimer, 
            cardPrediction,
            btnBack.getRootView().findViewById(R.id.button_panel)
        );
    }
    
    /**
     * Sets initial visibility states for UI elements
     */
    public void setInitialVisibility() {
        if (cardPoseChallenge != null) {
            cardPoseChallenge.setVisibility(View.VISIBLE);
        }
        
        if (imageCompletion != null) {
            imageCompletion.setVisibility(View.GONE);
        }
    }
}
