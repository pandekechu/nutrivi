package cl.ipvg.nutrivi;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        if (requestCode == REQUEST_NOTIFICATIONS
                && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Activa las notificaciones para recibir tus recordatorios.", Toast.LENGTH_LONG).show();
        }
    }


    private void mostrarOpcionesDeRegistro() {

        Toast.makeText(this, "Abrir opciones de registro de comida", Toast.LENGTH_SHORT).show();
    }


    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}