package es.ifp.fairpay;

import at.favre.lib.crypto.bcrypt.BCrypt;
public class Encriptar {

    /**
     * Encripta una cadena de texto utilizando el algoritmo BCrypt con la configuración por defecto.
     *
     * @param str La cadena de texto a encriptar. No puede ser nula ni vacía.
     * @return El hash BCrypt de la cadena de texto. Este hash es necesario para la verificación posterior.
     * @throws IllegalArgumentException si la cadena de entrada es nula o vacía.
     */
    public static String encriptarHash(String str) {
        if (str == null || str.isEmpty()){
            throw new IllegalArgumentException("La cadena no puede ser nula o vacía");
        }
        return BCrypt.withDefaults().hashToString(12, str.toCharArray());
    }

    /**
     * Verifica si una cadena de texto coincide con un hash BCrypt.
     *
     * @param str La cadena de texto a verificar.
     * @param bcryptHashString El hash BCrypt con el que se comparará la cadena.
     * @return {@code true} si la cadena coincide con el hash, {@code false} en caso contrario o si ocurre un error durante la verificación.
     */
    public static boolean verificarHash(String str, String bcryptHashString) {
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(str.toCharArray(), bcryptHashString);
            return result.verified;
        } catch(Exception e) {
            System.out.println("Error al verificar el hash: " + e.getMessage());
            return false;
        }
    }

}
