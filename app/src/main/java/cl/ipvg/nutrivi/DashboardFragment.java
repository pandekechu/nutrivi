package cl.ipvg.nutrivi;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class DashboardFragment extends Fragment {

    private CircularProgressIndicator progressCalories;
    private TextView tvCaloriesCurrent, tvCaloriesGoal;

    private TextView tvWaterCount;
    private MaterialButton btnAddWater;

    private SwitchMaterial switchWaterReminder, switchMealReminder;

    private TextView tvStepsCount, tvStepsGoal;
    private LinearProgressIndicator progressSteps;

    private StepReceiver stepReceiver;

    private int caloriasMeta = 2000;
    private int caloriasActuales = 1200;

    private int metaAgua = 8;
    private int vasosAgua = 3;

    private int metaPasos = 10000;
    private int pasosActuales = 4500;

    public DashboardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboardfragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarVistas(view);
        configurarEventos();

        Intent serviceIntent = new Intent(requireContext(), StepCounterService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);

        stepReceiver = new StepReceiver();

        actualizarUI();
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

    private void inicializarVistas(View view) {
        progressCalories = view.findViewById(R.id.progress_calories);
        tvCaloriesCurrent = view.findViewById(R.id.tv_calories_current);
        tvCaloriesGoal = view.findViewById(R.id.tv_calories_goal);

        tvWaterCount = view.findViewById(R.id.tv_water_count);
        btnAddWater = view.findViewById(R.id.btn_add_water);
        switchWaterReminder = view.findViewById(R.id.switch_water_reminder);
        switchMealReminder = view.findViewById(R.id.switch_meal_reminder);

        tvStepsCount = view.findViewById(R.id.tv_steps_count);
        tvStepsGoal = view.findViewById(R.id.tv_steps_goal);
        progressSteps = view.findViewById(R.id.progress_steps);
    }

    private void configurarEventos() {
        switchWaterReminder.setChecked(ReminderScheduler.isWaterEnabled(requireContext()));
        switchWaterReminder.setOnCheckedChangeListener((button, enabled) -> {
            ReminderScheduler.setWaterEnabled(requireContext(), enabled);
        });

        switchMealReminder.setChecked(ReminderScheduler.areMealsEnabled(requireContext()));
        switchMealReminder.setOnCheckedChangeListener((button, enabled) -> {
            ReminderScheduler.setMealsEnabled(requireContext(), enabled);
        });

        btnAddWater.setOnClickListener(v -> {
            if (vasosAgua < metaAgua) {
                vasosAgua++;
                actualizarTarjetaAgua();
                Toast.makeText(getContext(), "¡Bien hecho! Mantente hidratado ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Meta de agua diaria alcanzada!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUI() {
        tvCaloriesCurrent.setText(String.valueOf(caloriasActuales));
        tvCaloriesGoal.setText("/ " + caloriasMeta + " kcal");
        progressCalories.setMax(caloriasMeta);
        animarProgresoCircular(progressCalories, caloriasActuales);

        actualizarTarjetaAgua();

        tvStepsCount.setText(String.valueOf(pasosActuales));
        tvStepsGoal.setText("/ " + metaPasos);
        progressSteps.setMax(metaPasos);
        progressSteps.setProgress(pasosActuales, true);
    }

    private void actualizarTarjetaAgua() {
        tvWaterCount.setText(vasosAgua + " / " + metaAgua);
    }

    private void animarProgresoCircular(CircularProgressIndicator progressBar, int progreso) {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, progreso);
        animation.setDuration(1200);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private class StepReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_STEP_UPDATE.equals(intent.getAction())) {
                pasosActuales = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0);

                tvStepsCount.setText(String.valueOf(pasosActuales));
                progressSteps.setProgress(pasosActuales, true);
            }
        }
    }
}