package es.ifp.fairpay;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Timer;
import java.util.TimerTask;

public class LoadScreenActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected TimerTask tt;
    protected Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_load_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.load_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // TimerTask para crear una tarea para el temporizador
        tt = new TimerTask() {
            @Override
            public void run() {
                // Intent para cambiar de Activity
                pasarPantalla = new Intent(LoadScreenActivity.this, OkActivity.class);
                startActivity(pasarPantalla);
            }
        };
        // Se instancia un temporizador
        t = new Timer();
        // Al metodo schedule se le pasa la tarea a ejecutar y el tiempo en milisegundos
        t.schedule(tt, 3000);
    }
}