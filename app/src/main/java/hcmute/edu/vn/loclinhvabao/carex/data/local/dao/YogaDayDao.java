package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;

@Dao
public interface YogaDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDay(YogaDayEntity day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllDays(List<YogaDayEntity> days);

    @Update
    void updateDay(YogaDayEntity day);

    @Delete
    void deleteDay(YogaDayEntity day);

    @Query("SELECT * FROM yoga_days WHERE dayNumber = :dayNumber")
    LiveData<YogaDayEntity> getDayByNumber(int dayNumber);

    @Query("SELECT * FROM yoga_days ORDER BY dayNumber ASC")
    LiveData<List<YogaDayEntity>> getAllDays();

    @Query("SELECT * FROM yoga_days WHERE category = :category ORDER BY dayNumber ASC")
    LiveData<List<YogaDayEntity>> getDaysByCategory(String category);

    @Query("SELECT * FROM yoga_days WHERE difficulty = :difficulty ORDER BY dayNumber ASC")
    LiveData<List<YogaDayEntity>> getDaysByDifficulty(String difficulty);

    @Query("SELECT * FROM yoga_days WHERE focusArea = :focusArea ORDER BY dayNumber ASC")
    LiveData<List<YogaDayEntity>> getDaysByFocusArea(String focusArea);

    @Query("SELECT COUNT(*) FROM yoga_days")
    int getDaysCount();
}
