package hcmute.edu.vn.loclinhvabao.carex.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.main.MainActivity;
import hcmute.edu.vn.loclinhvabao.carex.worker.YogaReminderWorker;

public class NotificationUtils {
    public static final String CHANNEL_ID = "yoga_reminders";
    public static final int NOTIFICATION_ID = 1001;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Yoga Reminders";
            String description = "Reminders for your scheduled yoga sessions";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showYogaReminder(Context context, String title, String message) {
        // Create an intent for when the notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_yoga)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification with permission check
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            // Nếu không có quyền, bạn có thể xử lý ở đây (ví dụ: ghi log)
        } else {
            // For Android 12 and below, no runtime permission needed
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void scheduleReminders(Context context, String reminderTime, List<String> reminderDays) {
        // Cancel existing reminders
        WorkManager.getInstance(context).cancelUniqueWork("YOGA_REMINDERS");

        if (reminderDays.isEmpty()) return;

        // Get hour and minute from reminderTime (format: "HH:mm")
        String[] timeParts = reminderTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // Calculate initial delay
        long initialDelay = calculateInitialDelay(hour, minute, reminderDays);

        // Prepare data for worker
        Data inputData = new Data.Builder()
                .putString("reminderTime", reminderTime)
                .putStringArray("reminderDays", reminderDays.toArray(new String[0]))
                .build();

        // Create and schedule the work request
        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                YogaReminderWorker.class,
                24, TimeUnit.HOURS) // Daily check
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "YOGA_REMINDERS",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }

    private static long calculateInitialDelay(int hour, int minute, List<String> reminderDays) {
        Calendar now = Calendar.getInstance();
        Calendar scheduledTime = Calendar.getInstance();

        // Set the scheduled time
        scheduledTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduledTime.set(Calendar.MINUTE, minute);
        scheduledTime.set(Calendar.SECOND, 0);
        scheduledTime.set(Calendar.MILLISECOND, 0);

        // If the scheduled time has already passed today, set it for tomorrow
        if (scheduledTime.before(now)) {
            scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Find the next scheduled day
        boolean foundNextDay = false;
        int daysToAdd = 0;
        int maxDaysToCheck = 7; // Check a week at most

        while (!foundNextDay && daysToAdd < maxDaysToCheck) {
            int dayOfWeek = scheduledTime.get(Calendar.DAY_OF_WEEK);
            String dayName = getDayNameFromCalendar(dayOfWeek);

            if (reminderDays.contains(dayName)) {
                foundNextDay = true;
            } else {
                scheduledTime.add(Calendar.DAY_OF_YEAR, 1);
                daysToAdd++;
            }
        }

        return scheduledTime.getTimeInMillis() - now.getTimeInMillis();
    }

    private static String getDayNameFromCalendar(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            case Calendar.SATURDAY: return "SATURDAY";
            case Calendar.SUNDAY: return "SUNDAY";
            default: return "";
        }
    }
}