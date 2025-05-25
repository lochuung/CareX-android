package hcmute.edu.vn.loclinhvabao.carex.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.util.NotificationUtils;

public class YogaReminderWorker extends Worker {

    public YogaReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Get input data
            String reminderTime = getInputData().getString("reminderTime");
            String[] reminderDaysArray = getInputData().getStringArray("reminderDays");

            if (reminderTime == null || reminderDaysArray == null || reminderDaysArray.length == 0) {
                return Result.failure();
            }

            List<String> reminderDays = Arrays.asList(reminderDaysArray);

            // Check if today is a reminder day
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String today = getDayNameFromCalendar(dayOfWeek);

            if (reminderDays.contains(today)) {
                // Format the reminder time for display
                String formattedTime = formatTime(reminderTime);

                // Show the reminder notification with English messages
                NotificationUtils.showYogaReminder(
                        getApplicationContext(),
                        "Time for Yoga! 🧘‍♀️",
                        "Your " + formattedTime + " yoga session is ready to begin. Take a moment for yourself!"
                );
            }

            return Result.success();

        } catch (Exception e) {
            android.util.Log.e("YogaReminderWorker", "Error in doWork", e);
            return Result.failure();
        }
    }

    private String getDayNameFromCalendar(int dayOfWeek) {
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

    private String formatTime(String time) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            return time;
        }
    }
}