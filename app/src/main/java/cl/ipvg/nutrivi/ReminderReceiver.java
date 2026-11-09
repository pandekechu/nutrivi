package cl.ipvg.nutrivi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "nutrivi_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            ReminderScheduler.scheduleAll(context);
            return;
        }

        int notificationId;
        String title;
        String message;

        if (ReminderScheduler.ACTION_WATER.equals(action)) {
            if (ReminderScheduler.isWaterEnabled(context)) {
                notificationId = 100;
                title = "Hora de hidratarte";
                message = "Toma un vaso de agua y registra tu progreso.";
                mostrarNotificacion(context, notificationId, title, message);
            }
            ReminderScheduler.scheduleNextWaterReminder(context);
            return;
        }

        if (ReminderScheduler.ACTION_MEAL.equals(action)
                && ReminderScheduler.areMealsEnabled(context)) {
            String mealName = intent.getStringExtra(ReminderScheduler.EXTRA_MEAL_NAME);
            notificationId = intent.getIntExtra(ReminderScheduler.EXTRA_REQUEST_CODE, 201);
            title = "Registra tu comida";
            message = "No olvides registrar tu " + mealName + " en Nutrivi.";
            mostrarNotificacion(context, notificationId, title, message);

            if (notificationId == 201) {
                ReminderScheduler.scheduleNextMealReminder(context, 201, "desayuno", 9);
            } else if (notificationId == 202) {
                ReminderScheduler.scheduleNextMealReminder(context, 202, "almuerzo", 14);
            } else if (notificationId == 203) {
                ReminderScheduler.scheduleNextMealReminder(context, 203, "cena", 20);
            }
        }
    }

    private void mostrarNotificacion(Context context, int notificationId, String title, String message) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (!notificationManager.areNotificationsEnabled()) {
            return;
        }

        crearCanal(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(notificationId, notification.build());
    }

    private void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Nutrivi",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
