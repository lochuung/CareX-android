package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaSessionDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaSessionEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.ProgressStats;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.data.mapper.YogaSessionMapper;

@Singleton
public class YogaRepository {
    private final YogaSessionDao yogaSessionDao;

    @Inject
    public YogaRepository(YogaSessionDao yogaSessionDao) {
        this.yogaSessionDao = yogaSessionDao;
    }

    public void saveSession(YogaSession session) {
        if (session.getId() == null) {
            session.setId(UUID.randomUUID().toString());
        }

        YogaSessionEntity entity = YogaSessionMapper.modelToEntity(session);
        new Thread(() -> yogaSessionDao.insertSession(entity)).start();
    }

    public void updateSession(YogaSession session) {
        YogaSessionEntity entity = YogaSessionMapper.modelToEntity(session);
        new Thread(() -> yogaSessionDao.updateSession(entity)).start();
    }

    public void deleteSession(YogaSession session) {
        YogaSessionEntity entity = YogaSessionMapper.modelToEntity(session);
        new Thread(() -> yogaSessionDao.deleteSession(entity)).start();
    }

    public YogaSession getSessionById(String id) {
        YogaSessionEntity entity = yogaSessionDao.getSessionById(id);
        return YogaSessionMapper.entityToModel(entity);
    }

    public LiveData<List<YogaSession>> getAllSessions() {
        return Transformations.map(yogaSessionDao.getAllSessions(),
                YogaSessionMapper::entityListToModelList);
    }

    public LiveData<List<YogaSession>> getRecentSessions(int limit) {
        return Transformations.map(yogaSessionDao.getRecentSessions(limit),
                YogaSessionMapper::entityListToModelList);
    }

    public LiveData<List<YogaSession>> getSessionsBetweenDates(Date startDate, Date endDate) {
        return Transformations.map(
                yogaSessionDao.getSessionsBetweenDates(
                        startDate.getTime(), endDate.getTime()),
                YogaSessionMapper::entityListToModelList
        );
    }

    public ProgressStats calculateStats() {
        int totalSessions = yogaSessionDao.getTotalSessionsCount();
        int totalMinutes = yogaSessionDao.getTotalDuration();
        int totalCalories = yogaSessionDao.getTotalCalories();
        String favoriteStyle = yogaSessionDao.getFavoriteYogaStyle();

        // Calculate weekly stats
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30); // Last 30 days

        float avgSessionsPerWeek = totalSessions / 4f; // Assuming 4 weeks in a month
        float avgDurationPerSession = totalSessions > 0 ? (float) totalMinutes / totalSessions : 0;
        float avgCaloriesPerSession = totalSessions > 0 ? (float) totalCalories / totalSessions : 0;

        // For a complete implementation, fetch the weekly and monthly data
        // and transform it into DailyProgress objects

        return ProgressStats.builder()
                .totalSessions(totalSessions)
                .totalMinutes(totalMinutes)
                .totalCalories(totalCalories)
                .avgSessionsPerWeek(avgSessionsPerWeek)
                .avgDurationPerSession(avgDurationPerSession)
                .avgCaloriesPerSession(avgCaloriesPerSession)
                .favoriteYogaStyle(favoriteStyle)
                .mostConsistentDay("Monday") // Should be calculated from actual data
                .weeklyProgress(new ArrayList<>()) // Placeholder for actual data
                .monthlyProgress(new ArrayList<>()) // Placeholder for actual data
                .yogaStyleDistribution(new HashMap<>()) // Placeholder for actual data
                .build();
    }
}