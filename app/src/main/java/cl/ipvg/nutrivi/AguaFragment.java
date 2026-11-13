package cl.ipvg.nutrivi;

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

public class AguaFragment extends Fragment {

    private TextView tvAguaHoy, tvAguaSemanal, tvAguaMensual;
    private MaterialButton btnSumarAgua;
    private int vasosAgua = 3;
    private final int metaAgua = 8;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agua, container, false);

        tvAguaHoy = view.findViewById(R.id.tv_agua_hoy);
        tvAguaSemanal = view.findViewById(R.id.tv_agua_semanal);
        tvAguaMensual = view.findViewById(R.id.tv_agua_mensual);
        btnSumarAgua = view.findViewById(R.id.btn_sumar_agua);

        vasosAgua = NutriViPrefs.getWaterToday(requireContext());
        actualizarUI();

        btnSumarAgua.setOnClickListener(v -> {
            if (vasosAgua < metaAgua) {
                vasosAgua++;
                NutriViPrefs.setWaterToday(requireContext(), vasosAgua);
                actualizarUI();
                Toast.makeText(getContext(), "¡Bien hecho! Vaso registrado 💧", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "¡Meta de agua diaria alcanzada!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void actualizarUI() {
        tvAguaHoy.setText(vasosAgua + " / " + metaAgua);
        float sem = NutriViPrefs.getWeeklyWaterAverage(requireContext(), vasosAgua);
        float men = NutriViPrefs.getMonthlyWaterAverage(requireContext(), vasosAgua);
        tvAguaSemanal.setText(sem + " vasos diarios (Basado en tu actividad)");
        tvAguaMensual.setText(men + " vasos diarios (Basado en tu actividad)");
    }
}
