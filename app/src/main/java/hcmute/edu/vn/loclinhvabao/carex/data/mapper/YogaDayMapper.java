package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Maps between YogaDayEntity (database) and YogaDay (UI model)
 */
public class YogaDayMapper {
    private final YogaPoseDao yogaPoseDao;

    @Inject
    public YogaDayMapper(YogaPoseDao yogaPoseDao) {
        this.yogaPoseDao = yogaPoseDao;
    }

    /**
     * Convert YogaDayEntity to YogaDay
     * This is more complex because we need to load the poses from their IDs
     */
    public YogaDay entityToModel(YogaDayEntity entity) {
        if (entity == null) return null;
        
        // Parse pose IDs from JSON
        List<Integer> poseIds = YogaPoseMapper.parsePoseIdsFromJson(entity.getPosesJson());
        
        // Load all poses - in a real app this would be done asynchronously
        // This is a simplification for demonstration purposes
        List<YogaPose> poses = new ArrayList<>();
        for (Integer poseId : poseIds) {
            // This synchronous approach is for demonstration only
            // In a real app, you would use LiveData and observe the result
            YogaPoseEntity poseEntity = yogaPoseDao.getPoseById(poseId).getValue();
            if (poseEntity != null) {
                poses.add(YogaPoseMapper.entityToModel(poseEntity));
            }
        }
        
        return new YogaDay(
            entity.getDayNumber(),
            entity.getTitle(),
            entity.getDescription(),
            poses
        );
    }
    
    /**
     * Convert a list of YogaDayEntity to a list of YogaDay
     */
    public List<YogaDay> entityListToModelList(List<YogaDayEntity> entities) {
        if (entities == null) return new ArrayList<>();
        
        List<YogaDay> models = new ArrayList<>();
        for (YogaDayEntity entity : entities) {
            models.add(entityToModel(entity));
        }
        return models;
    }
    
    /**
     * Convert YogaDay to YogaDayEntity
     */
    public YogaDayEntity modelToEntity(YogaDay model) {
        if (model == null) return null;
        
        // Extract pose IDs from the model's poses
        List<Integer> poseIds = new ArrayList<>();
        for (YogaPose pose : model.getPoses()) {
            poseIds.add(pose.getId());
        }
        
        return YogaDayEntity.builder()
            .dayNumber(model.getDayNumber())
            .title(model.getTitle())
            .description(model.getDescription())
            .posesJson(new com.google.gson.Gson().toJson(poseIds))
            .totalDuration(model.getTotalDuration())
            .category("") // These fields aren't in the YogaDay model
            .difficulty("")
            .focusArea("")
            .build();
    }
}
