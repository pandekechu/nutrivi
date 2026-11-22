package cl.ipvg.nutrivi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
    private TextView tvEmail;
    private MaterialButton btnSave, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

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
        btnSave = view.findViewById(R.id.btn_save_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            if (tvEmail != null && currentUser.getEmail() != null) {
                tvEmail.setText(currentUser.getEmail());
            }
            cargarDatosPerfil();
        } else {
            Toast.makeText(getContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> guardarDatosPerfil());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
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
                        Toast.makeText(getContext(), "Perfil actualizado correctamente 🎉", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
