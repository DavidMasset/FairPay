package es.ifp.fairpay.ui.authenticator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.ifp.fairpay.R;
import es.ifp.fairpay.data.database.DatabaseConnection;

public class LoginActivity extends AppCompatActivity {

    protected Intent pasarPantalla;
    protected EditText email, password;
    protected Button login, registro;
    protected DatabaseConnection db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = (EditText) findViewById(R.id.edit_email_login);
        password = (EditText) findViewById(R.id.edit_password_login);
        login = (Button) findViewById(R.id.button_sesion_login);
        registro = (Button) findViewById(R.id.button_registro_login);
        db = new DatabaseConnection();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailLogin = email.getText().toString().trim();
                String passwordLogin = password.getText().toString().trim();
                if (emailLogin.isEmpty() || passwordLogin.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*Se pasan los métodos del listener (interfaz), emailLogin y passwordLogin*/
                db.loginUsuario(new DatabaseConnection.LoginListener() {
                    @Override
                    public void onLoginSuccess() {
                        /*Se ejecuta en el hilo principal las itnerfaces con los errores de la base de datos*/
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            pasarPantalla = new Intent(LoginActivity.this, LoadScreenActivity.class);
                            pasarPantalla.putExtra("PANTALLA", "InicioActivity");
                            startActivity(pasarPantalla);
                        });
                    }

                    @Override
                    public void onLoginFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }, emailLogin, passwordLogin);
            }
        });

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent para cambiar de Activity
                pasarPantalla = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(pasarPantalla);
            }
        });
    }
}
