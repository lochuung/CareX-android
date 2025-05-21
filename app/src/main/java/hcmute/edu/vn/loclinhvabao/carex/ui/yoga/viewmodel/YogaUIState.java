package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel;

import lombok.Getter;

/**
 * State class that holds all UI-related data for yoga pose detection
 * This centralizes UI state management and makes observing state changes simpler
 */
@Getter
public class YogaUIState {
    private String poseName;
    private float confidence;
    private int progressSeconds;
    private boolean completed;

    public YogaUIState() {
        this.poseName = "";
        this.confidence = 0f;
        this.progressSeconds = 0;
        this.completed = false;
    }

    public YogaUIState(String poseName, float confidence, int progressSeconds, boolean completed) {
        this.poseName = poseName;
        this.confidence = confidence;
        this.progressSeconds = progressSeconds;
        this.completed = completed;
    }

    /**
     * Creates a copy of the current state with updated fields
     */
    public YogaUIState copy(String poseName, float confidence, int progressSeconds, boolean completed) {
        return new YogaUIState(
                poseName != null ? poseName : this.poseName,
                confidence >= 0 ? confidence : this.confidence,
                progressSeconds >= 0 ? progressSeconds : this.progressSeconds,
                completed
        );
    }

    /**
     * Creates a copy with updated pose info
     */
    public YogaUIState withPoseInfo(String poseName, float confidence) {
        return copy(poseName, confidence, this.progressSeconds, this.completed);
    }

    /**
     * Creates a copy with updated progress
     */
    public YogaUIState withProgress(int progressSeconds) {
        return copy(this.poseName, this.confidence, progressSeconds, this.completed);
    }

    /**
     * Creates a copy where the challenge is marked as completed
     */
    public YogaUIState withCompleted() {
        return copy(this.poseName, this.confidence, this.progressSeconds, true);
    }

    // Getters
    public String getPoseName() {
        return poseName;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getProgressSeconds() {
        return progressSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }
}
