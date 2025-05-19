package hcmute.edu.vn.loclinhvabao.carex.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import hcmute.edu.vn.loclinhvabao.carex.data.local.AppDatabase;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.UserProfileDao;
import hcmute.edu.vn.loclinhvabao.carex.data.local.dao.YogaSessionDao;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "carex.db")
                .fallbackToDestructiveMigration() // Cho phép xóa và tạo lại database khi version thay đổi
                .build();
    }
    @Provides
    public static YogaSessionDao provideYogaSessionDao(AppDatabase appDatabase) {
        return appDatabase.yogaSessionDao();
    }

    @Provides
    public static UserProfileDao provideUserProfileDao(AppDatabase appDatabase) {
        return appDatabase.userProfileDao();
    }
}