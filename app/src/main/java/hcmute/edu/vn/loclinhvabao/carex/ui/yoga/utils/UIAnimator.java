package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;

import hcmute.edu.vn.loclinhvabao.carex.R;

/**
 * Handles UI animations throughout the application.
 * Follows the Single Responsibility Principle by encapsulating animation logic.
 */
public class UIAnimator {
    private static final String TAG = "UIAnimator";
    
    /**
     * Animates UI elements for a smooth entrance effect.
     */
    public static void animateEntranceElements(PreviewView viewFinder, CardView cardTimer, 
                                              CardView cardPrediction, View buttonPanel) {
        // Fade in the camera view
        viewFinder.setAlpha(0f);
        viewFinder.animate().alpha(1f).setDuration(800).start();
        
        // Slide in the timer from top
        cardTimer.setTranslationY(-200f);
        cardTimer.setAlpha(0f);
        cardTimer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .start();
        
        // Slide in the prediction card from bottom
        cardPrediction.setTranslationY(200f);
        cardPrediction.setAlpha(0f);
        cardPrediction.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(500)
                .start();
        
        // Fade and slide in the button panel
        if (buttonPanel != null) {
            buttonPanel.setAlpha(0f);
            buttonPanel.setTranslationY(100f);
            buttonPanel.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(600)
                    .setStartDelay(700)
                    .start();
        }
    }
    
    /**
     * Animates a card with a scaling effect.
     */
    public static void animateCardScale(CardView card) {
        if (card == null) return;
        
        card.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(300)
                .withEndAction(() -> 
                    card.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(300)
                            .start())
                .start();
    }
    
    /**
     * Update progress bar based on confidence value.
     * @param poseProgress The progress bar to update
     * @param confidence The confidence value (0.0-1.0)
     * @param cardToAnimate Card to apply pulse animation if confidence is high
     */
    public static void updateProgressWithConfidence(ProgressBar poseProgress, float confidence, 
                                                  CardView cardToAnimate) {
        if (poseProgress == null) return;
        
        try {
            // Calculate progress as function of confidence (0.7-1.0 maps to 0-100%)
            float normalizedConfidence = (confidence - 0.7f) / 0.3f;
            int progress = (int) (Math.max(0, Math.min(1, normalizedConfidence)) * 100);
            
            // Smooth animation of progress
            ObjectAnimator.ofInt(poseProgress, "progress", poseProgress.getProgress(), progress)
                    .setDuration(300)
                    .start();
            
            // Apply pulse animation to card when confidence is very high
            if (confidence > 0.9f && cardToAnimate != null) {
                Animation pulseAnimation = AnimationUtils.loadAnimation(
                        cardToAnimate.getContext(), R.anim.pulse_animation);
                cardToAnimate.startAnimation(pulseAnimation);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating progress bar: " + e.getMessage());
        }
    }
}
