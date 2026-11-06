package cl.ipvg.nutrivi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
    }

    @Override
    public int getItemCount() {
        return recetaList.size();
    }

    public static class RecetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePlato, tvCalorias, tvIngredientes, tvAutor;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePlato = itemView.findViewById(R.id.tvNombrePlato);
            tvCalorias = itemView.findViewById(R.id.tvCalorias);
            tvIngredientes = itemView.findViewById(R.id.tvIngredientes);
            tvAutor = itemView.findViewById(R.id.tvAutor);
        }
    }
}