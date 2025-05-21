package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaDayDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.YogaDayMapper;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;

/**
 * Repository for managing yoga program days
 * Provides a clean API for ViewModel to interact with the data layer
 */
@Singleton
public class YogaDayRepository {
    private final YogaDayDao yogaDayDao;
    private final YogaDayMapper yogaDayMapper;

    @Inject
    public YogaDayRepository(YogaDayDao yogaDayDao, YogaDayMapper yogaDayMapper) {
        this.yogaDayDao = yogaDayDao;
        this.yogaDayMapper = yogaDayMapper;
    }

    /**
     * Insert a new yoga day
     * @param day The YogaDay object to insert
     */
    public void insertDay(YogaDay day) {
        YogaDayEntity entity = yogaDayMapper.modelToEntity(day);
        new Thread(() -> yogaDayDao.insertDay(entity)).start();
    }

    /**
     * Insert a list of yoga days
     * @param days The list of YogaDay objects to insert
     */
    public void insertAllDays(List<YogaDay> days) {
        List<YogaDayEntity> entities = new java.util.ArrayList<>();
        for (YogaDay day : days) {
            entities.add(yogaDayMapper.modelToEntity(day));
        }
        new Thread(() -> yogaDayDao.insertAllDays(entities)).start();
    }

    /**
     * Update an existing yoga day
     * @param day The YogaDay object to update
     */
    public void updateDay(YogaDay day) {
        YogaDayEntity entity = yogaDayMapper.modelToEntity(day);
        new Thread(() -> yogaDayDao.updateDay(entity)).start();
    }

    /**
     * Delete a yoga day
     * @param day The YogaDay object to delete
     */
    public void deleteDay(YogaDay day) {
        YogaDayEntity entity = yogaDayMapper.modelToEntity(day);
        new Thread(() -> yogaDayDao.deleteDay(entity)).start();
    }

    /**
     * Get a yoga day by its number
     * @param dayNumber The number of the day to retrieve
     * @return A LiveData object containing the requested YogaDay
     */
    public LiveData<YogaDay> getDayByNumber(int dayNumber) {
        return Transformations.map(
                yogaDayDao.getDayByNumber(dayNumber),
                yogaDayMapper::entityToModel
        );
    }

    /**
     * Get all yoga days in the program
     * @return A LiveData object containing a list of all YogaDay objects
     */
    public LiveData<List<YogaDay>> getAllDays() {
        return Transformations.map(
                yogaDayDao.getAllDays(),
                yogaDayMapper::entityListToModelList
        );
    }

    /**
     * Get yoga days by category
     * @param category The category to filter by
     * @return A LiveData object containing a list of YogaDay objects in the specified category
     */
    public LiveData<List<YogaDay>> getDaysByCategory(String category) {
        return Transformations.map(
                yogaDayDao.getDaysByCategory(category),
                yogaDayMapper::entityListToModelList
        );
    }

    /**
     * Get yoga days by difficulty level
     * @param difficulty The difficulty level to filter by
     * @return A LiveData object containing a list of YogaDay objects with the specified difficulty
     */
    public LiveData<List<YogaDay>> getDaysByDifficulty(String difficulty) {
        return Transformations.map(
                yogaDayDao.getDaysByDifficulty(difficulty),
                yogaDayMapper::entityListToModelList
        );
    }

    /**
     * Get yoga days by focus area
     * @param focusArea The focus area to filter by
     * @return A LiveData object containing a list of YogaDay objects with the specified focus area
     */
    public LiveData<List<YogaDay>> getDaysByFocusArea(String focusArea) {
        return Transformations.map(
                yogaDayDao.getDaysByFocusArea(focusArea),
                yogaDayMapper::entityListToModelList
        );
    }

    /**
     * Get the total number of days in the program
     * @return The count of days in the yoga program
     */
    public int getDaysCount() {
        return yogaDayDao.getDaysCount();
    }
}
