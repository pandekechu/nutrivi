package cl.ipvg.nutrivi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class PasosFragment extends Fragment implements SensorEventListener {

    private TextView tvPasosHoy, tvPasosSemanal, tvPasosMensual;
    private LinearProgressIndicator progressPasosDetalle;
    private StepReceiver stepReceiver;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int pasosActuales = 0;
    private final int metaPasos = 10000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pasos, container, false);

        tvPasosHoy = view.findViewById(R.id.tv_pasos_hoy);
        tvPasosSemanal = view.findViewById(R.id.tv_pasos_semanal);
        tvPasosMensual = view.findViewById(R.id.tv_pasos_mensual);
        progressPasosDetalle = view.findViewById(R.id.progress_pasos_detalle);
        progressPasosDetalle.setMax(metaPasos);

        stepReceiver = new StepReceiver();
        actualizarUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_UPDATE);
            ContextCompat.registerReceiver(requireContext(), stepReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

            sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                if (stepSensor == null) {
                    stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                }
                if (stepSensor != null) {
                    sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
                }
            }
        } catch (Exception e) {
            Log.e("PasosFragment", "Error registrando sensor en primer plano", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (stepReceiver != null && getContext() != null) {
                requireContext().unregisterReceiver(stepReceiver);
            }
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            Log.e("PasosFragment", "Error desregistrando sensor", e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            pasosActuales++;
            SharedPreferences prefs = requireContext().getSharedPreferences("NutriAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("pasos_detector_acumulados", pasosActuales).apply();
            actualizarUI();
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int total = (int) event.values[0];
            SharedPreferences prefs = requireContext().getSharedPreferences("NutriAppPrefs", Context.MODE_PRIVATE);
            int inicial = prefs.getInt("pasos_iniciales_dia", -1);
            if (inicial == -1) {
                inicial = total;
                prefs.edit().putInt("pasos_iniciales_dia", inicial).apply();
            }
            pasosActuales = total - inicial;
            if (pasosActuales < 0) pasosActuales = 0;
            actualizarUI();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void actualizarUI() {
        if (!isAdded()) return;
        if (tvPasosHoy != null) tvPasosHoy.setText(String.valueOf(pasosActuales));
        if (progressPasosDetalle != null) progressPasosDetalle.setProgress(pasosActuales, true);

        if (getContext() != null) {
            int semanal = NutriViPrefs.getWeeklyStepsAverage(requireContext(), pasosActuales);
            int mensual = NutriViPrefs.getMonthlyStepsAverage(requireContext(), pasosActuales);

            if (tvPasosSemanal != null) tvPasosSemanal.setText(semanal + " pasos diarios (Basado en tus registros)");
            if (tvPasosMensual != null) tvPasosMensual.setText(mensual + " pasos diarios (Basado en tus registros)");
        }
    }

    private class StepReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_STEP_UPDATE.equals(intent.getAction())) {
                pasosActuales = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0);
                actualizarUI();
            }
        }
    }
}
