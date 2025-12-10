package es.ifp.fairpay.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import es.ifp.fairpay.R;

public class WalletFragment extends Fragment {
    protected ListView lista;
    protected ArrayList<String> listaArray = new ArrayList<String>();
    protected ArrayAdapter<String> adaptador;
    protected Button verMovimientos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    // Se pone la lógica una vez se ha creado la vista para que todos los componentes estén cargados
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Instancia de los elementos
        lista = view.findViewById(R.id.listView_contactos_agenda);
        verMovimientos = view.findViewById(R.id.boton_vertodo_wallet);

        // Contenido de prueba, se carga desde BD
        listaArray.clear();
        listaArray.add("Usuario1");
        listaArray.add("Usuario2");
        listaArray.add("Usuario3");
        listaArray.add("Usuario4");
        listaArray.add("Usuario5");

        adaptador = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1 , listaArray);
        lista.setAdapter(adaptador);

        // Botón de ver todos los movimientos
        verMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
                bottomNav.setSelectedItemId(R.id.movimientosFragment);
            }
        });
    }
}