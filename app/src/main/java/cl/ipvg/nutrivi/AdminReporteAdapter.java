package cl.ipvg.nutrivi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminReporteAdapter extends RecyclerView.Adapter<AdminReporteAdapter.ReporteViewHolder> {

    private List<Reporte> reporteList;

    public AdminReporteAdapter(List<Reporte> reporteList) {
        this.reporteList = reporteList;
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        Reporte reporte = reporteList.get(position);
        holder.tvReceta.setText("Plato: " + reporte.getReceta());
        holder.tvMotivo.setText("Motivo: " + reporte.getMotivo());
        holder.tvAutor.setText("Creador por: " + reporte.getAutorReceta());
        holder.tvPor.setText("Reportado por: " + reporte.getReportadoPor());
    }

    @Override
    public int getItemCount() {
        return reporteList != null ? reporteList.size() : 0;
    }

    public static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView tvReceta, tvMotivo, tvAutor, tvPor;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceta = itemView.findViewById(R.id.tvReporteReceta);
            tvMotivo = itemView.findViewById(R.id.tvReporteMotivo);
            tvAutor = itemView.findViewById(R.id.tvReporteAutor);
            tvPor = itemView.findViewById(R.id.tvReportePor);
        }
    }
}
