package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;

@Dao
public interface YogaPoseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPose(YogaPoseEntity pose);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPoses(List<YogaPoseEntity> poses);

    @Update
    void updatePose(YogaPoseEntity pose);

    @Delete
    void deletePose(YogaPoseEntity pose);

    @Query("SELECT * FROM yoga_poses WHERE id = :poseId")
    LiveData<YogaPoseEntity> getPoseById(int poseId);
    
    @Query("SELECT * FROM yoga_poses ORDER BY id")
    LiveData<List<YogaPoseEntity>> getAllPoses();
    
    @Query("SELECT * FROM yoga_poses WHERE category = :category ORDER BY id")
    LiveData<List<YogaPoseEntity>> getPosesByCategory(String category);
    
    @Query("SELECT * FROM yoga_poses WHERE difficulty = :difficulty ORDER BY id")
    LiveData<List<YogaPoseEntity>> getPosesByDifficulty(String difficulty);
    
    @Query("SELECT * FROM yoga_poses WHERE englishName LIKE '%' || :query || '%' OR sanskritName LIKE '%' || :query || '%' ORDER BY id")
    LiveData<List<YogaPoseEntity>> searchPoses(String query);
    
    @Query("SELECT COUNT(*) FROM yoga_poses")
    int getPosesCount();
}
