package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.UserProfileDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaGoal;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.UserProfileMapper;

@Singleton
public class UserProfileRepository {
    private final UserProfileDao userProfileDao;
    private final Gson gson = new Gson();

    @Inject
    public UserProfileRepository(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    public void saveProfile(UserProfile profile) {
        if (profile.getId() == null) {
            profile.setId(UUID.randomUUID().toString());
        }

        new Thread(() -> userProfileDao.insertProfile(
                UserProfileMapper.modelToEntity(profile))).start();
    }

    public void updateProfile(UserProfile profile) {
        new Thread(() -> userProfileDao.updateProfile(
                UserProfileMapper.modelToEntity(profile))).start();
    }

    public LiveData<UserProfile> getUserProfile(String userId) {
        return Transformations.map(userProfileDao.getUserProfile(userId),
                UserProfileMapper::entityToModel);
    }

    public void updateNotificationSettings(String userId, boolean enabled, String time, List<String> days) {
        new Thread(() -> {
            userProfileDao.updateNotificationStatus(userId, enabled);
            userProfileDao.updateReminderTime(userId, time);
            userProfileDao.updateReminderDays(userId, gson.toJson(days));
        }).start();
    }

    public void updateStreak(String userId, int streak) {
        new Thread(() -> userProfileDao.updateStreak(userId, streak)).start();
    }

    public UserProfile createDefaultProfile(String userId, String name) {
        UserProfile profile = UserProfile.builder()
                .id(userId)
                .name(name)
                .height(175) // default height in cm
                .weight(70)  // default weight in kg
                .age(30)     // default age
                .goal(YogaGoal.FLEXIBILITY)
                .notificationsEnabled(true)
                .reminderTime("08:00") // default reminder time
                .reminderDays(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"))
                .currentStreak(0)
                .build();

        saveProfile(profile);
        return profile;
    }
}