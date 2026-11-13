package cl.ipvg.nutrivi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class PasosFragment extends Fragment {

    private TextView tvPasosHoy, tvPasosSemanal, tvPasosMensual;
    private LinearProgressIndicator progressPasosDetalle;
    private StepReceiver stepReceiver;
    private int pasosActuales = 4500;
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
        IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_UPDATE);
        ContextCompat.registerReceiver(requireContext(), stepReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(stepReceiver);
    }

    private void actualizarUI() {
        tvPasosHoy.setText(String.valueOf(pasosActuales));
        progressPasosDetalle.setProgress(pasosActuales, true);

        int semanal = NutriViPrefs.getWeeklyStepsAverage(requireContext(), pasosActuales);
        int mensual = NutriViPrefs.getMonthlyStepsAverage(requireContext(), pasosActuales);

        tvPasosSemanal.setText(semanal + " pasos diarios (Basado en tus registros)");
        tvPasosMensual.setText(mensual + " pasos diarios (Basado en tus registros)");
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
