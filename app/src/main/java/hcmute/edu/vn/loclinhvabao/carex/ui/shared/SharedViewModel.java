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
import hcmute.edu.vn.loclinhvabao.carex.data.repository.UserProfileRepository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaDayRepository;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaPoseRepository;
import hcmute.edu.vn.loclinhvabao.carex.util.Constants;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

@HiltViewModel
public class SharedViewModel extends ViewModel {
      private final YogaDayRepository yogaDayRepository;
    private final YogaPoseRepository yogaPoseRepository;
    private final ProgressRepository progressRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final UserProfileRepository userProfileRepository;
    
    // Current user ID - in a real app this would come from authentication
    private final String currentUserId = Constants.CURRENT_USER_ID;
    
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
    
    // LiveData for completion events (to show congratulation only when actually completing)
    private final MutableLiveData<Progress> dayCompletionEvent = new MutableLiveData<>();
      @Inject
    public SharedViewModel(YogaDayRepository yogaDayRepository, 
                          YogaPoseRepository yogaPoseRepository,
                          ProgressRepository progressRepository,
                          DailyProgressRepository dailyProgressRepository,
                          UserProfileRepository userProfileRepository) {
        this.yogaDayRepository = yogaDayRepository;
        this.yogaPoseRepository = yogaPoseRepository;
        this.progressRepository = progressRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        this.userProfileRepository = userProfileRepository;
        
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
    
    // Completion event methods
    public LiveData<Progress> getDayCompletionEvent() {
        return dayCompletionEvent;
    }
    
    public void clearCompletionEvent() {
        dayCompletionEvent.setValue(null);
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
    }    // Updated method to mark day complete with additional data
    public void markDayComplete(int dayNumber, int duration, int calories, float averageConfidence, List<YogaPose> completedPoses) {
        // Always update the simple map first (this doesn't rely on database)
        Map<Integer, Boolean> simpleProgress = userProgress.getValue();
        if (simpleProgress == null) {
            simpleProgress = new HashMap<>();
            // Initialize all days as incomplete
            for (int i = 1; i <= 10; i++) {
                simpleProgress.put(i, false);
            }
        }
        simpleProgress.put(dayNumber, true);
        userProgress.setValue(simpleProgress);
        
        // Try to save to database with enhanced error handling
        try {
            // Ensure user profile exists before saving progress
            ensureUserProfileExists();
            
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
              // Save to repository with error handling
            progressRepository.insertProgressSafely(progress);
              // Update daily progress (keep duration in seconds for consistency)
            updateDailyProgress(duration, calories, averageConfidence);
            
            // Trigger completion event
            dayCompletionEvent.setValue(progress);
            
        } catch (Exception e) {
            // Log error but don't crash the app - UI progress is already updated
            android.util.Log.e("SharedViewModel", "Error saving progress to database", e);
        }
    }
    
    // Legacy method for backward compatibility
    public void markDayComplete(int dayNumber) {
        // Default values when no details are provided
        markDayComplete(dayNumber, 900, 100, 0.75f, null);
    }
    
    /**
     * Update overall yoga progress for the 10-day challenge
     * This method ensures the progress tracking is properly synchronized
     * @param dayNumber The day number that was completed (1-10)
     */
    public void updateYogaProgress(int dayNumber) {
        // Ensure the progress map is updated
        Map<Integer, Boolean> currentProgress = userProgress.getValue();
        if (currentProgress == null) {
            currentProgress = new HashMap<>();
            // Initialize all days as incomplete
            for (int i = 1; i <= 10; i++) {
                currentProgress.put(i, false);
            }
        }
        
        // Mark the current day as complete
        currentProgress.put(dayNumber, true);
        userProgress.setValue(currentProgress);
        
        // Trigger a refresh of detailed progress to ensure consistency
        loadDetailedProgress();
    }    /**
     * Update daily progress with new activity
     * @param duration Duration in seconds
     * @param calories Calories burned
     * @param score Score for the session
     */
    private void updateDailyProgress(int duration, int calories, float score) {
        try {
            // Ensure user profile exists before saving daily progress
            ensureUserProfileExists();
            
            // Create a new daily progress entry
            DailyProgress newProgress = DailyProgress.builder()
                    .date(new Date())
                    .duration(duration)
                    .calories(calories)
                    .score(score)
                    .build();
            
            // Save to repository with error handling
            dailyProgressRepository.insertDailyProgressSafely(newProgress, currentUserId);
        } catch (Exception e) {
            // Log error but don't crash the app
            android.util.Log.e("SharedViewModel", "Error saving daily progress to database", e);
        }
    }
      /**
     * Ensure the user profile exists in the database to satisfy foreign key constraints
     * This prevents foreign key constraint errors when inserting progress records
     */
    private void ensureUserProfileExists() {
        try {
            // Check if user profile exists, if not create a default one
            // This is done synchronously to ensure the profile exists before inserting progress
            new Thread(() -> {
                try {
                    // Use the UserProfileRepository to check and create profile if needed
                    userProfileRepository.syncUserProfile(currentUserId, Constants.DEFAULT_USER_NAME, null);
                } catch (Exception e) {
                    android.util.Log.e("SharedViewModel", "Error ensuring user profile exists", e);
                }
            }).start();
            
            // Give the thread a moment to complete
            Thread.sleep(100);
        } catch (Exception e) {
            android.util.Log.e("SharedViewModel", "Error in ensureUserProfileExists", e);
        }
    }
    
    /**
     * Check if user profile exists in database
     */
    private boolean userProfileExists(String userId) {
        // This method is no longer needed as we're using UserProfileRepository.syncUserProfile
        // which handles the existence check internally
        return true;
    }
    
    /**
     * Create a default user profile to satisfy foreign key constraints
     */    private void createDefaultUserProfile(String userId) {
        // Use UserProfileRepository to create the default profile
        try {
            userProfileRepository.createDefaultProfile(userId, Constants.DEFAULT_USER_NAME);
            android.util.Log.d("SharedViewModel", "Created default user profile for: " + userId);
        } catch (Exception e) {
            android.util.Log.e("SharedViewModel", "Error creating default user profile", e);
        }
    }

    /**
     * Reset all progress for the current user
     */
    public void resetAllProgress() {
        // Delete all progress data
        progressRepository.deleteAllProgressForUser(currentUserId);
        dailyProgressRepository.deleteAllDailyProgressForUser(currentUserId);
        
        // Reset the userProgress map to show all days as incomplete
        Map<Integer, Boolean> resetProgress = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            resetProgress.put(i, false);
        }
        userProgress.setValue(resetProgress);
        
        // Clear any completion events
        clearCompletionEvent();
        
        android.util.Log.d("SharedViewModel", "Reset all progress for user: " + currentUserId);
    }
}
