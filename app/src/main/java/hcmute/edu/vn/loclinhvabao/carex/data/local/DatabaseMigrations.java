package hcmute.edu.vn.loclinhvabao.carex.data.local;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Migration from version 1 to version 2 of the database.
 * This migration adds new tables for yoga poses, yoga days, progress tracking, and daily progress.
 */
public class DatabaseMigrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create yoga_poses table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `yoga_poses` (" +
                "`id` INTEGER NOT NULL, " +
                "`englishName` TEXT, " +
                "`sanskritName` TEXT, " +
                "`instructions` TEXT, " +
                "`durationInSeconds` INTEGER NOT NULL, " +
                "`videoUrl` TEXT, " +
                "`imageUrl` TEXT, " +
                "`category` TEXT, " +
                "`difficulty` TEXT, " +
                "`benefitsJson` TEXT, " +
                "PRIMARY KEY(`id`))");

            // Create yoga_days table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `yoga_days` (" +
                "`dayNumber` INTEGER NOT NULL, " +
                "`title` TEXT, " +
                "`description` TEXT, " +
                "`posesJson` TEXT, " +
                "`totalDuration` INTEGER NOT NULL, " +
                "`category` TEXT, " +
                "`difficulty` TEXT, " +
                "`focusArea` TEXT, " +
                "PRIMARY KEY(`dayNumber`))");            // Create progress table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `progress` (" +
                "`id` TEXT NOT NULL, " +
                "`userId` TEXT, " +
                "`dayNumber` INTEGER NOT NULL, " +
                "`isCompleted` INTEGER NOT NULL, " +
                "`completionTimestamp` INTEGER NOT NULL, " +
                "`duration` INTEGER NOT NULL, " +
                "`calories` INTEGER NOT NULL, " +
                "`averageConfidence` REAL NOT NULL, " +
                "`completedPosesJson` TEXT, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`dayNumber`) REFERENCES `yoga_days`(`dayNumber`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`userId`) REFERENCES `user_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");

            // Create daily_progress table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `daily_progress` (" +
                "`id` TEXT NOT NULL, " +
                "`userId` TEXT, " +
                "`dateTimestamp` INTEGER NOT NULL, " +
                "`totalDuration` INTEGER NOT NULL, " +
                "`totalCalories` INTEGER NOT NULL, " +
                "`sessionsCount` INTEGER NOT NULL, " +
                "`averageScore` REAL NOT NULL, " +
                "`yogaStylesJson` TEXT, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`userId`) REFERENCES `user_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");            // Create indices for faster foreign key lookups
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_dayNumber` ON `progress` (`dayNumber`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_userId` ON `progress` (`userId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_progress_userId` ON `daily_progress` (`userId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_progress_dateTimestamp` ON `daily_progress` (`dateTimestamp`)");
        }
    };
}
