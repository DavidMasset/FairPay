package es.ifp.fairpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LoginActivity extends AppCompatActivity {

    protected Intent pasarPantalla;
    protected EditText email, password;
    protected Button login, registro;

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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent para cambiar de Activity
                pasarPantalla = new Intent(LoginActivity.this, LoadScreenActivity.class);
                pasarPantalla.putExtra("PANTALLA", "InicioActivity");
                startActivity(pasarPantalla);
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



    // PONER AQUÍ LA LÓGICA DE INICIO DE SESIÓN



}
