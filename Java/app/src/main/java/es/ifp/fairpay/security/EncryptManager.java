package es.ifp.fairpay.security;

import android.content.Context;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Gestiona el cifrado y descifrado de datos sensibles utilizando Jetpack Security.
 * Esta clase utiliza el AndroidKeystore para almacenar de forma segura la clave de cifrado.
 */
public class EncryptManager {

    private final Context context;
    private final MasterKey masterKey;

    /**
     * Constructor que inicializa el gestor de cifrado.
     * @param context El contexto de la aplicación, necesario para acceder al Keystore y al almacenamiento.
     * @throws GeneralSecurityException Si hay un error al crear la clave maestra.
     * @throws IOException Si hay un error de entrada/salida.
     */
    public EncryptManager(Context context) throws GeneralSecurityException, IOException {
        this.context = context.getApplicationContext();
                //Esta clave NUNCA sale del entorno seguro del Keystore.
        this.masterKey = new MasterKey.Builder(this.context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
    }

    /**
     * Cifra un dato de texto y lo guarda en un archivo seguro.
     * @param alias El nombre del archivo donde se guardará el dato cifrado (ej: "private_key_usuario123").
     * @param dataToEncrypt El texto plano que se quiere cifrar (ej: la clave privada).
     */
    public void encryptAndSave(String alias, String dataToEncrypt) throws GeneralSecurityException, IOException {
        // Se crea una referencia al archivo donde se guardarán los datos cifrados.
        File file = new File(context.getFilesDir(), alias);

        // Se crea un objeto EncryptedFile que se encarga de todo el cifrado.
        EncryptedFile encryptedFile = new EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        // Se escribe el dato en el archivo. La librería lo cifra automáticamente antes de escribirlo.
        byte[] fileContent = dataToEncrypt.getBytes(StandardCharsets.UTF_8);
        encryptedFile.openFileOutput().write(fileContent);
        encryptedFile.openFileOutput().flush();
        encryptedFile.openFileOutput().close();
    }

    /**
     * Carga un archivo cifrado y devuelve su contenido descifrado.
     * @param alias El nombre del archivo que contiene el dato cifrado.
     * @return El texto plano original, o null si el archivo no existe.
     */
    public String loadAndDecrypt(String alias) throws GeneralSecurityException, IOException {
        File file = new File(context.getFilesDir(), alias);

        if (!file.exists()) {
            return null; // El archivo no existe, no hay nada que descifrar.
        }

        EncryptedFile encryptedFile = new EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

        // Se lee el contenido del archivo. La librería lo descifra automáticamente después de leerlo.
        byte[] fileContent = encryptedFile.openFileInput().readAllBytes();
        return new String(fileContent, StandardCharsets.UTF_8);
    }
}
