package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaSessionEntity;

@Dao
public interface YogaSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(YogaSessionEntity session);

    @Update
    void updateSession(YogaSessionEntity session);

    @Delete
    void deleteSession(YogaSessionEntity session);

    @Query("SELECT * FROM yoga_sessions WHERE id = :id")
    YogaSessionEntity getSessionById(String id);

    @Query("SELECT * FROM yoga_sessions ORDER BY dateTimestamp DESC")
    LiveData<List<YogaSessionEntity>> getAllSessions();

    @Query("SELECT * FROM yoga_sessions WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC")
    LiveData<List<YogaSessionEntity>> getSessionsBetweenDates(long startDate, long endDate);

    @Query("SELECT COUNT(*) FROM yoga_sessions")
    int getTotalSessionsCount();

    @Query("SELECT SUM(duration) FROM yoga_sessions")
    int getTotalDuration();

    @Query("SELECT SUM(calories) FROM yoga_sessions")
    int getTotalCalories();

    @Query("SELECT * FROM yoga_sessions ORDER BY dateTimestamp DESC LIMIT :limit")
    LiveData<List<YogaSessionEntity>> getRecentSessions(int limit);

    @Query("SELECT type FROM yoga_sessions GROUP BY type ORDER BY COUNT(*) DESC LIMIT 1")
    String getFavoriteYogaStyle();
}