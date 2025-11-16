package es.ifp.fairpay.utils;

import android.util.Patterns;

/**
 * Clase de utilidad para agrupar funciones de validación comunes.
 */
public class ValidationUtils {

    /**
     * Valida si el formato de una cadena de texto corresponde a un email válido.
     * @param email La cadena de texto a validar.
     * @return true si el email tiene un formato válido, false en caso contrario.
     */
    public static boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Aquí podrías añadir otras validaciones en el futuro (contraseñas, números, etc.)
}
