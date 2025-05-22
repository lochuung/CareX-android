package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaPoseRepository;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Maps between YogaDayEntity (database) and YogaDay (UI model)
 */
public class YogaDayMapper {
    private final YogaPoseDao yogaPoseDao;
    private final YogaPoseRepository yogaPoseRepository;

    @Inject
    public YogaDayMapper(YogaPoseDao yogaPoseDao, YogaPoseRepository yogaPoseRepository) {
        this.yogaPoseDao = yogaPoseDao;
        this.yogaPoseRepository = yogaPoseRepository;
    }    /**
     * Convert YogaDayEntity to YogaDay
     * This is more complex because we need to load the poses from their IDs
     */
    public YogaDay entityToModel(YogaDayEntity entity) {
        if (entity == null) return null;
        
        // Parse pose IDs from JSON
        List<Integer> poseIds = YogaPoseMapper.parsePoseIdsFromJson(entity.getPosesJson());
        
        // Load all poses from background thread
        List<YogaPose> poses = loadPosesFromIds(poseIds);
        
        return new YogaDay(
            entity.getDayNumber(),
            entity.getTitle(),
            entity.getDescription(),
            poses
        );
    }
    
    /**
     * Load pose models from a list of pose IDs using background thread
     */
    private List<YogaPose> loadPosesFromIds(List<Integer> poseIds) {
        final List<YogaPose> poses = new ArrayList<>();
        
        if (poseIds == null || poseIds.isEmpty()) {
            return poses;
        }
        
        // Create a thread to load the poses synchronously from the database
        Thread thread = new Thread(() -> {
            List<YogaPoseEntity> poseEntities = yogaPoseRepository.getPoseEntitiesByIdsSync(poseIds);
            for (YogaPoseEntity entity : poseEntities) {
                poses.add(YogaPoseMapper.entityToModel(entity));
            }
        });
        
        // Start the thread and wait for it to complete
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return poses;
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
