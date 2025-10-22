package es.ifp.fairpay.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import es.ifp.fairpay.R;
import es.ifp.fairpay.database.DatabaseConnection;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    protected Intent pasarPantalla;
    protected TimerTask tt;
    protected Timer t;

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
        /*Esta clase se encargará de ofrecer los datos a las activities y fragmentos.
        * Se encarga de la conexión con la base de datos y la ejecución de las consultas.
        * */
        // --- Lógica de la base de datos en un hilo secundario ---
        new Thread(() -> {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            /*Ejemplo de método para obtener los usuarios de la base de datos*/
            databaseConnection.obtenerUsuarios(rs -> {
                Log.d("FairPayDB", "--- Lista de usuarios ---");
                while (rs.next()) {
                    Log.d("FairPayDB", rs.getString("id_contacto") + "-" + rs.getString("nombre") + "-" + rs.getString("correo"));
                }
            });
        }).start();
        /* Se cierra el hilo para evitar que se bloquee la aplicación y se pueda seguir trabajando.
        * --- Fin de la lógica de la base de datos ---
        * */

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
