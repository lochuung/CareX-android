package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.HealthData;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.dto.TotalDistance;


@Dao
public interface HealthDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HealthData data);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HealthData> dataList);
    
    @Update
    void update(HealthData data);
    
    @Query("SELECT * FROM health_data WHERE date = :date AND isWorkout = 0 LIMIT 1")
    LiveData<HealthData> getDailyHealthDataForDate(String date);
    
    @Query("SELECT * FROM health_data WHERE date = :date AND isWorkout = 1")
    LiveData<List<HealthData>> getWorkoutsForDate(String date);
    
    @Query("SELECT * FROM health_data WHERE date BETWEEN :startDate AND :endDate AND isWorkout = 0")
    LiveData<List<HealthData>> getDailyHealthDataBetweenDates(String startDate, String endDate);
    
    @Query("SELECT * FROM health_data WHERE isWorkout = 1 ORDER BY date DESC, workoutTime DESC LIMIT :limit")
    LiveData<List<HealthData>> getRecentWorkouts(int limit);
    
    @Query("SELECT SUM(steps) FROM health_data WHERE date = :date AND isWorkout = 0")
    LiveData<Integer> getTotalStepsForDate(String date);
    
    @Query("SELECT SUM(calories) FROM health_data WHERE date = :date")
    LiveData<Integer> getTotalCaloriesForDate(String date);
    
    @Query("SELECT SUM(distance) AS total FROM health_data WHERE date = :date")
    LiveData<TotalDistance> getTotalDistanceForDate(String date);

    @Query("DELETE FROM health_data WHERE date = :date AND isWorkout = 0")
    void deleteDailyDataForDate(String date);
    @Transaction
    default void replaceTodayData(String date, HealthData newData) {
        deleteDailyDataForDate(date);
        insert(newData);
    }
}