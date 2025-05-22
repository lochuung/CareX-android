package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import hcmute.edu.vn.loclinhvabao.carex.util.Constants;

/**
 * ViewModel for the Yoga feature
 * Handles communication between UI and repositories
 */
@HiltViewModel
public class YogaViewModel extends ViewModel {
    private final YogaPoseRepository yogaPoseRepository;
    private final YogaDayRepository yogaDayRepository;
    private final ProgressRepository progressRepository;    private final DailyProgressRepository dailyProgressRepository;
    
    // Current user ID - in a real app this would come from authentication
    private final String currentUserId = Constants.CURRENT_USER_ID;
    
    // LiveData for the UI to observe
    private final MediatorLiveData<List<YogaDay>> availableDays = new MediatorLiveData<>();
    private final MutableLiveData<YogaDay> selectedDay = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MediatorLiveData<List<Progress>> userProgress = new MediatorLiveData<>();
    private final MediatorLiveData<List<DailyProgress>> weeklyProgress = new MediatorLiveData<>();

    @Inject
    public YogaViewModel(
            YogaPoseRepository yogaPoseRepository,
            YogaDayRepository yogaDayRepository,
            ProgressRepository progressRepository,
            DailyProgressRepository dailyProgressRepository) {
        this.yogaPoseRepository = yogaPoseRepository;
        this.yogaDayRepository = yogaDayRepository;
        this.progressRepository = progressRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        
        // Initialize data
        loadYogaProgram();
        loadUserProgress();
        loadWeeklyProgress();
    }
    
    /**
     * Load all yoga days from the repository
     */
    private void loadYogaProgram() {
        isLoading.setValue(true);
        
        // Add the yoga days data source to the mediator
        availableDays.addSource(yogaDayRepository.getAllDays(), days -> {
            availableDays.setValue(days);
            isLoading.setValue(false);
        });
    }
    
    /**
     * Load user progress data
     */
    private void loadUserProgress() {
        userProgress.addSource(
                progressRepository.getAllProgressForUser(currentUserId),
                progressList -> userProgress.setValue(progressList)
        );
    }
    
    /**
     * Load weekly progress data
     */
    private void loadWeeklyProgress() {
        // Calculate start and end dates for the current week
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfWeek = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DATE, 7);
        long endOfWeek = calendar.getTimeInMillis();
        
        // Add the weekly progress data source to the mediator
        weeklyProgress.addSource(
                dailyProgressRepository.getDailyProgressBetweenDates(currentUserId, startOfWeek, endOfWeek),
                dailyProgressList -> weeklyProgress.setValue(dailyProgressList)
        );
    }
    
    /**
     * Get all available yoga days
     * @return LiveData containing the list of yoga days
     */
    public LiveData<List<YogaDay>> getAvailableDays() {
        return availableDays;
    }
    
    /**
     * Set the currently selected day
     * @param day The selected YogaDay
     */
    public void selectDay(YogaDay day) {
        selectedDay.setValue(day);
    }
    
    /**
     * Get the currently selected day
     * @return LiveData containing the selected day
     */
    public LiveData<YogaDay> getSelectedDay() {
        return selectedDay;
    }
    
    /**
     * Get loading state
     * @return LiveData containing the loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Get user progress data
     * @return LiveData containing user progress
     */
    public LiveData<List<Progress>> getUserProgress() {
        return userProgress;
    }
    
    /**
     * Get weekly progress data
     * @return LiveData containing weekly progress
     */
    public LiveData<List<DailyProgress>> getWeeklyProgress() {
        return weeklyProgress;
    }
    
    /**
     * Mark a day as completed
     * @param day The completed YogaDay
     * @param duration Duration in minutes
     * @param calories Calories burned
     * @param averageConfidence Average confidence score
     * @param completedPoses List of completed poses
     */
    public void completeDay(YogaDay day, int duration, int calories, float averageConfidence, List<YogaPose> completedPoses) {
        // Create a new Progress object
        Progress progress = Progress.builder()
                .userId(currentUserId)
                .dayNumber(day.getDayNumber())
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
    }
    
    /**
     * Update daily progress with new activity
     * @param duration Duration in minutes
     * @param calories Calories burned
     * @param score Score for the session
     */
    private void updateDailyProgress(int duration, int calories, float score) {
        // Check if we already have a record for today
        DailyProgress existingProgress = dailyProgressRepository.getLatestDailyProgress(currentUserId);
        Date today = new Date();
        
        if (existingProgress != null && isSameDay(existingProgress.getDate(), today)) {
            // Update existing record
            DailyProgress updatedProgress = DailyProgress.builder()
                    .date(existingProgress.getDate())
                    .duration(existingProgress.getDuration() + duration)
                    .calories(existingProgress.getCalories() + calories)
                    .score((existingProgress.getScore() + score) / 2) // Average of old and new scores
                    .build();
            
            dailyProgressRepository.updateDailyProgress(updatedProgress, currentUserId);
        } else {
            // Create new record for today
            DailyProgress newProgress = DailyProgress.builder()
                    .date(today)
                    .duration(duration)
                    .calories(calories)
                    .score(score)
                    .build();
            
            dailyProgressRepository.insertDailyProgress(newProgress, currentUserId);
        }
    }
    
    /**
     * Check if two dates are on the same day
     * @param date1 First date
     * @param date2 Second date
     * @return True if both dates are on the same day
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * Get statistics for the user
     * @return Map containing various statistics
     */
    public java.util.Map<String, Object> getUserStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // Get completed days count
        int completedDays = progressRepository.getCompletedDaysCount(currentUserId);
        stats.put("completedDays", completedDays);
        
        // Get total duration (convert from seconds to minutes)
        int totalDurationSeconds = dailyProgressRepository.getTotalDurationForUser(currentUserId);
        int totalDurationMinutes = totalDurationSeconds / 60;
        stats.put("totalDurationMinutes", totalDurationMinutes);
        
        // Get total calories
        int totalCalories = dailyProgressRepository.getTotalCaloriesForUser(currentUserId);
        stats.put("totalCalories", totalCalories);
        
        // Get average score
        float averageScore = dailyProgressRepository.getAverageScoreForUser(currentUserId);
        stats.put("averageScore", averageScore);
        
        // Get unique practice days
        int uniquePracticeDays = dailyProgressRepository.getUniquePracticeDaysCount(currentUserId);
        stats.put("uniquePracticeDays", uniquePracticeDays);
        
        return stats;
    }
}
