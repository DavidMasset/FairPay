package es.ifp.fairpay.activities;

import android.content.Intent;
import android.os.Bundle;
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

import es.ifp.fairpay.R;
import es.ifp.fairpay.database.DatabaseConnection;

public class RegistroActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected EditText nombre, apellidos, email, telefono, wallet, password, password2;
    protected Button registro;
    protected CheckBox condiciones;
    protected DatabaseConnection databaseConnection;

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

        nombre = (EditText) findViewById(R.id.edit_nombre_registro);
        apellidos = (EditText) findViewById(R.id.edit_apellidos_registro);
        email = (EditText) findViewById(R.id.edit_email_registro);
        telefono = (EditText) findViewById(R.id.edit_telefono_registro);
        wallet = (EditText) findViewById(R.id.edit_wallet_registro);
        password = (EditText) findViewById(R.id.edit_password_registro);
        password2 = (EditText) findViewById(R.id.edit_password2_registro);
        registro = (Button) findViewById(R.id.button_registrarse_registro);
        condiciones = (CheckBox) findViewById(R.id.check_privacidad_registro);

        databaseConnection = new DatabaseConnection();

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Toast.makeText(RegistroActivity.this, "Rellene todos los campos. ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!passwordRegistro.equals(password2Registro)) {
                    Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden. ", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO: Encriptar la contraseña, no puede ser texto plano.
                String contrasenaHash = passwordRegistro;
                //TODO: Encriptar la clave privada.
                String clavePrivadaCifrada = " Texto pare evitar duplicidad " +walletRegistro;

                databaseConnection.registrarUsuario(nombreRegistro,apellidosRegistro,emailRegistro , contrasenaHash , telefonoRegistro , walletRegistro ,clavePrivadaCifrada );

                Toast.makeText(RegistroActivity.this, "Usuario registrado correctamente. ", Toast.LENGTH_SHORT).show();
                pasarPantalla = new Intent(RegistroActivity.this, LoadScreenActivity.class);
                pasarPantalla.putExtra("PANTALLA", "OkActivity");
                startActivity(pasarPantalla);
            }
        });
    }

}

