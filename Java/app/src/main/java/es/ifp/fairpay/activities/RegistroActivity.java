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

public class RegistroActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected EditText nombre, apellidos, email, telefono, wallet, password, password2;
    protected Button registro;
    protected CheckBox condiciones;


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


            registro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Se comprueba si ha aceptado las condiciones
                    if (condiciones.isChecked()) {
                        // CÓDIGO DE LÓGICA DE REGISTRO
                        // Intent para cambiar de Activity
                        pasarPantalla = new Intent(RegistroActivity.this, LoadScreenActivity.class);
                        pasarPantalla.putExtra("PANTALLA", "OkActivity");
                        startActivity(pasarPantalla);
                    } else {
                        Toast.makeText(RegistroActivity.this,R.string.toast_terminos, Toast.LENGTH_SHORT).show();
                    }
                }
            });




    }

    // PONER LA LÓGICA DE REGISTRO Y CONTROLES DE LOS INPUTTEXT
}