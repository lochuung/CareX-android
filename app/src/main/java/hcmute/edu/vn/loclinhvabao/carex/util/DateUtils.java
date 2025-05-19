package hcmute.edu.vn.loclinhvabao.carex.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());

    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }

    public static String getDayOfWeek(Date date) {
        if (date == null) return "";
        return DAY_FORMAT.format(date);
    }

    public static Date getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return truncateTime(calendar.getTime());
    }

    public static Date getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return truncateTime(calendar.getTime());
    }

    public static Date getStartOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return truncateTime(calendar.getTime());
    }

    public static Date truncateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static String getRelativeDate(Date date) {
        if (date == null) return "";

        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);

        if (isSameDay(today.getTime(), targetDate.getTime())) {
            return "Today";
        }

        today.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(today.getTime(), targetDate.getTime())) {
            return "Yesterday";
        }

        // Default to formatted date for older dates
        return formatDate(date);
    }
}