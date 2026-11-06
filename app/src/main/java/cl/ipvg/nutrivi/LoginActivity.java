package cl.ipvg.nutrivi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnAction, btnGoogleSignIn;
    private TextView tvToggleMode;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CredentialManager credentialManager;

    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase y CredentialManager
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        credentialManager = CredentialManager.create(this);

        // Vincular Vistas
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnAction = findViewById(R.id.btn_action);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        tvToggleMode = findViewById(R.id.tv_toggle_mode);

        // Eventos
        tvToggleMode.setOnClickListener(v -> alternarModo());
        btnAction.setOnClickListener(v -> procesarAutenticacionCorreo());
        btnGoogleSignIn.setOnClickListener(v -> iniciarSesionConGoogle());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            irAMainActivity();
        }
    }

    private void alternarModo() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            btnAction.setText("Iniciar Sesión");
            tvToggleMode.setText("¿No tienes cuenta? Regístrate aquí");
        } else {
            btnAction.setText("Registrarse");
            tvToggleMode.setText("¿Ya tienes cuenta? Inicia sesión");
        }
    }


    private void procesarAutenticacionCorreo() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAction.setEnabled(false);

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            irAMainActivity();
                        } else {
                            mostrarError("Error al iniciar sesión", task.getException());
                        }
                    });
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            crearPerfilUsuarioFirestore(mAuth.getCurrentUser().getUid(), email);
                        } else {
                            mostrarError("Error al registrar", task.getException());
                        }
                    });
        }
    }


    private void iniciarSesionConGoogle() {
        btnGoogleSignIn.setEnabled(false);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        manejarRespuestaGoogle(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) {
                            btnGoogleSignIn.setEnabled(true);
                        } else {
                            mostrarError("Error en el inicio de sesión con Google", e);
                        }
                    }
                }
        );
    }

    private void manejarRespuestaGoogle(GetCredentialResponse result) {
        if (result.getCredential() instanceof CustomCredential) {
            CustomCredential credential = (CustomCredential) result.getCredential();

            if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
                    AuthCredential authCredential = GoogleAuthProvider.getCredential(googleIdToken.getIdToken(), null);

                    mAuth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (task.getResult() != null &&
                                        task.getResult().getAdditionalUserInfo() != null &&
                                        task.getResult().getAdditionalUserInfo().isNewUser()) {
                                        crearPerfilUsuarioFirestore(user.getUid(), user.getEmail());
                                    } else {
                                        irAMainActivity();
                                    }
                                } else {
                                    mostrarError("Falló la autenticación en Firebase", task.getException());
                                }
                            });
                } catch (Exception e) {
                    mostrarError("Error procesando Token de Google", e);
                }
            } else {
                mostrarError("Tipo de credencial no soportado", null);
            }
        } else {
            mostrarError("Tipo de credencial no reconocido", null);
        }
    }


    private void crearPerfilUsuarioFirestore(String userId, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("fecha_registro", System.currentTimeMillis());
        user.put("meta_calorias", 2000);
        user.put("meta_agua", 8);

        db.collection("usuarios").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> irAMainActivity())
                .addOnFailureListener(e -> mostrarError("Error guardando perfil", e));
    }

    private void irAMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void mostrarError(String mensaje, Exception e) {
        btnAction.setEnabled(true);
        btnGoogleSignIn.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        if (e != null) {
            Log.e("AuthError", mensaje, e);
        }
    }
}