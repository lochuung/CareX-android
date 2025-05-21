package hcmute.edu.vn.loclinhvabao.carex.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.DailyProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.ProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.UserProfileDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaDayDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaSessionDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.DailyProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.ProgressEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.UserProfileEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaDayEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaPoseEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaSessionEntity;

@Database(
    entities = {
        YogaSessionEntity.class,
        UserProfileEntity.class,
        YogaPoseEntity.class,
        YogaDayEntity.class,
        ProgressEntity.class,
        DailyProgressEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract YogaSessionDao yogaSessionDao();
    public abstract UserProfileDao userProfileDao();
    public abstract YogaPoseDao yogaPoseDao();
    public abstract YogaDayDao yogaDayDao();
    public abstract ProgressDao progressDao();
    public abstract DailyProgressDao dailyProgressDao();
}