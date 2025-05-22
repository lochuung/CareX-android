package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.DailyProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.DailyProgress;

/**
 * Maps between DailyProgressEntity (database) and DailyProgress (model)
 */
public class DailyProgressMapper {
    private static final Gson gson = new Gson();

    /**
     * Convert DailyProgressEntity to DailyProgress model
     */
    public static DailyProgress entityToModel(DailyProgressEntity entity) {
        if (entity == null) return null;

        // Parse yoga styles from JSON
        List<String> yogaStyles = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            yogaStyles = gson.fromJson(entity.getYogaStylesJson(), listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert duration from seconds to minutes for the model
        int durationMinutes = entity.getTotalDuration() / 60;

        return DailyProgress.builder()
                .date(new Date(entity.getDateTimestamp()))
                .duration(durationMinutes)  // Store duration in minutes in the model
                .calories(entity.getTotalCalories())
                .score(entity.getAverageScore())
                .build();
    }

    /**
     * Convert DailyProgress model to DailyProgressEntity
     */
    public static DailyProgressEntity modelToEntity(DailyProgress model, String userId) {
        if (model == null) return null;

        // Convert duration from minutes to seconds for storage
        int durationSeconds = model.getDuration() * 60;

        return DailyProgressEntity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .dateTimestamp(model.getDate().getTime())
                .totalDuration(durationSeconds)  // Store duration in seconds in the entity
                .totalCalories(model.getCalories())
                .sessionsCount(1)  // Default to 1 session if not specified
                .averageScore(model.getScore())
                .yogaStylesJson(gson.toJson(new ArrayList<>())) // Empty list if not specified
                .build();
    }

    /**
     * Convert a list of DailyProgressEntity to a list of DailyProgress models
     */
    public static List<DailyProgress> entityListToModelList(List<DailyProgressEntity> entities) {
        if (entities == null) return new ArrayList<>();
        
        List<DailyProgress> models = new ArrayList<>();
        for (DailyProgressEntity entity : entities) {
            models.add(entityToModel(entity));
        }
        return models;
    }
}
