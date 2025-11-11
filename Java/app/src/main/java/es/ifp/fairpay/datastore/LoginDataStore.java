package es.ifp.fairpay.datastore;

import android.content.Context;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import io.reactivex.rxjava3.core.Single;

public class LoginDataStore {

    protected static final String DS_NAME = "login_data";
    protected static final Preferences.Key<String> KEY_EMAIL =
            PreferencesKeys.stringKey("EMAIL");
    protected static final Preferences.Key<String> KEY_PASS =
            PreferencesKeys.stringKey("PASS");
    protected RxDataStore<Preferences> dataStore;

    /**
     * Constructor de la clase LoginDataStore
     * Ruta del archivo: data/data/nombre_paquete/files/datastore
     * Crea el archivo login_data.preferences.pb
     * Le añade las claves EMAIL y PASS vacías
     * Se pasa el contexto (View) como parámetro
     * @param context vista o contexto sobre el que se ejecuta
     */
    public LoginDataStore(Context context) {
        this.dataStore = new RxPreferenceDataStoreBuilder(context, DS_NAME).build();
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences m = prefsIn.toMutablePreferences();
            m.set(KEY_EMAIL, "");
            m.set(KEY_PASS, "");
            return Single.just(m);
        });
    }

    /**
     * Metodo para guardar los datos en DataStore en sus respectivas claves
     * @param email valor de la clave EMAIL
     * @param pass valor de la clave PASS
     */
    public void guardarDatos(String email, String pass) {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences m = prefsIn.toMutablePreferences();
            m.set(KEY_EMAIL, email);
            m.set(KEY_PASS, pass);
            return Single.just(m);
        });
    }

    /**
     * Metodo para limpiar las claves de DataStore (Cerrar Sesión)
     * Las claves siguen existiendo pero vacías
     */
    public void borrarDatos() {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences m = prefsIn.toMutablePreferences();
            m.set(KEY_EMAIL, "");
            m.set(KEY_PASS, "");
            return Single.just(m);
        });
    }

    /**
     * Metodo para leer un dato almacenado pasando como parámetro el nombre de la key
     * @param key nombre de la key a leer
     * @return String con el valor de la clave
     */
    public String leerDato(String key) {
        Preferences.Key<String> PREF_KEY = PreferencesKeys.stringKey(key);
        Single<String> value = dataStore.data().firstOrError().map(prefs -> prefs.get(PREF_KEY)).onErrorReturnItem("null");
        return value.blockingGet();
    }
}
