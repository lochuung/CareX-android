package hcmute.edu.vn.loclinhvabao.carex;

import android.app.Application;

import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import hcmute.edu.vn.loclinhvabao.carex.util.NotificationUtils;

@HiltAndroidApp
public class CareXApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel
        NotificationUtils.createNotificationChannel(this);
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}