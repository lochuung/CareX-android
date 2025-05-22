package hcmute.edu.vn.loclinhvabao.carex.ui.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.DailyProgress;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.Progress;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.DailyProgressRepository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.ProgressRepository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaDayRepository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaPoseRepository;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    
    private final YogaDayRepository yogaDayRepository;
    private final YogaPoseRepository yogaPoseRepository;
    private final ProgressRepository progressRepository;
    private final DailyProgressRepository dailyProgressRepository;
    
    // Current user ID - in a real app this would come from authentication
    private final String currentUserId = "current_user";
    
    // LiveData for yoga days (10-day program)
    private final MediatorLiveData<List<YogaDay>> yogaDays = new MediatorLiveData<>();
    
    // LiveData for selected yoga day
    private final MutableLiveData<YogaDay> selectedYogaDay = new MutableLiveData<>();
    
    // LiveData for selected yoga pose
    private final MutableLiveData<YogaPose> selectedYogaPose = new MutableLiveData<>();
    
    // LiveData for user progress (completed days)
    private final MutableLiveData<Map<Integer, Boolean>> userProgress = new MutableLiveData<>();
    
    // LiveData for advanced progress data
    private final MediatorLiveData<List<Progress>> detailedProgress = new MediatorLiveData<>();
    
    // LiveData for daily progress
    private final MediatorLiveData<List<DailyProgress>> dailyProgress = new MediatorLiveData<>();
    
    @Inject
    public SharedViewModel(YogaDayRepository yogaDayRepository, 
                          YogaPoseRepository yogaPoseRepository,
                          ProgressRepository progressRepository,
                          DailyProgressRepository dailyProgressRepository) {
        this.yogaDayRepository = yogaDayRepository;
        this.yogaPoseRepository = yogaPoseRepository;
        this.progressRepository = progressRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        
        loadDaysFromDatabase();
        initUserProgress();
        loadDetailedProgress();
        loadDailyProgress();
    }
    
    private void loadDaysFromDatabase() {
        // Add the yoga days data source to the mediator
        yogaDays.addSource(yogaDayRepository.getAllDays(), yogaDays::setValue);
    }
    
    private void initUserProgress() {
        // Also update the map when detailed progress changes
        detailedProgress.observeForever(progressList -> {
            if (progressList != null) {
                Map<Integer, Boolean> updatedProgress = new HashMap<>();
                for (int i = 1; i <= 10; i++) {
                    updatedProgress.put(i, false);
                }
                
                for (Progress p : progressList) {
                    if (p.isCompleted()) {
                        updatedProgress.put(p.getDayNumber(), true);
                    }
                }
                
                userProgress.setValue(updatedProgress);
            }
        });
    }
    
    private void loadDetailedProgress() {
        // Add the detailed progress data source to the mediator
        detailedProgress.addSource(
            progressRepository.getAllProgressForUser(currentUserId),
            detailedProgress::setValue
        );
    }
    
    private void loadDailyProgress() {
        // Add the daily progress data source to the mediator
        dailyProgress.addSource(
            dailyProgressRepository.getAllDailyProgressForUser(currentUserId),
            dailyProgress::setValue
        );
    }
    
    // Getters for LiveData
    public LiveData<List<YogaDay>> getYogaDays() {
        return yogaDays;
    }
    
    public LiveData<YogaDay> getSelectedYogaDay() {
        return selectedYogaDay;
    }
    
    public LiveData<YogaPose> getSelectedYogaPose() {
        return selectedYogaPose;
    }
    
    public LiveData<Map<Integer, Boolean>> getUserProgress() {
        return userProgress;
    }
    
    // New getters for detailed progress
    public LiveData<List<Progress>> getDetailedProgress() {
        return detailedProgress;
    }
    
    public LiveData<List<DailyProgress>> getDailyProgress() {
        return dailyProgress;
    }
    
    public LiveData<Progress> getProgressForDay(int dayNumber) {
        return progressRepository.getProgressForDay(currentUserId, dayNumber);
    }
    
    public LiveData<List<Progress>> getCompletedProgress() {
        return progressRepository.getCompletedProgressForUser(currentUserId);
    }
    
    // Statistics methods
    public int getCompletedDaysCount() {
        return progressRepository.getCompletedDaysCount(currentUserId);
    }
    
    public float getAverageConfidence() {
        return progressRepository.getAverageConfidence(currentUserId);
    }
    
    public int getTotalCaloriesBurned() {
        return dailyProgressRepository.getTotalCaloriesForUser(currentUserId);
    }
    
    public int getTotalDuration() {
        return dailyProgressRepository.getTotalDurationForUser(currentUserId);
    }
    
    // Setters
    public void selectYogaDay(YogaDay day) {
        selectedYogaDay.setValue(day);
    }
    
    public void selectYogaPose(YogaPose pose) {
        selectedYogaPose.setValue(pose);
    }
      // Updated method to mark day complete with additional data
    public void markDayComplete(int dayNumber, int duration, int calories, float averageConfidence, List<YogaPose> completedPoses) {
        // Create a new Progress object
        Progress progress = Progress.builder()
                .userId(currentUserId)
                .dayNumber(dayNumber)
                .isCompleted(true)
                .completionDate(new Date())
                .duration(duration)
                .calories(calories)
                .averageConfidence(averageConfidence)
                .completedPoses(completedPoses)
                .build();
        
        // Save to repository
        progressRepository.insertProgress(progress);
        
        // Update daily progress
        updateDailyProgress(duration, calories, averageConfidence);
        
        // For backward compatibility, also update the simple map
        Map<Integer, Boolean> simpleProgress = userProgress.getValue();        if (simpleProgress != null) {
            simpleProgress.put(dayNumber, true);
            userProgress.setValue(simpleProgress);
        }
    }
    
    // Legacy method for backward compatibility
    public void markDayComplete(int dayNumber) {
        // Default values when no details are provided
        markDayComplete(dayNumber, 900, 100, 0.75f, null);
    }
    
    /**
     * Update daily progress with new activity
     * @param duration Duration in minutes
     * @param calories Calories burned
     * @param score Score for the session
     */
    private void updateDailyProgress(int duration, int calories, float score) {
        // Create a new daily progress entry
        DailyProgress newProgress = DailyProgress.builder()
                .date(new Date())
                .duration(duration)
                .calories(calories)
                .score(score)
                .build();
        
        // Save to repository
        dailyProgressRepository.insertDailyProgress(newProgress, currentUserId);
    }
}
