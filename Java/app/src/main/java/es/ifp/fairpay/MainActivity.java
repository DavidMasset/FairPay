package es.ifp.fairpay;

import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MainActivity extends AppCompatActivity {

    private Conn conn = new Conn();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //conn.registrarUsuario("Juan", "4321","0x987654321");

        // Crea y ejecuta un hilo con la tarea a futuro futureTask
        new Thread(futureTask).start();

        try {
            ResultSet result = futureTask.get(); // Para la ejecución del hilo principal a la espera del resultado
            // Recorremos el ResultSet con los registros de la BD
            while (result.next()) {
                Log.d("",result.getString("usuario")+"-"+result.getString("password")+"-"+result.getString("billetera"));
            }
            // Cerramos el ResultSet
            result.close();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // Cerramos la conexion a la BD
            conn.desconectar();
        }
    }

    FutureTask<ResultSet> futureTask = new FutureTask<>(new Callable<ResultSet>() {
        @Override
        public ResultSet call() throws Exception {
            // Ejecuta las operaciones en la BD
            return conn.verTabla("usuarios"); // Devuelve el resultado
        }
    });
}
