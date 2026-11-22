package cl.ipvg.nutrivi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecetaAdapter recetaAdapter;
    private List<Receta> recetaList;
    private FirebaseFirestore db;
    private MaterialButton btnAgregarRecetaTop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewComunidad);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recetaList = new ArrayList<>();
        recetaAdapter = new RecetaAdapter(recetaList, this::mostrarDialogoEditarReceta);
        recyclerView.setAdapter(recetaAdapter);

        btnAgregarRecetaTop = view.findViewById(R.id.btn_agregar_receta_top);
        btnAgregarRecetaTop.setOnClickListener(v -> mostrarDialogoAgregarReceta());

        db = FirebaseFirestore.getInstance();
        cargarRecetasDesdeFirestore();

        return view;
    }

    private void cargarRecetasDesdeFirestore() {
        db.collection("recetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recetaList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Receta receta = document.toObject(Receta.class);
                        receta.setId(document.getId());
                        if (document.contains("userId")) {
                            receta.setUserId(document.getString("userId"));
                        }
                        recetaList.add(receta);
                    }

                    if (recetaList.isEmpty()) {
                        cargarRecetasEjemplo();
                    } else {
                        recetaAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    cargarRecetasEjemplo();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Mostrando recetas de ejemplo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarRecetasEjemplo() {
        recetaList.clear();
        recetaList.add(new Receta("1", "Ensalada César Saludable", "Lechuga romana, pollo a la plancha, queso parmesano, yogur griego, crotones integrales.", 320, "NutriChef", ""));
        recetaList.add(new Receta("2", "Batido Proteico de Banano y Avena", "1 banano, 1 taza de avena, 1 medida de proteína en polvo, leche de almendras.", 280, "FitnessLife", ""));
        recetaList.add(new Receta("3", "Pechuga al Horno con Vegetales", "Pechuga de pollo, brócoli, zanahoria, aceite de oliva, especias al gusto.", 380, "Ana Gómez", ""));
        recetaAdapter.notifyDataSetChanged();
    }

    private void mostrarDialogoAgregarReceta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_receta, null);
        builder.setView(dialogView);

        EditText etPlato = dialogView.findViewById(R.id.etNombrePlato);
        EditText etIngredientes = dialogView.findViewById(R.id.etIngredientesReceta);
        EditText etCalorias = dialogView.findViewById(R.id.etCaloriasReceta);

        builder.setTitle("Publicar Nueva Receta")
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String plato = etPlato.getText().toString().trim();
                    String ingredientes = etIngredientes.getText().toString().trim();
                    String caloriasStr = etCalorias.getText().toString().trim();

                    if (plato.isEmpty() || ingredientes.isEmpty() || caloriasStr.isEmpty()) {
                        Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int calorias = Integer.parseInt(caloriasStr);

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String autor = "Anónimo";
                    String userId = "";
                    if (currentUser != null) {
                        userId = currentUser.getUid();
                        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                            autor = currentUser.getDisplayName();
                        } else if (currentUser.getEmail() != null) {
                            String email = currentUser.getEmail();
                            autor = email.contains("@") ? email.split("@")[0] : email;
                        }
                    }

                    guardarRecetaFirestore(plato, ingredientes, calorias, autor, userId);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void mostrarDialogoEditarReceta(Receta receta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_receta, null);
        builder.setView(dialogView);

        EditText etPlato = dialogView.findViewById(R.id.etNombrePlato);
        EditText etIngredientes = dialogView.findViewById(R.id.etIngredientesReceta);
        EditText etCalorias = dialogView.findViewById(R.id.etCaloriasReceta);

        etPlato.setText(receta.getNombrePlato());
        etIngredientes.setText(receta.getIngredientes());
        etCalorias.setText(String.valueOf(receta.getCalorias()));

        builder.setTitle("Editar Receta")
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String plato = etPlato.getText().toString().trim();
                    String ingredientes = etIngredientes.getText().toString().trim();
                    String caloriasStr = etCalorias.getText().toString().trim();

                    if (plato.isEmpty() || ingredientes.isEmpty() || caloriasStr.isEmpty()) {
                        Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int calorias = Integer.parseInt(caloriasStr);
                    actualizarRecetaFirestore(receta.getId(), plato, ingredientes, calorias);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void guardarRecetaFirestore(String plato, String ingredientes, int calorias, String autor, String userId) {
        Map<String, Object> recetaMap = new HashMap<>();
        recetaMap.put("nombrePlato", plato);
        recetaMap.put("ingredientes", ingredientes);
        recetaMap.put("calorias", calorias);
        recetaMap.put("autor", autor);
        recetaMap.put("userId", userId);

        db.collection("recetas")
                .add(recetaMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "¡Receta publicada con éxito! 🎉", Toast.LENGTH_SHORT).show();
                    cargarRecetasDesdeFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al publicar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void actualizarRecetaFirestore(String recetaId, String plato, String ingredientes, int calorias) {
        if (recetaId == null) {
            Toast.makeText(getContext(), "ID de receta inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> recetaMap = new HashMap<>();
        recetaMap.put("nombrePlato", plato);
        recetaMap.put("ingredientes", ingredientes);
        recetaMap.put("calorias", calorias);

        db.collection("recetas").document(recetaId)
                .update(recetaMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "¡Receta actualizada con éxito! ✏️", Toast.LENGTH_SHORT).show();
                    cargarRecetasDesdeFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
