package es.ifp.fairpay.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserRepository {
    public void setUsuarioClavePrivada(Context contexto, String clavePrivada){
        SharedPreferences prefs = contexto.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("CURRENT_USER_PRIVATE_KEY", clavePrivada);
        editor.apply();
    }

    public String getUsuarioClavePrivada(Context contexto){
        SharedPreferences prefs = contexto.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String clavePrivada = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (clavePrivada.isEmpty()) {
            Log.w("UserRepository", "No se encontró clave privada guardada.");
        }
        return clavePrivada;
    }

    public String getUsuarioEmail(Context contexto){
        SharedPreferences prefs = contexto.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String usuarioEmail = prefs.getString("CURRENT_USER_EMAIL", "");
        return usuarioEmail;
    }

    //TODO: Implementar seguramente la interfaz que pueda recoger los datos del db
}
