package cl.ipvg.nutrivi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecetaAdapter recetaAdapter;
    private List<Receta> recetaList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewComunidad);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recetaList = new ArrayList<>();
        recetaAdapter = new RecetaAdapter(recetaList);
        recyclerView.setAdapter(recetaAdapter);

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
                        recetaList.add(receta);
                    }
                    recetaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar recetas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}