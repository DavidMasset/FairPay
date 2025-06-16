package es.ifp.fairpay;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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
        getHuella();
    }

    private void getHuella(){
        // Toma el control del hilo principal para ejecutar tareas
        executor = ContextCompat.getMainExecutor(this);
        // Se instancia el prompt o ventana flotante con 3 eventos Error, Succeeded y Failed
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            // Se ejecuta cuando la autenticacion se cancela
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(MainActivity.this, "Cancelado: " + errString, Toast.LENGTH_SHORT).show();
            }

            // Se ejecuta cuando la autenticacion es correcta
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(MainActivity.this, "Correcto! " + result, Toast.LENGTH_SHORT).show();
            }

            // Se ejecuta cuando la autenticacion es erronea
            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(MainActivity.this, "Autenticacion erronea", Toast.LENGTH_SHORT).show();
            }
        });

        // Genera el prompt con la información y solicitando la huella dactilar
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setSubtitle("Inicie sesión con su huella dactilar")
                .setNegativeButtonText("Cancelar")
                .build();

        // Ejecuta la autenticación instanciada anteriormente con los parametros de promptInfo
        biometricPrompt.authenticate(promptInfo);
    }
}