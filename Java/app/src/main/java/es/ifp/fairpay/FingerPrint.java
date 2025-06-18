package es.ifp.fairpay;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.concurrent.Executor;

public class FingerPrint {

    public void getHuella(Context context){

        Executor executor;
        BiometricPrompt biometricPrompt;
        BiometricPrompt.PromptInfo promptInfo;

        // Toma el control del hilo principal para ejecutar tareas
        executor = ContextCompat.getMainExecutor(context);
        // Se instancia el prompt o ventana flotante con 3 eventos Error, Succeeded y Failed
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            // Se ejecuta cuando la autenticacion se cancela
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(context, "Cancelado: " + errString, Toast.LENGTH_SHORT).show();
            }

            // Se ejecuta cuando la autenticacion es correcta
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(context, "Correcto! " + result, Toast.LENGTH_SHORT).show();
            }

            // Se ejecuta cuando la autenticacion es erronea
            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(context, "Autenticacion erronea", Toast.LENGTH_SHORT).show();
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
