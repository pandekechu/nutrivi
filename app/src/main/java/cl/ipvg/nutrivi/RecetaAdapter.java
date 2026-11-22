package cl.ipvg.nutrivi;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder> {

    private List<Receta> recetaList;

    public RecetaAdapter(List<Receta> recetaList) {
        this.recetaList = recetaList;
    }

    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = recetaList.get(position);
        holder.tvNombrePlato.setText(receta.getNombrePlato());
        holder.tvCalorias.setText(receta.getCalorias() + " kcal");
        holder.tvIngredientes.setText("Ingredientes: " + receta.getIngredientes());
        holder.tvAutor.setText("Por: " + receta.getAutor());

        holder.btnReportarReceta.setOnClickListener(v -> {
            Context context = v.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Reportar Receta");

            final EditText input = new EditText(context);
            input.setHint("Motivo del reporte (ej. información incorrecta)");
            builder.setView(input);

            builder.setPositiveButton("Enviar Reporte", (dialog, which) -> {
                String motivo = input.getText().toString().trim();
                if (motivo.isEmpty()) {
                    motivo = "Sin motivo especificado";
                }

                Map<String, Object> reporte = new HashMap<>();
                reporte.put("receta", receta.getNombrePlato());
                reporte.put("autorReceta", receta.getAutor());
                reporte.put("motivo", motivo);
                reporte.put("fecha", System.currentTimeMillis());

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                reporte.put("reportadoPor", currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "Anónimo");

                FirebaseFirestore.getInstance().collection("reportes_recetas")
                        .add(reporte)
                        .addOnSuccessListener(documentReference ->
                                Toast.makeText(context, "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
                        );
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return recetaList.size();
    }

    public static class RecetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePlato, tvCalorias, tvIngredientes, tvAutor;
        ImageButton btnReportarReceta;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePlato = itemView.findViewById(R.id.tvNombrePlato);
            tvCalorias = itemView.findViewById(R.id.tvCalorias);
            tvIngredientes = itemView.findViewById(R.id.tvIngredientes);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            btnReportarReceta = itemView.findViewById(R.id.btnReportarReceta);
        }
    }
}
