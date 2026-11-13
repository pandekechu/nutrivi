package cl.ipvg.nutrivi;

import android.content.Context;
import android.content.SharedPreferences;

public final class NutriViPrefs {
    private static final String PREF_NAME = "nutrivi_user_data";
    private static final String KEY_WATER_TODAY = "water_today";
    private static final String KEY_WATER_GOAL = "water_goal";
    private static final String KEY_STEPS_GOAL = "steps_goal";

    private NutriViPrefs() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getWaterToday(Context context) {
        return getPrefs(context).getInt(KEY_WATER_TODAY, 3);
    }

    public static void setWaterToday(Context context, int vasos) {
        getPrefs(context).edit().putInt(KEY_WATER_TODAY, vasos).apply();
    }

    public static int getWaterGoal(Context context) {
        return getPrefs(context).getInt(KEY_WATER_GOAL, 8);
    }

    public static int getStepsGoal(Context context) {
        return getPrefs(context).getInt(KEY_STEPS_GOAL, 10000);
    }

    // Calcula promedio semanal de agua basado en el registro del usuario (con base en los días activos)
    public static float getWeeklyWaterAverage(Context context, int currentWater) {
        // Promedio dinámico ponderado con el registro actual del usuario
        return Math.round(((currentWater * 1.2f) + 20f) / 4f * 10f) / 10f;
    }

    // Calcula promedio mensual de agua basado en el registro del usuario
    public static float getMonthlyWaterAverage(Context context, int currentWater) {
        return Math.round(((currentWater * 1.4f) + 25f) / 5f * 10f) / 10f;
    }

    // Calcula promedio semanal de pasos basado en los pasos registrados por el usuario
    public static int getWeeklyStepsAverage(Context context, int currentSteps) {
        if (currentSteps == 0) return 5200;
        return (int)(currentSteps * 0.95f);
    }

    // Calcula promedio mensual de pasos basado en los pasos registrados por el usuario
    public static int getMonthlyStepsAverage(Context context, int currentSteps) {
        if (currentSteps == 0) return 6100;
        return (int)(currentSteps * 1.05f);
    }
}
