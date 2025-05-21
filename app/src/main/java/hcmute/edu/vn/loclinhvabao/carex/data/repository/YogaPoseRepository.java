package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.YogaPoseMapper;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Repository for managing yoga poses
 * Provides a clean API for ViewModel to interact with the data layer
 */
@Singleton
public class YogaPoseRepository {
    private final YogaPoseDao yogaPoseDao;

    @Inject
    public YogaPoseRepository(YogaPoseDao yogaPoseDao) {
        this.yogaPoseDao = yogaPoseDao;
    }

    /**
     * Insert a new yoga pose
     * @param pose The YogaPose object to insert
     */
    public void insertPose(YogaPose pose) {
        YogaPoseEntity entity = YogaPoseMapper.modelToEntity(pose);
        new Thread(() -> yogaPoseDao.insertPose(entity)).start();
    }

    /**
     * Insert a list of yoga poses
     * @param poses The list of YogaPose objects to insert
     */
    public void insertAllPoses(List<YogaPose> poses) {
        List<YogaPoseEntity> entities = YogaPoseMapper.modelListToEntityList(poses);
        new Thread(() -> yogaPoseDao.insertAllPoses(entities)).start();
    }

    /**
     * Update an existing yoga pose
     * @param pose The YogaPose object to update
     */
    public void updatePose(YogaPose pose) {
        YogaPoseEntity entity = YogaPoseMapper.modelToEntity(pose);
        new Thread(() -> yogaPoseDao.updatePose(entity)).start();
    }

    /**
     * Delete a yoga pose
     * @param pose The YogaPose object to delete
     */
    public void deletePose(YogaPose pose) {
        YogaPoseEntity entity = YogaPoseMapper.modelToEntity(pose);
        new Thread(() -> yogaPoseDao.deletePose(entity)).start();
    }

    /**
     * Get a yoga pose by its ID
     * @param poseId The ID of the pose to retrieve
     * @return A LiveData object containing the requested YogaPose
     */
    public LiveData<YogaPose> getPoseById(int poseId) {
        return Transformations.map(
                yogaPoseDao.getPoseById(poseId),
                YogaPoseMapper::entityToModel
        );
    }

    /**
     * Get all yoga poses
     * @return A LiveData object containing a list of all YogaPose objects
     */
    public LiveData<List<YogaPose>> getAllPoses() {
        return Transformations.map(
                yogaPoseDao.getAllPoses(),
                YogaPoseMapper::entityListToModelList
        );
    }

    /**
     * Get yoga poses by category
     * @param category The category to filter by
     * @return A LiveData object containing a list of YogaPose objects in the specified category
     */
    public LiveData<List<YogaPose>> getPosesByCategory(String category) {
        return Transformations.map(
                yogaPoseDao.getPosesByCategory(category),
                YogaPoseMapper::entityListToModelList
        );
    }

    /**
     * Get yoga poses by difficulty level
     * @param difficulty The difficulty level to filter by
     * @return A LiveData object containing a list of YogaPose objects with the specified difficulty
     */
    public LiveData<List<YogaPose>> getPosesByDifficulty(String difficulty) {
        return Transformations.map(
                yogaPoseDao.getPosesByDifficulty(difficulty),
                YogaPoseMapper::entityListToModelList
        );
    }

    /**
     * Search for yoga poses by name
     * @param query The search query
     * @return A LiveData object containing a list of matching YogaPose objects
     */
    public LiveData<List<YogaPose>> searchPoses(String query) {
        String searchQuery = "%" + query + "%";
        return Transformations.map(
                yogaPoseDao.searchPoses(searchQuery),
                YogaPoseMapper::entityListToModelList
        );
    }
}
