package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class YogaCameraViewModel extends ViewModel {

    // Create a single MutableLiveData for the UI state
    private final MutableLiveData<YogaUIState> _uiState = new MutableLiveData<>(new YogaUIState());
    
    // Expose only the immutable LiveData to observers
    public final LiveData<YogaUIState> uiState = _uiState;
    
    // Keep these for backward compatibility until all code is updated
    @Deprecated
    public final LiveData<String> currentPose;
    @Deprecated
    public final LiveData<Float> confidence;
    @Deprecated
    public final LiveData<Integer> poseProgress;
    @Deprecated
    public final LiveData<Boolean> completed;

    @Inject
    public YogaCameraViewModel() {
        // Initialize the legacy LiveData objects that map to the uiState fields
        // These will be removed once all code is updated to use uiState
        MutableLiveData<String> _currentPose = new MutableLiveData<>("");
        MutableLiveData<Float> _confidence = new MutableLiveData<>(0f);
        MutableLiveData<Integer> _poseProgress = new MutableLiveData<>(0);
        MutableLiveData<Boolean> _completed = new MutableLiveData<>(false);
        
        currentPose = _currentPose;
        confidence = _confidence;
        poseProgress = _poseProgress;
        completed = _completed;
        
        // Observe uiState and update the legacy LiveData objects
        uiState.observeForever(state -> {
            _currentPose.setValue(state.getPoseName());
            _confidence.setValue(state.getConfidence());
            _poseProgress.setValue(state.getProgressSeconds());
            _completed.setValue(state.isCompleted());
        });
    }

    /**
     * Updates the pose name and confidence in the UI state
     */
    public void updatePose(String pose, float confidenceScore) {
        YogaUIState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withPoseInfo(pose, confidenceScore));
        }
    }

    /**
     * Updates the pose progress in the UI state
     */
    public void updateProgress(int seconds) {
        YogaUIState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withProgress(seconds));
        }
    }

    /**
     * Marks the yoga challenge as completed in the UI state
     */
    public void markCompleted() {
        YogaUIState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withCompleted());
        }
    }

    /**
     * Resets the UI state to initial values
     */
    public void resetState() {
        _uiState.setValue(new YogaUIState());
    }
}
