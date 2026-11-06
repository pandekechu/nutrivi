package cl.ipvg.nutrivi;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            int totalPasosDispositivo = (int) event.values[0];


            SharedPreferences prefs = getSharedPreferences("NutriAppPrefs", MODE_PRIVATE);
            int pasosIniciales = prefs.getInt("pasos_iniciales_dia", -1);


            if (pasosIniciales == -1) {
                pasosIniciales = totalPasosDispositivo;
                prefs.edit().putInt("pasos_iniciales_dia", pasosIniciales).apply();
            }


            int pasosDeHoy = totalPasosDispositivo - pasosIniciales;


            Intent broadcastIntent = new Intent(ACTION_STEP_UPDATE);
            broadcastIntent.putExtra(EXTRA_STEPS, pasosDeHoy);
            sendBroadcast(broadcastIntent);


            actualizarNotificacion(pasosDeHoy);
        }
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

        startForeground(1, notification);
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

        manager.notify(1, notification);
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