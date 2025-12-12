package es.ifp.fairpay.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class OperationsRepository {
    public void  saveOperationState(Context context, String state, String userEmail, String hash, String id, String logMsg) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("OP_STATE", state);
        if(hash!=null) editor.putString("OP_HASH", hash);
        if(hash!=null) editor.putString("OP_ID", id);
        if(hash!=null) editor.putString("OP_LOG", logMsg);
        editor.putString("OP_USER", userEmail);
        editor.apply();

    }
    public void clearOperationState(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();


    }
    public SharedPreferences restoreOperationState(Context context) {
        if (context == null){
            return null;
        }
        return context.getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
    }
}
