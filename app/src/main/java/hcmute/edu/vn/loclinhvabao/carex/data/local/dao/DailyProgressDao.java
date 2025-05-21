package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.DailyProgressEntity;

@Dao
public interface DailyProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDailyProgress(DailyProgressEntity dailyProgress);

    @Update
    void updateDailyProgress(DailyProgressEntity dailyProgress);

    @Delete
    void deleteDailyProgress(DailyProgressEntity dailyProgress);

    @Query("SELECT * FROM daily_progress WHERE id = :id")
    LiveData<DailyProgressEntity> getDailyProgressById(String id);

    @Query("SELECT * FROM daily_progress WHERE userId = :userId ORDER BY dateTimestamp DESC")
    LiveData<List<DailyProgressEntity>> getAllDailyProgressForUser(String userId);

    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp ASC")
    LiveData<List<DailyProgressEntity>> getDailyProgressBetweenDates(String userId, long startDate, long endDate);

    @Query("SELECT SUM(totalDuration) FROM daily_progress WHERE userId = :userId")
    int getTotalDurationForUser(String userId);

    @Query("SELECT SUM(totalCalories) FROM daily_progress WHERE userId = :userId")
    int getTotalCaloriesForUser(String userId);

    @Query("SELECT SUM(sessionsCount) FROM daily_progress WHERE userId = :userId")
    int getTotalSessionsForUser(String userId);

    @Query("SELECT AVG(averageScore) FROM daily_progress WHERE userId = :userId")
    float getAverageScoreForUser(String userId);

    @Query("SELECT * FROM daily_progress WHERE userId = :userId ORDER BY dateTimestamp DESC LIMIT 1")
    DailyProgressEntity getLatestDailyProgress(String userId);

    @Query("SELECT COUNT(DISTINCT strftime('%Y-%m-%d', dateTimestamp / 1000, 'unixepoch')) FROM daily_progress WHERE userId = :userId")
    int getUniquePracticeDaysCount(String userId);
}
