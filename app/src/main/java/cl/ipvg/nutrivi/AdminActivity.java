package cl.ipvg.nutrivi;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminReporteAdapter adapter;
    private List<Reporte> reporteList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerViewReportes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reporteList = new ArrayList<>();
        adapter = new AdminReporteAdapter(reporteList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        cargarReportes();
    }

    private void cargarReportes() {
        db.collection("reportes_recetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reporteList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reporte reporte = doc.toObject(Reporte.class);
                        reporte.setId(doc.getId());
                        reporteList.add(reporte);
                    }
                    adapter.notifyDataSetChanged();
                    if (reporteList.isEmpty()) {
                        Toast.makeText(this, "No hay reportes pendientes", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
