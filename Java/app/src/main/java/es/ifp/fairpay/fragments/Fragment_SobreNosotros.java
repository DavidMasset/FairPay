package es.ifp.fairpay.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import es.ifp.fairpay.R;

public class Fragment_SobreNosotros extends Fragment {

    private ListView listViewRedes;

    // 🔹 Redes sociales que queremos mostrar
    private final String[] redesSociales = {"Twitter", "Instagram", "TikTok"};

    public Fragment_SobreNosotros() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflamos el layout del fragment
        return inflater.inflate(R.layout.fragment_sobre_nosotros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔹 Referencia al ListView del XML
        listViewRedes = view.findViewById(R.id.listView_sobrenosotros);

        // 🔹 Adaptador simple para mostrar las redes sociales
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                redesSociales
        );

        // 🔹 Asignamos el adaptador al ListView
        listViewRedes.setAdapter(adapter);
    }
}