package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.DailyProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.DailyProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.DailyProgress;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.DailyProgressMapper;

/**
 * Repository for managing daily yoga activity progress
 * Provides a clean API for ViewModel to interact with the data layer
 */
@Singleton
public class DailyProgressRepository {
    private final DailyProgressDao dailyProgressDao;

    @Inject
    public DailyProgressRepository(DailyProgressDao dailyProgressDao) {
        this.dailyProgressDao = dailyProgressDao;
    }    /**
     * Insert a new daily progress record
     * @param dailyProgress The DailyProgress object to insert
     * @param userId The user's ID
     */
    public void insertDailyProgress(DailyProgress dailyProgress, String userId) {
        DailyProgressEntity entity = DailyProgressMapper.modelToEntity(dailyProgress, userId);
        new Thread(() -> dailyProgressDao.insertDailyProgress(entity)).start();
    }

    /**
     * Insert a new daily progress record with better error handling
     * This method catches foreign key constraint errors and handles them gracefully
     * @param dailyProgress The DailyProgress object to insert
     * @param userId The user's ID
     */
    public void insertDailyProgressSafely(DailyProgress dailyProgress, String userId) {
        DailyProgressEntity entity = DailyProgressMapper.modelToEntity(dailyProgress, userId);
        new Thread(() -> {
            try {
                dailyProgressDao.insertDailyProgress(entity);
            } catch (android.database.sqlite.SQLiteConstraintException e) {
                // Log the foreign key constraint error but don't crash
                android.util.Log.e("DailyProgressRepository", "Foreign key constraint error when inserting daily progress", e);
                // Optionally, try to create the missing user profile here
            } catch (Exception e) {
                // Log any other database errors
                android.util.Log.e("DailyProgressRepository", "Database error when inserting daily progress", e);
            }
        }).start();
    }

    /**
     * Update an existing daily progress record
     * @param dailyProgress The DailyProgress object to update
     * @param userId The user's ID
     */
    public void updateDailyProgress(DailyProgress dailyProgress, String userId) {
        DailyProgressEntity entity = DailyProgressMapper.modelToEntity(dailyProgress, userId);
        new Thread(() -> dailyProgressDao.updateDailyProgress(entity)).start();
    }

    /**
     * Delete a daily progress record
     * @param dailyProgress The DailyProgress object to delete
     * @param userId The user's ID
     */
    public void deleteDailyProgress(DailyProgress dailyProgress, String userId) {
        DailyProgressEntity entity = DailyProgressMapper.modelToEntity(dailyProgress, userId);
        new Thread(() -> dailyProgressDao.deleteDailyProgress(entity)).start();
    }

    /**
     * Get a daily progress record by its ID
     * @param id The ID of the daily progress to retrieve
     * @return A LiveData object containing the requested DailyProgress object
     */
    public LiveData<DailyProgress> getDailyProgressById(String id) {
        return Transformations.map(
                dailyProgressDao.getDailyProgressById(id),
                DailyProgressMapper::entityToModel
        );
    }

    /**
     * Get all daily progress records for a user
     * @param userId The user's ID
     * @return A LiveData object containing a list of all DailyProgress objects for the user
     */
    public LiveData<List<DailyProgress>> getAllDailyProgressForUser(String userId) {
        return Transformations.map(
                dailyProgressDao.getAllDailyProgressForUser(userId),
                DailyProgressMapper::entityListToModelList
        );
    }

    /**
     * Get daily progress records between two dates
     * @param userId The user's ID
     * @param startDate The start date in milliseconds
     * @param endDate The end date in milliseconds
     * @return A LiveData object containing a list of DailyProgress objects in the specified date range
     */
    public LiveData<List<DailyProgress>> getDailyProgressBetweenDates(String userId, long startDate, long endDate) {
        return Transformations.map(
                dailyProgressDao.getDailyProgressBetweenDates(userId, startDate, endDate),
                DailyProgressMapper::entityListToModelList
        );
    }    /**
     * Get the total duration of all yoga sessions for a user (in seconds)
     * @param userId The user's ID
     * @return The total duration in seconds
     */
    public int getTotalDurationForUser(String userId) {
        int[] duration = new int[1];
        Thread thread = new Thread(() -> {
            duration[0] = dailyProgressDao.getTotalDurationForUser(userId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return duration[0];
    }

    /**
     * Get the total calories burned in all yoga sessions for a user
     * @param userId The user's ID
     * @return The total calories burned
     */
    public int getTotalCaloriesForUser(String userId) {
        int[] calories = new int[1];
        Thread thread = new Thread(() -> {
            calories[0] = dailyProgressDao.getTotalCaloriesForUser(userId);
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
     * Get the total number of yoga sessions for a user
     * @param userId The user's ID
     * @return The total number of sessions
     */
    public int getTotalSessionsForUser(String userId) {
        int[] sessions = new int[1];
        Thread thread = new Thread(() -> {
            sessions[0] = dailyProgressDao.getTotalSessionsForUser(userId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return sessions[0];
    }    /**
     * Get the average score across all yoga sessions for a user
     * @param userId The user's ID
     * @return The average score
     */
    public float getAverageScoreForUser(String userId) {
        float[] score = new float[1];
        Thread thread = new Thread(() -> {
            score[0] = dailyProgressDao.getAverageScoreForUser(userId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return score[0];
    }

    /**
     * Get the latest daily progress record for a user
     * @param userId The user's ID
     * @return The latest DailyProgressEntity
     */
    public DailyProgress getLatestDailyProgress(String userId) {
        final DailyProgressEntity[] entity = new DailyProgressEntity[1];
        Thread thread = new Thread(() -> {
            entity[0] = dailyProgressDao.getLatestDailyProgress(userId);
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return DailyProgressMapper.entityToModel(entity[0]);
    }

    /**
     * Get the count of unique days on which the user practiced yoga
     * @param userId The user's ID
     * @return The count of unique practice days
     */
    public int getUniquePracticeDaysCount(String userId) {
        int[] count = new int[1];
        Thread thread = new Thread(() -> {
            count[0] = dailyProgressDao.getUniquePracticeDaysCount(userId);
        });
        thread.start();
        try {
            thread.join();        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        return count[0];
    }

    /**
     * Delete all daily progress records for a user
     * @param userId The user's ID
     */
    public void deleteAllDailyProgressForUser(String userId) {
        new Thread(() -> dailyProgressDao.deleteAllDailyProgressForUser(userId)).start();
    }
}
