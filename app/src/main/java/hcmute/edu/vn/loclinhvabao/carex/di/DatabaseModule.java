package hcmute.edu.vn.loclinhvabao.carex.di;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import hcmute.edu.vn.loclinhvabao.carex.data.local.AppDatabase;
import hcmute.edu.vn.loclinhvabao.carex.data.local.DatabaseMigrations;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.DailyProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.ProgressDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.UserProfileDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaDayDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaPoseDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaSessionDao;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "carex.db")
                .addMigrations(DatabaseMigrations.MIGRATION_1_2) // Use defined migration strategy
                .fallbackToDestructiveMigration() // Fallback in case migration fails
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Better performance and reliability
                .build();
    }@Provides
    public static YogaSessionDao provideYogaSessionDao(AppDatabase appDatabase) {
        return appDatabase.yogaSessionDao();
    }

    @Provides
    public static UserProfileDao provideUserProfileDao(AppDatabase appDatabase) {
        return appDatabase.userProfileDao();
    }
    
    @Provides
    public static YogaPoseDao provideYogaPoseDao(AppDatabase appDatabase) {
        return appDatabase.yogaPoseDao();
    }
    
    @Provides
    public static YogaDayDao provideYogaDayDao(AppDatabase appDatabase) {
        return appDatabase.yogaDayDao();
    }
    
    @Provides
    public static ProgressDao provideProgressDao(AppDatabase appDatabase) {
        return appDatabase.progressDao();
    }
    
    @Provides
    public static DailyProgressDao provideDailyProgressDao(AppDatabase appDatabase) {
        return appDatabase.dailyProgressDao();
    }
}