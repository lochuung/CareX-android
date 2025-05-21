package hcmute.edu.vn.loclinhvabao.carex.data.local;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaDayDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;

/**
 * This class seeds the database with initial data for yoga poses and program days.
 * It should be called once during app initialization if the database is empty.
 */
@Singleton
public class DataSeeder {
    private final YogaPoseDao yogaPoseDao;
    private final YogaDayDao yogaDayDao;
    private final Executor executor;
    private final Gson gson;

    @Inject
    public DataSeeder(YogaPoseDao yogaPoseDao, YogaDayDao yogaDayDao) {
        this.yogaPoseDao = yogaPoseDao;
        this.yogaDayDao = yogaDayDao;
        this.executor = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
    }

    /**
     * Check if seeding is needed and perform it if necessary
     */
    public void seedDatabaseIfNeeded() {
        executor.execute(() -> {
            if (yogaPoseDao.getPosesCount() == 0) {
                // Database is empty, seed it
                List<YogaPoseEntity> poses = createInitialPoses();
                yogaPoseDao.insertAllPoses(poses);
                
                // After seeding poses, create and seed program days
                if (yogaDayDao.getDaysCount() == 0) {
                    List<YogaDayEntity> days = createInitialDays();
                    yogaDayDao.insertAllDays(days);
                }
            }
        });
    }

    /**
     * Create initial set of yoga poses
     */
    private List<YogaPoseEntity> createInitialPoses() {
        List<YogaPoseEntity> poses = new ArrayList<>();
        
        // Chair Pose (ID: 1)
        poses.add(YogaPoseEntity.builder()
                .id(1)
                .englishName("Chair Pose")
                .sanskritName("Utkatasana")
                .instructions("Stand with feet hip-width apart, bend knees as if sitting in a chair, while raising arms overhead. Keep chest lifted and weight in heels.")
                .durationInSeconds(60)
                .videoUrl("https://www.youtube.com/watch?v=tEZhXr0FuAQ")
                .imageUrl("https://images.pexels.com/photos/7625226/pexels-photo-7625226.jpeg")
                .category("Standing")
                .difficulty("Beginner")
                .benefitsJson(gson.toJson(Arrays.asList("Strengthens thighs", "Tones abdomen", "Improves balance")))
                .build());
        
        // Downward Facing Dog (ID: 2)
        poses.add(YogaPoseEntity.builder()
                .id(2)
                .englishName("Downward Facing Dog")
                .sanskritName("Adho Mukha Svanasana")
                .instructions("Start on all fours, tuck toes and lift hips up and back forming an inverted V. Press through your hands, engage your legs and draw your heels toward the floor.")
                .durationInSeconds(60)
                .videoUrl("https://www.youtube.com/watch?v=j97SSGsnCAQ")
                .imageUrl("https://images.pexels.com/photos/6111609/pexels-photo-6111609.jpeg")
                .category("Inversion")
                .difficulty("Beginner")
                .benefitsJson(gson.toJson(Arrays.asList("Stretches hamstrings", "Strengthens arms", "Calms the nervous system")))
                .build());
                
        // Add more poses here...
        
        return poses;
    }

    /**
     * Create initial set of yoga program days
     */
    private List<YogaDayEntity> createInitialDays() {
        List<YogaDayEntity> days = new ArrayList<>();
        
        // Day 1 - Foundation
        days.add(YogaDayEntity.builder()
                .dayNumber(1)
                .title("Day 1 - Foundation")
                .description("Learn the basics of yoga poses and breathing techniques.")
                .posesJson(gson.toJson(Arrays.asList(1, 2, 3))) // IDs of yoga poses for this day
                .totalDuration(180) // 3 poses x 60 seconds each
                .category("Foundation")
                .difficulty("Beginner")
                .focusArea("Full Body")
                .build());
                
        // Add more days here...
        
        return days;
    }
}
