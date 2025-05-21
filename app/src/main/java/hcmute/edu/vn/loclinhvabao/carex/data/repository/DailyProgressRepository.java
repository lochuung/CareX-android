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
    }

    /**
     * Insert a new daily progress record
     * @param dailyProgress The DailyProgress object to insert
     * @param userId The user's ID
     */
    public void insertDailyProgress(DailyProgress dailyProgress, String userId) {
        DailyProgressEntity entity = DailyProgressMapper.modelToEntity(dailyProgress, userId);
        new Thread(() -> dailyProgressDao.insertDailyProgress(entity)).start();
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
    }

    /**
     * Get the total duration of all yoga sessions for a user (in seconds)
     * @param userId The user's ID
     * @return The total duration in seconds
     */
    public int getTotalDurationForUser(String userId) {
        return dailyProgressDao.getTotalDurationForUser(userId);
    }

    /**
     * Get the total calories burned in all yoga sessions for a user
     * @param userId The user's ID
     * @return The total calories burned
     */
    public int getTotalCaloriesForUser(String userId) {
        return dailyProgressDao.getTotalCaloriesForUser(userId);
    }

    /**
     * Get the total number of yoga sessions for a user
     * @param userId The user's ID
     * @return The total number of sessions
     */
    public int getTotalSessionsForUser(String userId) {
        return dailyProgressDao.getTotalSessionsForUser(userId);
    }

    /**
     * Get the average score across all yoga sessions for a user
     * @param userId The user's ID
     * @return The average score
     */
    public float getAverageScoreForUser(String userId) {
        return dailyProgressDao.getAverageScoreForUser(userId);
    }

    /**
     * Get the latest daily progress record for a user
     * @param userId The user's ID
     * @return The latest DailyProgressEntity
     */
    public DailyProgress getLatestDailyProgress(String userId) {
        DailyProgressEntity entity = dailyProgressDao.getLatestDailyProgress(userId);
        return DailyProgressMapper.entityToModel(entity);
    }

    /**
     * Get the count of unique days on which the user practiced yoga
     * @param userId The user's ID
     * @return The count of unique practice days
     */
    public int getUniquePracticeDaysCount(String userId) {
        return dailyProgressDao.getUniquePracticeDaysCount(userId);
    }
}
