package es.ifp.fairpay.data.security;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import java.util.concurrent.Executor;

public class FingerprintCheck {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    public FingerprintCheck() {

    }
    public void showFingerPrint(Context context){

        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(context, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                /*
                if (!cajaUsuario.getText().toString().isEmpty() && !cajaPassword.getText().toString().isEmpty()) {
                    prefs.guardarInfo(cajaUsuario.getText().toString(), crypt.Encriptar(cajaPassword.getText().toString()));
                }
                pasarPantalla = new Intent(MainActivity.this, InicioActivity.class);
                startActivity(pasarPantalla);
                */
                Toast.makeText(context, "Todo correcto", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setSubtitle("Inicie sesión con su huella dactilar")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
