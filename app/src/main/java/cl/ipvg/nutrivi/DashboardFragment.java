package cl.ipvg.nutrivi;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private CircularProgressIndicator progressCalories;
    private TextView tvCaloriesCurrent, tvCaloriesGoal, tvGreeting;

    private TextView tvWaterCount;
    private MaterialButton btnAddWater;

    private TextView tvStepsCount, tvStepsGoal;
    private LinearProgressIndicator progressSteps;

    private ImageView btnProfileShortcut;
    private TextView tvRecetaNombre, tvRecetaCalorias, tvRecetaIngredientes, tvRecetaAutor;

    private StepReceiver stepReceiver;

    private int caloriasMeta = 2000;
    private int caloriasActuales = 0;

    private int metaAgua = 8;
    private int vasosAgua = 0;

    private int metaPasos = 10000;
    private int pasosActuales = 0;

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

        try {
            Intent serviceIntent = new Intent(requireContext(), StepCounterService.class);
            ContextCompat.startForegroundService(requireContext(), serviceIntent);
        } catch (Exception e) {
            Log.e("Dashboard", "No se pudo iniciar el servicio de podómetro", e);
        }

        stepReceiver = new StepReceiver();

        cargarMetasYDatos();
        cargarNombreUsuario();
        cargarRecetaDelDia();
        actualizarUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMetasYDatos();
        cargarNombreUsuario();
        cargarRecetaDelDia();
        actualizarUI();
        try {
            IntentFilter filter = new IntentFilter(StepCounterService.ACTION_STEP_UPDATE);
            ContextCompat.registerReceiver(requireContext(), stepReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception e) {
            Log.e("Dashboard", "Error registrando receiver", e);
        }
    }

    private void cargarMetasYDatos() {
        if (!isAdded() || getContext() == null) return;
        vasosAgua = NutriViPrefs.getWaterToday(requireContext());
        caloriasMeta = NutriViPrefs.getCalorieGoal(requireContext());
        metaAgua = NutriViPrefs.getWaterGoal(requireContext());
        metaPasos = NutriViPrefs.getStepsGoal(requireContext());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (isAdded() && getContext() != null && doc.exists()) {
                            if (doc.contains("meta_agua")) {
                                Long mAgua = doc.getLong("meta_agua");
                                if (mAgua != null) {
                                    metaAgua = mAgua.intValue();
                                    NutriViPrefs.setWaterGoal(requireContext(), metaAgua);
                                }
                            }
                            if (doc.contains("meta_calorias")) {
                                Long mCal = doc.getLong("meta_calorias");
                                if (mCal != null) {
                                    caloriasMeta = mCal.intValue();
                                    NutriViPrefs.setCalorieGoal(requireContext(), caloriasMeta);
                                }
                            }
                            actualizarUI();
                        }
                    });
        }
    }

    private void cargarNombreUsuario() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (btnProfileShortcut != null && currentUser.getPhotoUrl() != null && isAdded()) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .placeholder(android.R.drawable.ic_menu_info_details)
                        .into(btnProfileShortcut);
            }

            if (tvGreeting != null) {
                FirebaseFirestore.getInstance().collection("usuarios").document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (isAdded() && tvGreeting != null && documentSnapshot.exists() && documentSnapshot.contains("nombre")) {
                                String nombre = documentSnapshot.getString("nombre");
                                if (nombre != null && !nombre.isEmpty()) {
                                    tvGreeting.setText("Hola, " + nombre + " 👋");
                                }
                            } else if (isAdded() && tvGreeting != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                                tvGreeting.setText("Hola, " + currentUser.getDisplayName() + " 👋");
                            }
                        });
            }
        }
    }

    private void cargarRecetaDelDia() {
        FirebaseFirestore.getInstance().collection("recetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    List<Receta> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Receta r = doc.toObject(Receta.class);
                        lista.add(r);
                    }
                    if (lista.isEmpty()) {
                        lista.add(new Receta("1", "Ensalada César Saludable", "Lechuga romana, pollo, queso parmesano, yogur griego.", 320, "NutriChef", ""));
                        lista.add(new Receta("2", "Batido Proteico de Banano y Avena", "1 banano, 1 taza de avena, proteína en polvo.", 280, "FitnessLife", ""));
                        lista.add(new Receta("3", "Pechuga al Horno con Vegetales", "Pechuga de pollo, brócoli, zanahoria, aceite de oliva.", 380, "Ana Gómez", ""));
                    }

                    int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                    int index = dayOfYear % lista.size();
                    Receta seleccionada = lista.get(index);

                    if (tvRecetaNombre != null) {
                        tvRecetaNombre.setText(seleccionada.getNombrePlato());
                        tvRecetaCalorias.setText(seleccionada.getCalorias() + " kcal");
                        tvRecetaIngredientes.setText("Ingredientes: " + seleccionada.getIngredientes());
                        tvRecetaAutor.setText("Por: " + seleccionada.getAutor());
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && tvRecetaNombre != null) {
                        tvRecetaNombre.setText("Ensalada César Saludable");
                        tvRecetaCalorias.setText("320 kcal");
                        tvRecetaIngredientes.setText("Ingredientes: Lechuga, pollo, parmesano, yogur.");
                        tvRecetaAutor.setText("Por: NutriChef");
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (getContext() != null && stepReceiver != null) {
                requireContext().unregisterReceiver(stepReceiver);
            }
        } catch (Exception e) {
            Log.e("Dashboard", "Error desregistrando receiver", e);
        }
    }

    private void inicializarVistas(View view) {
        progressCalories = view.findViewById(R.id.progress_calories);
        tvCaloriesCurrent = view.findViewById(R.id.tv_calories_current);
        tvCaloriesGoal = view.findViewById(R.id.tv_calories_goal);
        tvGreeting = view.findViewById(R.id.tv_greeting);

        tvWaterCount = view.findViewById(R.id.tv_water_count);
        btnAddWater = view.findViewById(R.id.btn_add_water);
        btnProfileShortcut = view.findViewById(R.id.btn_profile_shortcut);

        tvStepsCount = view.findViewById(R.id.tv_steps_count);
        tvStepsGoal = view.findViewById(R.id.tv_steps_goal);
        progressSteps = view.findViewById(R.id.progress_steps);

        tvRecetaNombre = view.findViewById(R.id.tv_receta_dia_nombre);
        tvRecetaCalorias = view.findViewById(R.id.tv_receta_dia_calorias);
        tvRecetaIngredientes = view.findViewById(R.id.tv_receta_dia_ingredientes);
        tvRecetaAutor = view.findViewById(R.id.tv_receta_dia_autor);
    }

    private void configurarEventos() {
        if (btnProfileShortcut != null) {
            btnProfileShortcut.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                }
            });
        }

        if (btnAddWater != null) {
            btnAddWater.setOnClickListener(v -> {
                if (vasosAgua < metaAgua) {
                    vasosAgua++;
                    NutriViPrefs.setWaterToday(requireContext(), vasosAgua);
                    actualizarTarjetaAgua();
                    Toast.makeText(getContext(), "¡Bien hecho! Vaso registrado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Meta de agua diaria alcanzada!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void actualizarUI() {
        if (!isAdded()) return;
        if (tvCaloriesCurrent != null) tvCaloriesCurrent.setText(String.valueOf(caloriasActuales));
        if (tvCaloriesGoal != null) tvCaloriesGoal.setText("/ " + caloriasMeta + " kcal");
        if (progressCalories != null) {
            progressCalories.setMax(caloriasMeta);
            animarProgresoCircular(progressCalories, caloriasActuales);
        }

        actualizarTarjetaAgua();

        if (tvStepsCount != null) tvStepsCount.setText(String.valueOf(pasosActuales));
        if (tvStepsGoal != null) tvStepsGoal.setText("/ " + metaPasos);
        if (progressSteps != null) {
            progressSteps.setMax(metaPasos);
            progressSteps.setProgress(pasosActuales, true);
        }
    }

    private void actualizarTarjetaAgua() {
        if (tvWaterCount != null) {
            tvWaterCount.setText(vasosAgua + " / " + metaAgua);
        }
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

                if (isAdded()) {
                    if (tvStepsCount != null) tvStepsCount.setText(String.valueOf(pasosActuales));
                    if (progressSteps != null) progressSteps.setProgress(pasosActuales, true);
                }
            }
        }
    }
}
