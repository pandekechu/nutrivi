package cl.ipvg.nutrivi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private static final String CHANNEL_ID = "StepCounterChannel";
    public static final String ACTION_STEP_UPDATE = "cl.ipvg.nutriapp.STEP_UPDATE";
    public static final String EXTRA_STEPS = "pasos_del_dia";

    @Override
    public void onCreate() {
        super.onCreate();

        crearCanalNotificacion();
        iniciarServicioEnPrimerPlano();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor == null) {
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            }
        }

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int pasosDeHoy = 0;
        SharedPreferences prefs = getSharedPreferences("NutriAppPrefs", MODE_PRIVATE);

        String hoyFechaLocal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String ultimaFecha = prefs.getString("ultima_fecha_pasos", "");

        if (!hoyFechaLocal.equals(ultimaFecha)) {
            // Reinicio de medianoche a las 00:00 hora local
            int actual = event.sensor.getType() == Sensor.TYPE_STEP_COUNTER ? (int) event.values[0] : 0;
            prefs.edit()
                    .putString("ultima_fecha_pasos", hoyFechaLocal)
                    .putInt("pasos_iniciales_dia", actual)
                    .putInt("pasos_detector_acumulados", 0)
                    .apply();
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalPasosDispositivo = (int) event.values[0];
            int pasosIniciales = prefs.getInt("pasos_iniciales_dia", -1);

            if (pasosIniciales == -1) {
                pasosIniciales = totalPasosDispositivo;
                prefs.edit().putInt("pasos_iniciales_dia", pasosIniciales).apply();
            }

            pasosDeHoy = totalPasosDispositivo - pasosIniciales;
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            int detectorPasos = prefs.getInt("pasos_detector_acumulados", 0) + 1;
            prefs.edit().putInt("pasos_detector_acumulados", detectorPasos).apply();
            pasosDeHoy = detectorPasos;
        }

        if (pasosDeHoy < 0) pasosDeHoy = 0;

        Intent broadcastIntent = new Intent(ACTION_STEP_UPDATE);
        broadcastIntent.putExtra(EXTRA_STEPS, pasosDeHoy);
        sendBroadcast(broadcastIntent);

        actualizarNotificacion(pasosDeHoy);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void iniciarServicioEnPrimerPlano() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NutriApp Activo")
                .setContentText("Contando tus pasos diarios...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
            } else {
                startForeground(1, notification);
            }
        } catch (Exception e) {
            Log.e("StepService", "Error al iniciar servicio en primer plano", e);
        }
    }

    private void actualizarNotificacion(int pasos) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NutriApp Activo")
                .setContentText("Pasos de hoy: " + pasos)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();

        if (manager != null) {
            try {
                manager.notify(1, notification);
            } catch (Exception e) {
                Log.e("StepService", "Error notificando pasos", e);
            }
        }
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal de Podómetro NutriApp",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
