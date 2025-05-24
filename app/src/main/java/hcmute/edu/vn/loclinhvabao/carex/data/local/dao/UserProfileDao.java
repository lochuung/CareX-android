package hcmute.edu.vn.loclinhvabao.carex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.UserProfileEntity;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfile(UserProfileEntity profile);

    @Update
    void updateProfile(UserProfileEntity profile);

    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    LiveData<UserProfileEntity> getUserProfile(String userId);

    @Query("UPDATE user_profiles SET notificationsEnabled = :enabled WHERE id = :userId")
    void updateNotificationStatus(String userId, boolean enabled);

    @Query("UPDATE user_profiles SET reminderTime = :time WHERE id = :userId")
    void updateReminderTime(String userId, String time);

    @Query("UPDATE user_profiles SET reminderDaysJson = :daysJson WHERE id = :userId")
    void updateReminderDays(String userId, String daysJson);

    @Query("UPDATE user_profiles SET currentStreak = :streak WHERE id = :userId")
    void updateStreak(String userId, int streak);

    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    UserProfileEntity getUserProfileSync(String userId);

    @Query("DELETE FROM user_profiles")
    void deleteAllProfiles();

    @Delete
    void deleteProfile(UserProfileEntity profile);
}