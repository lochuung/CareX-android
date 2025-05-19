package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.session;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils.UIAnimator;

/**
 * Manages the yoga session state, timer, and related UI.
 * Follows the Single Responsibility Principle by encapsulating session management.
 */
public class YogaSessionManager {
    private final Context context;
    private final TextView textTimer;
    private final ProgressBar timerProgress;
    private final MaterialButton toggleButton;
    private final CardView timerCard;
    
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable;
    
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean isSessionActive = false;
    
    public interface SessionStateListener {
        void onSessionStateChanged(boolean isActive);
    }
    
    private final SessionStateListener sessionStateListener;
    
    public YogaSessionManager(Context context, TextView textTimer, ProgressBar timerProgress,
                             MaterialButton toggleButton, CardView timerCard,
                             SessionStateListener listener) {
        this.context = context;
        this.textTimer = textTimer;
        this.timerProgress = timerProgress;
        this.toggleButton = toggleButton;
        this.timerCard = timerCard;
        this.sessionStateListener = listener;
        
        // Set up timer runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timerHandler.postDelayed(this, 1000);
            }
        };
        
        // Set up toggle button click listener
        toggleButton.setOnClickListener(v -> toggleSession());
    }
    
    /**
     * Toggle workout session timer on/off
     */
    public void toggleSession() {
        if (isSessionActive) {
            // Stop session
            stopTimer();
            toggleButton.setText("Start Session");
            isSessionActive = false;
        } else {
            // Start session
            startTimer();
            toggleButton.setText("Stop Session");
            isSessionActive = true;
        }
        
        // Notify listener
        if (sessionStateListener != null) {
            sessionStateListener.onSessionStateChanged(isSessionActive);
        }
    }
    
    /**
     * Start the workout timer
     */
    public void startTimer() {
        if (isSessionActive) return; // Already running
        
        startTime = System.currentTimeMillis() - elapsedTime;
        timerHandler.postDelayed(timerRunnable, 0);
        
        // Apply animation to the timer card
        UIAnimator.animateCardScale(timerCard);
    }
    
    /**
     * Stop the workout timer
     */
    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        elapsedTime = System.currentTimeMillis() - startTime;
        
        // Provide vibration feedback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }
    
    /**
     * Update the timer display
     */
    private void updateTimer() {
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        
        textTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        
        // Update progress bar (assuming max workout time of 30 minutes)
        if (timerProgress != null) {
            int progress = (int) ((minutes * 60 + seconds) / (30.0f * 60) * 100);
            timerProgress.setProgress(Math.min(progress, 100));
        }
    }
    
    /**
     * Check if session is currently active
     */
    public boolean isSessionActive() {
        return isSessionActive;
    }
    
    /**
     * Clean up resources
     */
    public void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    /**
     * Handle activity pause
     */
    public void onPause() {
        if (isSessionActive) {
            stopTimer();
        }
    }
    
    /**
     * Handle activity resume
     */
    public void onResume() {
        if (isSessionActive) {
            startTimer();
        }
    }
}
