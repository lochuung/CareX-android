package hcmute.edu.vn.loclinhvabao.carex.di;

import android.content.Context;

import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.WorkManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class WorkModule {

    @Provides
    @Singleton
    public WorkManager provideWorkManager(@ApplicationContext Context context) {
        return WorkManager.getInstance(context);
    }
}