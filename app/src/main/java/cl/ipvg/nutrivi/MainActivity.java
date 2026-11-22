package cl.ipvg.nutrivi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATIONS = 1001;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReminderScheduler.scheduleAll(this);
        solicitarPermisoNotificaciones();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddCalories = findViewById(R.id.fab_add_calories);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                cargarFragment(new DashboardFragment());
                return true;
            } else if (itemId == R.id.nav_comunidad) {
                cargarFragment(new CommunityFragment());
                return true;
            } else if (itemId == R.id.nav_pasos) {
                cargarFragment(new PasosFragment());
                return true;
            } else if (itemId == R.id.nav_agua) {
                cargarFragment(new AguaFragment());
                return true;
            }
            return false;
        });

        fabAddCalories.setOnClickListener(view -> mostrarOpcionesDeRegistro());

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_inicio);
        }

        verificarAdministrador();
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ReminderScheduler.scheduleAll(this);
            } else {
                Toast.makeText(this, "Activa las notificaciones para recibir tus recordatorios.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void mostrarOpcionesDeRegistro() {
        Toast.makeText(this, "Abrir opciones de registro de comida", Toast.LENGTH_SHORT).show();
        // Si quieres que este botón abra directamente tu pantalla de cámara, descomenta la siguiente línea:
        // startActivity(new Intent(MainActivity.this, CameraActivity.class));
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void verificarAdministrador() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && "admin@nutrivi.cl".equalsIgnoreCase(user.getEmail())) {
            new AlertDialog.Builder(this)
                    .setTitle("Panel de Administrador 🛡️")
                    .setMessage("Has iniciado sesión como Administrador. ¿Deseas ver los reportes de recetas y fallos de la app?")
                    .setPositiveButton("Ver Reportes y Fallos", (dialog, which) -> mostrarReportesAdmin())
                    .setNegativeButton("Cerrar", null)
                    .show();
        }
    }

    private void mostrarReportesAdmin() {
        FirebaseFirestore.getInstance().collection("reportes_recetas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder sb = new StringBuilder("--- REPORTE DE RECETAS ---\n\n");
                    if (queryDocumentSnapshots.isEmpty()) {
                        sb.append("No hay reportes de recetas registrados.\n");
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            sb.append("• Receta: ").append(doc.getString("receta"))
                              .append("\n  Motivo: ").append(doc.getString("motivo"))
                              .append("\n  Reportado por: ").append(doc.getString("reportadoPor"))
                              .append("\n\n");
                        }
                    }

                    FirebaseFirestore.getInstance().collection("fallos_app")
                            .get()
                            .addOnSuccessListener(failSnapshots -> {
                                sb.append("\n--- FALLOS DE LA APP ---\n\n");
                                if (failSnapshots.isEmpty()) {
                                    sb.append("No hay fallos de la app registrados.");
                                } else {
                                    for (QueryDocumentSnapshot doc : failSnapshots) {
                                        sb.append("• Error: ").append(doc.getString("mensaje"))
                                          .append("\n  Detalle: ").append(doc.getString("detalle"))
                                          .append("\n\n");
                                    }
                                }

                                new AlertDialog.Builder(this)
                                        .setTitle("Panel de Administrador")
                                        .setMessage(sb.toString())
                                        .setPositiveButton("OK", null)
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                new AlertDialog.Builder(this)
                                        .setTitle("Panel de Administrador")
                                        .setMessage(sb.toString() + "\n\n(No se pudieron cargar los fallos)")
                                        .setPositiveButton("OK", null)
                                        .show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
