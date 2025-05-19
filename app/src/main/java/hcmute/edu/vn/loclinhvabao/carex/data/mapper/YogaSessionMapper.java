package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaSessionEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaPose;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;


public class YogaSessionMapper {
    private static final Gson gson = new Gson();

    public static YogaSession entityToModel(YogaSessionEntity entity) {
        if (entity == null) return null;

        // Convert poses from JSON
        List<YogaPose> poses = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<YogaPose>>(){}.getType();
            poses = gson.fromJson(entity.getPosesJson(), listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return YogaSession.builder()
                .id(entity.getId())
                .date(new Date(entity.getDateTimestamp()))
                .title(entity.getTitle())
                .type(entity.getType())
                .duration(entity.getDuration())
                .calories(entity.getCalories())
                .poses(poses)
                .completionRate(entity.getCompletionRate())
                .overallScore(entity.getOverallScore())
                .notes(entity.getNotes())
                .build();
    }

    public static YogaSessionEntity modelToEntity(YogaSession model) {
        if (model == null) return null;

        // Convert poses to JSON
        String posesJson = "[]";
        try {
            posesJson = gson.toJson(model.getPoses());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return YogaSessionEntity.builder()
                .id(model.getId())
                .dateTimestamp(model.getDate().getTime())
                .title(model.getTitle())
                .type(model.getType())
                .duration(model.getDuration())
                .calories(model.getCalories())
                .posesJson(posesJson)
                .completionRate(model.getCompletionRate())
                .overallScore(model.getOverallScore())
                .notes(model.getNotes())
                .build();
    }

    public static List<YogaSession> entityListToModelList(List<YogaSessionEntity> entities) {
        List<YogaSession> models = new ArrayList<>();
        if (entities != null) {
            for (YogaSessionEntity entity : entities) {
                models.add(entityToModel(entity));
            }
        }
        return models;
    }
}