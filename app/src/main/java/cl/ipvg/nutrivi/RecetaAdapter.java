package cl.ipvg.nutrivi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder> {

    private List<Receta> recetaList;
    private OnEditClickListener editClickListener;

    public interface OnEditClickListener {
        void onEditClick(Receta receta);
    }

    public RecetaAdapter(List<Receta> recetaList, OnEditClickListener listener) {
        this.recetaList = recetaList;
        this.editClickListener = listener;
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean esAutor = false;
        if (currentUser != null) {
            String currentUid = currentUser.getUid();
            String currentEmail = currentUser.getEmail();
            String currentName = currentEmail != null && currentEmail.contains("@") ? currentEmail.split("@")[0] : "";

            if (receta.getUserId() != null && receta.getUserId().equals(currentUid)) {
                esAutor = true;
            } else if (receta.getAutor() != null && (receta.getAutor().equalsIgnoreCase(currentName) || receta.getAutor().equalsIgnoreCase(currentEmail))) {
                esAutor = true;
            }
        }

        if (esAutor) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> {
                if (editClickListener != null) {
                    editClickListener.onEditClick(receta);
                }
            });
        } else {
            holder.btnEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recetaList.size();
    }

    public static class RecetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePlato, tvCalorias, tvIngredientes, tvAutor;
        ImageButton btnEdit;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePlato = itemView.findViewById(R.id.tvNombrePlato);
            tvCalorias = itemView.findViewById(R.id.tvCalorias);
            tvIngredientes = itemView.findViewById(R.id.tvIngredientes);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            btnEdit = itemView.findViewById(R.id.btn_edit_receta);
        }
    }
}
