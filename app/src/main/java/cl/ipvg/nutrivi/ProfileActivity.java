package cl.ipvg.nutrivi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etWeight, etGoalCalories, etGoalWater;
    private Button btnSave;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.et_profile_name);
        etWeight = findViewById(R.id.et_profile_weight);
        etGoalCalories = findViewById(R.id.et_goal_calories);
        etGoalWater = findViewById(R.id.et_goal_water);
        btnSave = findViewById(R.id.btn_save_profile);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            cargarDatosPerfil();
        } else {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSave.setOnClickListener(v -> guardarDatosPerfil());
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
                        if (document.contains("metaCalorias")) {
                            etGoalCalories.setText(String.valueOf(document.get("metaCalorias")));
                        }
                        if (document.contains("metaAgua")) {
                            etGoalWater.setText(String.valueOf(document.get("metaAgua")));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void guardarDatosPerfil() {
        String nombre = etName.getText().toString().trim();
        String pesoStr = etWeight.getText().toString().trim();
        String caloriasStr = etGoalCalories.getText().toString().trim();
        String aguaStr = etGoalWater.getText().toString().trim();

        if (nombre.isEmpty() || pesoStr.isEmpty() || caloriasStr.isEmpty() || aguaStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> perfilData = new HashMap<>();
        perfilData.put("nombre", nombre);
        perfilData.put("peso", Double.parseDouble(pesoStr));
        perfilData.put("metaCalorias", Integer.parseInt(caloriasStr));
        perfilData.put("metaAgua", Integer.parseInt(aguaStr));

        db.collection("usuarios").document(userId)
                .set(perfilData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}