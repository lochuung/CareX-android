package hcmute.edu.vn.loclinhvabao.carex.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.UserProfileDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaSessionDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.UserProfileEntity;
import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.YogaSessionEntity;

//@Database(entities = {}, version = 2)
@Database(entities = {YogaSessionEntity.class, UserProfileEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract YogaSessionDao yogaSessionDao();
    public abstract UserProfileDao userProfileDao();
}