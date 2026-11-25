package cl.ipvg.nutrivi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText etName, etWeight, etGoalCalories, etGoalWater;
    private TextView tvEmail, tvBadgeAgua, tvBadgePasos, tvBadgeChef;
    private ImageView imgAvatar;
    private MaterialButton btnSave, btnLogout, btnAdminPanel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    private static final String ADMIN_EMAIL = "fernando.villanuevasoto9@gmail.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = view.findViewById(R.id.et_profile_name);
        etWeight = view.findViewById(R.id.et_profile_weight);
        etGoalCalories = view.findViewById(R.id.et_goal_calories);
        etGoalWater = view.findViewById(R.id.et_goal_water);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvBadgeAgua = view.findViewById(R.id.tv_badge_agua);
        tvBadgePasos = view.findViewById(R.id.tv_badge_pasos);
        tvBadgeChef = view.findViewById(R.id.tv_badge_chef);
        imgAvatar = view.findViewById(R.id.img_profile_avatar);
        btnSave = view.findViewById(R.id.btn_save_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAdminPanel = view.findViewById(R.id.btn_admin_panel);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            if (tvEmail != null && currentUser.getEmail() != null) {
                tvEmail.setText(currentUser.getEmail());
            }

            if (imgAvatar != null && currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(imgAvatar);
            }

            if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase(ADMIN_EMAIL)) {
                btnAdminPanel.setVisibility(View.VISIBLE);
            } else {
                btnAdminPanel.setVisibility(View.GONE);
            }

            cargarDatosPerfil();
            actualizarInsignias();
        } else {
            Toast.makeText(getContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> guardarDatosPerfil());

        btnAdminPanel.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AdminActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            requireContext().getSharedPreferences("nutrivi_user_data", Context.MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarInsignias();
    }

    private void actualizarInsignias() {
        if (!isAdded() || getContext() == null) return;

        int aguaHoy = NutriViPrefs.getWaterToday(requireContext());
        int metaAgua = NutriViPrefs.getWaterGoal(requireContext());

        if (tvBadgeAgua != null) {
            if (aguaHoy >= metaAgua && metaAgua > 0) {
                tvBadgeAgua.setText("🏆 Hidratación Top (Desbloqueado - Meta cumplida)");
                tvBadgeAgua.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold_accent));
            } else {
                tvBadgeAgua.setText("🔒 Hidratación Top (Bloqueado - " + aguaHoy + "/" + metaAgua + " vasos)");
                tvBadgeAgua.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("NutriAppPrefs", Context.MODE_PRIVATE);
        int pasosDetector = prefs.getInt("pasos_detector_acumulados", 0);
        if (tvBadgePasos != null) {
            if (pasosDetector > 0) {
                tvBadgePasos.setText("🏆 Caminante Estelar (Desbloqueado - " + pasosDetector + " pasos)");
                tvBadgePasos.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold_accent));
            } else {
                tvBadgePasos.setText("🔒 Caminante Estelar (Bloqueado - Registra tus primeros pasos)");
                tvBadgePasos.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && db != null) {
            db.collection("recetas")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (isAdded() && tvBadgeChef != null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                tvBadgeChef.setText("🏆 Chef NutriVi (Desbloqueado - Recetas publicadas)");
                                tvBadgeChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.gold_accent));
                            } else {
                                tvBadgeChef.setText("🔒 Chef NutriVi (Bloqueado - Publica tu primera receta)");
                                tvBadgeChef.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                            }
                        }
                    });
        }
    }

    private void cargarDatosPerfil() {
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        if (document.contains("nombre")) {
                            etName.setText(document.getString("nombre"));
                        }
                        if (document.contains("peso")) {
                            etWeight.setText(String.valueOf(document.get("peso")));
                        }
                        if (document.contains("meta_calorias")) {
                            etGoalCalories.setText(String.valueOf(document.get("meta_calorias")));
                        } else if (document.contains("metaCalorias")) {
                            etGoalCalories.setText(String.valueOf(document.get("metaCalorias")));
                        }
                        if (document.contains("meta_agua")) {
                            etGoalWater.setText(String.valueOf(document.get("meta_agua")));
                        } else if (document.contains("metaAgua")) {
                            etGoalWater.setText(String.valueOf(document.get("metaAgua")));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarDatosPerfil() {
        String nombre = etName.getText() != null ? etName.getText().toString().trim() : "";
        String pesoStr = etWeight.getText() != null ? etWeight.getText().toString().trim() : "";
        String caloriasStr = etGoalCalories.getText() != null ? etGoalCalories.getText().toString().trim() : "";
        String aguaStr = etGoalWater.getText() != null ? etGoalWater.getText().toString().trim() : "";

        if (nombre.isEmpty() || pesoStr.isEmpty() || caloriasStr.isEmpty() || aguaStr.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> perfilData = new HashMap<>();
        perfilData.put("nombre", nombre);
        perfilData.put("peso", Double.parseDouble(pesoStr));
        perfilData.put("meta_calorias", Integer.parseInt(caloriasStr));
        perfilData.put("meta_agua", Integer.parseInt(aguaStr));

        NutriViPrefs.setCalorieGoal(requireContext(), Integer.parseInt(caloriasStr));
        NutriViPrefs.setWaterGoal(requireContext(), Integer.parseInt(aguaStr));

        db.collection("usuarios").document(userId)
                .set(perfilData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        actualizarInsignias();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
