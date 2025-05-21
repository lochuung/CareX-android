package hcmute.edu.vn.loclinhvabao.carex;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.hilt.work.HiltWorkerFactory;
import androidx.room.Room;
import androidx.work.Configuration;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import hcmute.edu.vn.loclinhvabao.carex.data.local.AppDatabase;
import hcmute.edu.vn.loclinhvabao.carex.data.local.DataSeeder;
import hcmute.edu.vn.loclinhvabao.carex.data.local.DatabaseMigrations;
import hcmute.edu.vn.loclinhvabao.carex.util.NotificationUtils;

@HiltAndroidApp
public class CareXApplication extends Application implements Configuration.Provider {

    private static final String TAG = "CareXApplication";

    @Inject
    HiltWorkerFactory workerFactory;
    
    @Inject
    DataSeeder dataSeeder;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel
        NotificationUtils.createNotificationChannel(this);
        
        // Seed database with initial data if needed
        try {
            dataSeeder.seedDatabaseIfNeeded();
        } catch (Exception e) {
            Log.e(TAG, "Error seeding database: " + e.getMessage(), e);
            // If database is corrupted, recreate it
            handleDatabaseCorruption(getApplicationContext());
        }
    }

    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
    
    /**
     * Handle database corruption by deleting and recreating the database.
     * This is a last resort solution when migrations fail.
     */
    private void handleDatabaseCorruption(Context context) {
        try {
            Log.w(TAG, "Attempting to handle database corruption by rebuilding the database");
            context.deleteDatabase("carex.db");
            
            // Rebuild database with fallback to destructive migration
            Room.databaseBuilder(context, AppDatabase.class, "carex.db")
                    .addMigrations(DatabaseMigrations.MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build();
                    
            Log.i(TAG, "Database rebuilt successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to rebuild database: " + e.getMessage(), e);
        }
    }
}