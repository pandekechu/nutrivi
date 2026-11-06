package cl.ipvg.nutrivi;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddCalories = findViewById(R.id.fab_add_calories);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Cargar HomeFragment
                return true;
            } else if (itemId == R.id.nav_community) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CommunityFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Cargar ProfileFragment
                return true;
            }
            return false;
        });


        fabAddCalories.setOnClickListener(view -> mostrarOpcionesDeRegistro());


        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
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