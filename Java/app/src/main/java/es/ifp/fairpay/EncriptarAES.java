package faseTesteo;

//Importamos las clases necesarias para cifrado y codificación Base64.
import javax.crypto.Cipher;                // Clase principal para cifrar/descifrar datos.
import javax.crypto.spec.SecretKeySpec;    // Define la clave AES a usar.
import android.util.Base64;                // Clase de Android para convertir bytes en texto Base64.
import java.nio.charset.StandardCharsets;  // Define el formato de caracteres (UTF-8).

/**
* Clase utilitaria para encriptar y desencriptar texto usando AES.
* Este ejemplo usa el modo "AES/ECB/PKCS5Padding".
*/
public class Encriptar {

 //Clave secreta utilizada para el cifrado AES.
 //Debe tener exactamente 16, 24 o 32 caracteres (128, 192 o 256 bits).
 private static final String CLAVE_SECRETA = "1234567890123456";

 /**
  * Encripta un texto usando AES (modo ECB con padding PKCS5).
  * @param str Texto a encriptar (no puede ser nulo o vacío).
  * @return El texto encriptado codificado en Base64.
  */
 public static String encriptar(String str) {
     if (str == null || str.isEmpty()) {
         throw new IllegalArgumentException("La cadena no puede ser nula o vacía");
     }

     try {
         //️Creamos la clave AES a partir de la cadena CLAVE_SECRETA.
         SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8), "AES");

         //Obtenemos una instancia del cifrador AES en modo ECB con relleno PKCS5.
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

         //Inicializamos el cifrador en modo ENCRIPTAR con nuestra clave.
         cipher.init(Cipher.ENCRYPT_MODE, secretKey);

         //Ciframos el texto (convertido a bytes).
         byte[] encryptedBytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));

         //Convertimos los bytes cifrados a Base64 (para poder guardarlos como texto legible).
         return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

     } catch (Exception e) {
         // Si algo falla, lanzamos una excepción con un mensaje claro.
         throw new RuntimeException("Error al encriptar: " + e.getMessage(), e);
     }
 }
 /**
  * Desencripta un texto cifrado con el mismo método AES.
  * @param encryptedText Texto cifrado en Base64.
  * @return Texto original desencriptado.
  */
 public static String desencriptar(String encryptedText) {
     if (encryptedText == null || encryptedText.isEmpty()) {
         throw new IllegalArgumentException("El texto cifrado no puede ser nulo o vacío");
     }

     try {
         //Creamos la misma clave AES (debe ser la misma usada al cifrar).
         SecretKeySpec secretKey = new SecretKeySpec(CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8), "AES");

         //Obtenemos el mismo tipo de cifrador usado al encriptar.
         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

         //Lo inicializamos en modo DESENCRIPTAR.
         cipher.init(Cipher.DECRYPT_MODE, secretKey);

         //Convertimos el texto Base64 de vuelta a bytes.
         byte[] decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT);

         //Desencriptamos los bytes para recuperar el texto original.
         byte[] decryptedBytes = cipher.doFinal(decodedBytes);

         //Convertimos los bytes a String y lo devolvemos.
         return new String(decryptedBytes, StandardCharsets.UTF_8);

     } catch (Exception e) {
         throw new RuntimeException("Error al desencriptar: " + e.getMessage(), e);
     }
 }

 /**
  * Verifica si un texto original coincide con un texto cifrado dado.
  * @param textoOriginal Texto sin cifrar.
  * @param textoCifrado Texto cifrado en Base64.
  * @return true si el texto desencriptado coincide con el original; false en caso contrario.
  */
 public static boolean verificar(String textoOriginal, String textoCifrado) {
     try {
         // Desencripta el texto cifrado y lo compara con el texto original.
         String desencriptado = desencriptar(textoCifrado);
         return textoOriginal.equals(desencriptado);
     } catch (Exception e) {
         // Si falla la desencriptación, se imprime el error y se devuelve false.
         System.out.println("Error al verificar: " + e.getMessage());
         return false;
     }
 }
 /**
  * Método de prueba para ver cómo funciona.
  */
 public static void main(String[] args) {
     String texto = "Hola AES";

     // Encriptamos el texto
     String cifrado = encriptar(texto);
     System.out.println("Texto cifrado: " + cifrado);

     // Lo desencriptamos
     String descifrado = desencriptar(cifrado);
     System.out.println("Texto descifrado: " + descifrado);

     // Verificamos si coincide
     System.out.println("Verificación: " + verificar(texto, cifrado));
 }
}	
