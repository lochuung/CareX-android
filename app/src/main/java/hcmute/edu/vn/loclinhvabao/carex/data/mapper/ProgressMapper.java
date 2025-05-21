package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.ProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.Progress;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Maps between ProgressEntity (database) and Progress (model)
 */
public class ProgressMapper {
    private static final Gson gson = new Gson();
    private final YogaPoseDao yogaPoseDao;

    @Inject
    public ProgressMapper(YogaPoseDao yogaPoseDao) {
        this.yogaPoseDao = yogaPoseDao;
    }

    /**
     * Convert ProgressEntity to Progress model
     */
    public Progress entityToModel(ProgressEntity entity) {
        if (entity == null) return null;

        // Parse completed poses from JSON
        List<Integer> poseIds = new ArrayList<>();
        try {
            Type listType = new TypeToken<List<Integer>>(){}.getType();
            poseIds = gson.fromJson(entity.getCompletedPosesJson(), listType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Map pose IDs to YogaPose objects
        List<YogaPose> completedPoses = new ArrayList<>();
        for (Integer poseId : poseIds) {
            // In a real app, you would use LiveData and async approach
            // This is simplified for the example
            if (yogaPoseDao.getPoseById(poseId).getValue() != null) {
                completedPoses.add(YogaPoseMapper.entityToModel(
                    yogaPoseDao.getPoseById(poseId).getValue()));
            }
        }

        return Progress.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .dayNumber(entity.getDayNumber())
                .isCompleted(entity.isCompleted())
                .completionDate(new Date(entity.getCompletionTimestamp()))
                .duration(entity.getDuration())
                .calories(entity.getCalories())
                .averageConfidence(entity.getAverageConfidence())
                .completedPoses(completedPoses)
                .build();
    }

    /**
     * Convert Progress model to ProgressEntity
     */
    public ProgressEntity modelToEntity(Progress model) {
        if (model == null) return null;

        // Extract pose IDs from the model's poses
        List<Integer> poseIds = new ArrayList<>();
        if (model.getCompletedPoses() != null) {
            for (YogaPose pose : model.getCompletedPoses()) {
                poseIds.add(pose.getId());
            }
        }

        return ProgressEntity.builder()
                .id(model.getId() != null ? model.getId() : UUID.randomUUID().toString())
                .userId(model.getUserId())
                .dayNumber(model.getDayNumber())
                .isCompleted(model.isCompleted())
                .completionTimestamp(model.getCompletionDate() != null ? 
                    model.getCompletionDate().getTime() : System.currentTimeMillis())
                .duration(model.getDuration())
                .calories(model.getCalories())
                .averageConfidence(model.getAverageConfidence())
                .completedPosesJson(gson.toJson(poseIds))
                .build();
    }

    /**
     * Convert a list of ProgressEntity to a list of Progress models
     */
    public List<Progress> entityListToModelList(List<ProgressEntity> entities) {
        if (entities == null) return new ArrayList<>();
        
        List<Progress> models = new ArrayList<>();
        for (ProgressEntity entity : entities) {
            models.add(entityToModel(entity));
        }
        return models;
    }
}
