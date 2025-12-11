package es.ifp.fairpay.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import es.ifp.fairpay.R;
import es.ifp.fairpay.data.repository.UserRepository;
import es.ifp.fairpay.ui.authenticator.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;
//TODO: Separar la parte de Backend y de Frontend
public class MainActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected TimerTask tt;
    protected Timer t;

    // Método principal que inicializa la pantalla de carga (Splash) y configura los elementos visuales
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        UserRepository userRepository = new UserRepository();
        String claveGuardada = userRepository.getUsuarioClavePrivada(this);
        Log.d("MainActivity", "Proceso de carga de datos iniciado a través del repositorio.");
        if (!claveGuardada.isEmpty()) {
            Log.d("MainActivity", "El repositorio encontró una clave guardada.");
        }
        iniciarTimerSplash();
    }

    public void iniciarTimerSplash() {
        tt = new TimerTask() {
            @Override
            public void run() {
                pasarPantalla = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(pasarPantalla);
                finish(); // Finaliza esta activity para que el usuario no pueda volver a ella
            }
        };
        t = new Timer();
        t.schedule(tt, 5000);
    }

}