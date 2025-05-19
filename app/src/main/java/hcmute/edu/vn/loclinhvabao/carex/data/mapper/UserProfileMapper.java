package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.UserProfileEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.UserProfile;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaGoal;

public class UserProfileMapper {
    private static final Gson gson = new Gson();

    public static UserProfile entityToModel(UserProfileEntity entity) {
        if (entity == null) return null;

        // Convert reminderDays from JSON
        List<String> reminderDays = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            reminderDays = gson.fromJson(entity.getReminderDaysJson(), listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        YogaGoal goal = YogaGoal.FLEXIBILITY; // Default
        try {
            goal = YogaGoal.valueOf(entity.getGoal());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return UserProfile.builder()
                .id(entity.getId())
                .name(entity.getName())
                .height(entity.getHeight())
                .weight(entity.getWeight())
                .age(entity.getAge())
                .goal(goal)
                .notificationsEnabled(entity.isNotificationsEnabled())
                .reminderTime(entity.getReminderTime())
                .reminderDays(reminderDays)
                .currentStreak(entity.getCurrentStreak())
                .build();
    }

    public static UserProfileEntity modelToEntity(UserProfile model) {
        if (model == null) return null;

        // Convert reminderDays to JSON
        String reminderDaysJson = "[]";
        try {
            reminderDaysJson = gson.toJson(model.getReminderDays());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return UserProfileEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .height(model.getHeight())
                .weight(model.getWeight())
                .age(model.getAge())
                .goal(model.getGoal().name())
                .notificationsEnabled(model.isNotificationsEnabled())
                .reminderTime(model.getReminderTime())
                .reminderDaysJson(reminderDaysJson)
                .currentStreak(model.getCurrentStreak())
                .build();
    }
}