package cl.ipvg.nutrivi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public final class ReminderScheduler {

    private static final String PREFERENCES = "reminder_preferences";
    private static final String WATER_ENABLED = "water_enabled";
    private static final String MEALS_ENABLED = "meals_enabled";

    static final String ACTION_WATER = "cl.ipvg.nutrivi.REMINDER_WATER";
    static final String ACTION_MEAL = "cl.ipvg.nutrivi.REMINDER_MEAL";
    static final String EXTRA_MEAL_NAME = "meal_name";
    static final String EXTRA_REQUEST_CODE = "request_code";

    private static final int WATER_REQUEST_CODE = 100;
    private static final int BREAKFAST_REQUEST_CODE = 201;
    private static final int LUNCH_REQUEST_CODE = 202;
    private static final int DINNER_REQUEST_CODE = 203;

    private ReminderScheduler() {
    }

    public static boolean isWaterEnabled(Context context) {
        return preferences(context).getBoolean(WATER_ENABLED, true);
    }

    public static boolean areMealsEnabled(Context context) {
        return preferences(context).getBoolean(MEALS_ENABLED, true);
    }

    public static void setWaterEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(WATER_ENABLED, enabled).apply();
        if (enabled) {
            scheduleNextWaterReminder(context);
        } else {
            cancel(context, ACTION_WATER, WATER_REQUEST_CODE);
        }
    }

    public static void setMealsEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(MEALS_ENABLED, enabled).apply();
        if (enabled) {
            scheduleNextMealReminder(context, BREAKFAST_REQUEST_CODE, "desayuno", 9);
            scheduleNextMealReminder(context, LUNCH_REQUEST_CODE, "almuerzo", 14);
            scheduleNextMealReminder(context, DINNER_REQUEST_CODE, "cena", 20);
        } else {
            cancel(context, ACTION_MEAL, BREAKFAST_REQUEST_CODE);
            cancel(context, ACTION_MEAL, LUNCH_REQUEST_CODE);
            cancel(context, ACTION_MEAL, DINNER_REQUEST_CODE);
        }
    }

    public static void scheduleAll(Context context) {
        if (isWaterEnabled(context)) {
            scheduleNextWaterReminder(context);
        }
        if (areMealsEnabled(context)) {
            scheduleNextMealReminder(context, BREAKFAST_REQUEST_CODE, "desayuno", 9);
            scheduleNextMealReminder(context, LUNCH_REQUEST_CODE, "almuerzo", 14);
            scheduleNextMealReminder(context, DINNER_REQUEST_CODE, "cena", 20);
        }
    }

    static void scheduleNextWaterReminder(Context context) {
        if (!isWaterEnabled(context)) {
            return;
        }

        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        int[] hours = {9, 11, 13, 15, 17, 19, 21};
        for (int hour : hours) {
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, 0);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);
            if (next.after(now)) {
                schedule(context, ACTION_WATER, WATER_REQUEST_CODE, next.getTimeInMillis(), null);
                return;
            }
        }

        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, hours[0]);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        schedule(context, ACTION_WATER, WATER_REQUEST_CODE, next.getTimeInMillis(), null);
    }

    static void scheduleNextMealReminder(Context context, int requestCode, String mealName, int hour) {
        if (!areMealsEnabled(context)) {
            return;
        }

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent extras = new Intent();
        extras.putExtra(EXTRA_MEAL_NAME, mealName);
        extras.putExtra(EXTRA_REQUEST_CODE, requestCode);
        schedule(context, ACTION_MEAL, requestCode, next.getTimeInMillis(), extras);
    }

    private static void schedule(Context context, String action, int requestCode, long triggerAt, Intent extras) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            throw new IllegalStateException("El servicio de alarmas no está disponible");
        }

        PendingIntent pendingIntent = pendingIntent(context, action, requestCode, extras);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    private static void cancel(Context context, String action, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            PendingIntent pendingIntent = pendingIntent(context, action, requestCode, null);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static PendingIntent pendingIntent(Context context, String action, int requestCode, Intent extras) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(action);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
