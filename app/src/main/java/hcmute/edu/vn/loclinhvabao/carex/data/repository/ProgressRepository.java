package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.ProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.ProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.Progress;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.ProgressMapper;

/**
 * Repository for managing user progress in yoga programs
 * Provides a clean API for ViewModel to interact with the data layer
 */
@Singleton
public class ProgressRepository {
    private final ProgressDao progressDao;
    private final ProgressMapper progressMapper;

    @Inject
    public ProgressRepository(ProgressDao progressDao, ProgressMapper progressMapper) {
        this.progressDao = progressDao;
        this.progressMapper = progressMapper;
    }    /**
     * Insert a new progress record
     * @param progress The Progress object to insert
     */
    public void insertProgress(Progress progress) {
        ProgressEntity entity = progressMapper.modelToEntity(progress);
        new Thread(() -> progressDao.insertProgress(entity)).start();
    }

    /**
     * Insert a new progress record with better error handling
     * This method catches foreign key constraint errors and handles them gracefully
     * @param progress The Progress object to insert
     */
    public void insertProgressSafely(Progress progress) {
        ProgressEntity entity = progressMapper.modelToEntity(progress);
        new Thread(() -> {
            try {
                progressDao.insertProgress(entity);
            } catch (android.database.sqlite.SQLiteConstraintException e) {
                // Log the foreign key constraint error but don't crash
                android.util.Log.e("ProgressRepository", "Foreign key constraint error when inserting progress", e);
                // Optionally, try to create the missing dependencies here
            } catch (Exception e) {
                // Log any other database errors
                android.util.Log.e("ProgressRepository", "Database error when inserting progress", e);
            }
        }).start();
    }

    /**
     * Update an existing progress record
     * @param progress The Progress object to update
     */
    public void updateProgress(Progress progress) {
        ProgressEntity entity = progressMapper.modelToEntity(progress);
        new Thread(() -> progressDao.updateProgress(entity)).start();
    }

    /**
     * Delete a progress record
     * @param progress The Progress object to delete
     */
    public void deleteProgress(Progress progress) {
        ProgressEntity entity = progressMapper.modelToEntity(progress);
        new Thread(() -> progressDao.deleteProgress(entity)).start();
    }

    /**
     * Get a progress record by its ID
     * @param progressId The ID of the progress record to retrieve
     * @return A LiveData object containing the requested Progress object
     */
    public LiveData<Progress> getProgressById(String progressId) {
        return Transformations.map(
                progressDao.getProgressById(progressId),
                progressMapper::entityToModel
        );
    }

    /**
     * Get all progress records for a user
     * @param userId The user's ID
     * @return A LiveData object containing a list of all Progress objects for the user
     */
    public LiveData<List<Progress>> getAllProgressForUser(String userId) {
        return Transformations.map(
                progressDao.getAllProgressForUser(userId),
                progressMapper::entityListToModelList
        );
    }

    /**
     * Get a progress record for a specific day
     * @param userId The user's ID
     * @param dayNumber The day number
     * @return A LiveData object containing the Progress object for the specified day
     */
    public LiveData<Progress> getProgressForDay(String userId, int dayNumber) {
        return Transformations.map(
                progressDao.getProgressForDay(userId, dayNumber),
                progressMapper::entityToModel
        );
    }

    /**
     * Get all completed progress records for a user
     * @param userId The user's ID
     * @return A LiveData object containing a list of completed Progress objects
     */
    public LiveData<List<Progress>> getCompletedProgressForUser(String userId) {
        return Transformations.map(
                progressDao.getCompletedProgressForUser(userId),
                progressMapper::entityListToModelList
        );
    }    /**
     * Get the count of completed days for a user
     * @param userId The user's ID
     * @return The count of completed days
     */
    public int getCompletedDaysCount(String userId) {
        int[] count = new int[1];
        Thread thread = new Thread(() -> {
            count[0] = progressDao.getCompletedDaysCount(userId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return count[0];
    }

    /**
     * Get the count of completed days in a time range
     * @param userId The user's ID
     * @param startTime The start time in milliseconds
     * @param endTime The end time in milliseconds
     * @return The count of completed days in the specified time range
     */
    public int getCompletedDaysInTimeRange(String userId, long startTime, long endTime) {
        int[] count = new int[1];
        Thread thread = new Thread(() -> {
            count[0] = progressDao.getCompletedDaysInTimeRange(userId, startTime, endTime);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return count[0];
    }

    /**
     * Get the total calories burned in a time range
     * @param userId The user's ID
     * @param startTime The start time in milliseconds
     * @param endTime The end time in milliseconds
     * @return The total calories burned in the specified time range
     */
    public int getTotalCaloriesInTimeRange(String userId, long startTime, long endTime) {
        int[] calories = new int[1];
        Thread thread = new Thread(() -> {
            calories[0] = progressDao.getTotalCaloriesInTimeRange(userId, startTime, endTime);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return calories[0];
    }

    /**
     * Get the average confidence score for a user
     * @param userId The user's ID
     * @return The average confidence score
     */
    public float getAverageConfidence(String userId) {
        float[] confidence = new float[1];
        Thread thread = new Thread(() -> {
            confidence[0] = progressDao.getAverageConfidence(userId);
        });
        thread.start();
        try {
            thread.join();        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return confidence[0];
    }

    /**
     * Delete all progress records for a user
     * @param userId The user's ID
     */
    public void deleteAllProgressForUser(String userId) {
        new Thread(() -> progressDao.deleteAllProgressForUser(userId)).start();
    }
}
