package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.ProgressEntity;

@Dao
public interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(ProgressEntity progress);

    @Update
    void updateProgress(ProgressEntity progress);

    @Delete
    void deleteProgress(ProgressEntity progress);

    @Query("SELECT * FROM progress WHERE id = :progressId")
    LiveData<ProgressEntity> getProgressById(String progressId);

    @Query("SELECT * FROM progress WHERE userId = :userId ORDER BY completionTimestamp DESC")
    LiveData<List<ProgressEntity>> getAllProgressForUser(String userId);

    @Query("SELECT * FROM progress WHERE userId = :userId AND dayNumber = :dayNumber")
    LiveData<ProgressEntity> getProgressForDay(String userId, int dayNumber);

    @Query("SELECT * FROM progress WHERE userId = :userId AND isCompleted = 1 ORDER BY completionTimestamp DESC")
    LiveData<List<ProgressEntity>> getCompletedProgressForUser(String userId);

    @Query("SELECT COUNT(*) FROM progress WHERE userId = :userId AND isCompleted = 1")
    int getCompletedDaysCount(String userId);

    @Query("SELECT COUNT(*) FROM progress WHERE userId = :userId AND isCompleted = 1 AND completionTimestamp BETWEEN :startTime AND :endTime")
    int getCompletedDaysInTimeRange(String userId, long startTime, long endTime);

    @Query("SELECT SUM(calories) FROM progress WHERE userId = :userId AND completionTimestamp BETWEEN :startTime AND :endTime")
    int getTotalCaloriesInTimeRange(String userId, long startTime, long endTime);

    @Query("SELECT AVG(averageConfidence) FROM progress WHERE userId = :userId AND isCompleted = 1")
    float getAverageConfidence(String userId);
}
