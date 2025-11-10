package es.ifp.fairpay.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.security.GeneralSecurityException;

import es.ifp.fairpay.R;
import es.ifp.fairpay.database.DatabaseConnection;
import es.ifp.fairpay.security.EncryptManager;

public class RegistroActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected EditText nombre, apellidos, email, telefono, wallet, password, password2;
    protected Button registro;
    protected CheckBox condiciones;
    protected DatabaseConnection databaseConnection;
    private EncryptManager encryptManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nombre = findViewById(R.id.edit_nombre_registro);
        apellidos = findViewById(R.id.edit_apellidos_registro);
        email = findViewById(R.id.edit_email_registro);
        telefono = findViewById(R.id.edit_telefono_registro);
        wallet = findViewById(R.id.edit_wallet_registro);
        password = findViewById(R.id.edit_password_registro);
        password2 = findViewById(R.id.edit_password2_registro);
        registro = findViewById(R.id.button_registrarse_registro);
        condiciones = findViewById(R.id.check_privacidad_registro);

        databaseConnection = new DatabaseConnection();

        // Inicializamos el EncryptManager
        try {
            encryptManager = new EncryptManager(getApplicationContext());
        } catch (GeneralSecurityException | IOException e) {
            Log.e("FairPayCrypto", "Error crítico al inicializar EncryptManager", e);
            Toast.makeText(this, "Error de seguridad irrecuperable. La aplicación no puede continuar.", Toast.LENGTH_LONG).show();
            // En una app real, aquí se debería deshabilitar la funcionalidad o cerrar la app.
            registro.setEnabled(false);
        }

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (encryptManager == null) {
                    Toast.makeText(RegistroActivity.this, "Error de seguridad. No se puede registrar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!condiciones.isChecked()) {
                    Toast.makeText(RegistroActivity.this, R.string.toast_terminos, Toast.LENGTH_SHORT).show();
                    return;
                }

                String nombreRegistro = nombre.getText().toString().trim();
                String apellidosRegistro = apellidos.getText().toString().trim();
                String emailRegistro = email.getText().toString().trim();
                String telefonoRegistro = telefono.getText().toString().trim();
                String walletRegistro = wallet.getText().toString().trim();
                String passwordRegistro = password.getText().toString().trim();
                String password2Registro = password2.getText().toString().trim();

                if (nombreRegistro.isEmpty() || apellidosRegistro.isEmpty() || emailRegistro.isEmpty() || telefonoRegistro.isEmpty() || walletRegistro.isEmpty() || passwordRegistro.isEmpty() || password2Registro.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Rellene todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!passwordRegistro.equals(password2Registro)) {
                    Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    /* gensalt() genera un "salt" aleatorio para cada contraseña.*/
                    String contrasenaHash = BCrypt.hashpw(passwordRegistro, BCrypt.gensalt());

                    /*Cifrar la clave privada con EncryptManager
                    * Se crea un alias único para el archivo cifrado. Usamos el email para identificarlo.*/
                    String privateKeyReal = "pk_placeholder_" + System.currentTimeMillis();
                    String privateKeyAlias = "pk_" + emailRegistro.replaceAll("[^a-zA-Z0-9]", "_");

                    encryptManager.encryptAndSave(privateKeyAlias, privateKeyReal);

                    databaseConnection.registrarUsuario(new DatabaseConnection.RegistroListener() {
                        @Override
                        public void onRegistroSuccess() {
                            runOnUiThread(()->{
                                Toast.makeText(RegistroActivity.this, "Usuario registrado correctamente.", Toast.LENGTH_SHORT).show();
                                pasarPantalla = new Intent(RegistroActivity.this, LoadScreenActivity.class);
                                pasarPantalla.putExtra("PANTALLA", "OkActivity");
                                startActivity(pasarPantalla);
                            });
                        }

                        @Override
                        public void onRegistroFailure(String error) {
                            runOnUiThread(()->{
                                Toast.makeText(RegistroActivity.this, error, Toast.LENGTH_SHORT).show();
                            });

                        }
                    }, nombreRegistro, apellidosRegistro, emailRegistro, contrasenaHash, telefonoRegistro, walletRegistro, privateKeyAlias);

                } catch (GeneralSecurityException | IOException e) {
                    Log.e("FairPayCrypto", "Error durante el proceso de cifrado en el registro.", e);
                    Toast.makeText(RegistroActivity.this, "Error de seguridad al guardar los datos.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
