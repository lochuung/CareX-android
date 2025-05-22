package hcmute.edu.vn.loclinhvabao.carex.data.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Maps between YogaPoseEntity (database) and YogaPose (UI model)
 */
public class YogaPoseMapper {
    private static final Gson gson = new Gson();

    /**
     * Convert YogaPoseEntity to YogaPose
     */
    public static YogaPose entityToModel(YogaPoseEntity entity) {
        if (entity == null) return null;
        
        return new YogaPose(
            entity.getId(),
            entity.getEnglishName(),
            entity.getSanskritName(),
            entity.getInstructions(),
            entity.getDurationInSeconds(),
            entity.getVideoUrl(),
            entity.getImageUrl()
        );
    }
    
    /**
     * Convert a list of YogaPoseEntity to a list of YogaPose
     */
    public static List<YogaPose> entityListToModelList(List<YogaPoseEntity> entities) {
        if (entities == null) return new ArrayList<>();
        
        List<YogaPose> models = new ArrayList<>();
        for (YogaPoseEntity entity : entities) {
            models.add(entityToModel(entity));
        }
        return models;
    }
    
    /**
     * Convert YogaPose to YogaPoseEntity
     */
    public static YogaPoseEntity modelToEntity(YogaPose model) {
        if (model == null) return null;
        
        return YogaPoseEntity.builder()
            .id(model.getId())
            .englishName(model.getEnglishName())
            .sanskritName(model.getSanskritName())
            .instructions(model.getInstructions())
            .durationInSeconds(model.getDurationInSeconds())
            .videoUrl(model.getVideoUrl())
            .imageUrl(model.getImageUrl())
            .category("")  // These fields aren't in the YogaPose model
            .difficulty("")
            .benefitsJson("[]")
            .build();
    }
    
    /**
     * Convert a list of YogaPose to a list of YogaPoseEntity
     */
    public static List<YogaPoseEntity> modelListToEntityList(List<YogaPose> models) {
        if (models == null) return new ArrayList<>();
        
        List<YogaPoseEntity> entities = new ArrayList<>();
        for (YogaPose model : models) {
            entities.add(modelToEntity(model));
        }
        return entities;
    }
    
    /**
     * Parse a JSON array of pose IDs into a list of integers
     */
    public static List<Integer> parsePoseIdsFromJson(String posesJson) {
        if (posesJson == null || posesJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<List<Integer>>(){}.getType();
            return gson.fromJson(posesJson, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
