package es.ifp.fairpay.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import es.ifp.fairpay.R;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected TimerTask tt;
    protected Timer t;

    // Variable global para almacenar la clave privada en memoria si fuera necesario
    private String usuarioClavePrivada = "";

    // Método principal que inicializa la pantalla de carga (Splash) y configura los elementos visuales
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajustamos los márgenes de la interfaz para respetar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intentamos recuperar la clave privada de las preferencias compartidas para tenerla disponible
        SharedPreferences prefs = getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String savedKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (!savedKey.isEmpty()) {
            this.usuarioClavePrivada = savedKey;
            Log.d("MainActivity", "Clave privada recuperada: " + savedKey);
        }

        // Tarea temporizada para realizar la transición automática a la pantalla de Login tras 5 segundos
        tt = new TimerTask() {
            @Override
            public void run() {
                pasarPantalla = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(pasarPantalla);
                finish();
            }
        };
        t = new Timer();
        t.schedule(tt, 5000);
    }

    // Método para actualizar la clave privada del usuario en esta actividad
    public void setUsuarioClavePrivada(String clave) {
        this.usuarioClavePrivada = clave;
    }

    // Método para obtener la clave privada almacenada en esta actividad
    public String getUsuarioClavePrivada() {
        return this.usuarioClavePrivada;
    }
}