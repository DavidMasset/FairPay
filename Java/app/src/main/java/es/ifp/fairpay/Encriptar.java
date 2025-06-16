package es.ifp.fairpay;

import at.favre.lib.crypto.bcrypt.BCrypt;
public class Encriptar {

    // Metodo para encriptar una string con las opciones por defecto
    // Devuelve el HashString necesario para la verificacion
    public String Crypt(String str) {
        return BCrypt.withDefaults().hashToString(12, str.toCharArray());
    }

    // Metodo para verificar si una string se corresponde con su HashString
    public boolean Verificar(String str, String bcryptHashString) {

        BCrypt.Result result = BCrypt.verifyer().verify(str.toCharArray(), bcryptHashString);

        return result.verified;
    }

}
