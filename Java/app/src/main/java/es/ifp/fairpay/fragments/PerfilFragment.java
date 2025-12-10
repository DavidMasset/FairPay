package es.ifp.fairpay.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.util.ArrayList;

import es.ifp.fairpay.R;
import es.ifp.fairpay.activities.LoginActivity;

public class PerfilFragment extends Fragment {

    protected ListView list;
    protected ImageButton imagenPerfil;
    protected ArrayList<String> listaArray = new ArrayList<String>();
    protected ArrayAdapter<String> adaptador;

    // Variable para guardar la URI de la imagen seleccionada temporalmente
    private Uri imageUri;

    // Lanzador para gestionar el resultado de la selección de imagen de la galería
    private ActivityResultLauncher<Intent> pickImageLauncher;

    public PerfilFragment() {
        // Constructor público vacío requerido por Android
    }

    // Esta función se encarga de inflar el diseño visual del fragmento para mostrarlo en pantalla
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    // Esta función se encarga de inicializar los componentes de la interfaz, configurar el menú de opciones y gestionar la selección de imagen de perfil
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vinculación de los elementos visuales
        list = (ListView) view.findViewById(R.id.listview_perfil);
        imagenPerfil = (ImageButton) view.findViewById(R.id.imageButton_perfil);

        // Añadimos las opciones del menú de perfil
        listaArray.add(getString(R.string.perfil_ayuda));
        listaArray.add(getString(R.string.perfil_modificar_datos));
        listaArray.add(getString(R.string.perfil_sobre_nosotros));
        listaArray.add(getString(R.string.perfil_cerrar_sesion));

        // Configuración del adaptador para la lista
        adaptador = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, listaArray);
        list.setAdapter(adaptador);

        // Listener para gestionar la navegación según la opción seleccionada en la lista
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

                switch (position) {
                    case 0: // Ayuda
                        navController.navigate(R.id.fragment_ayuda);
                        break;
                    case 1: // Modificar Datos
                        navController.navigate(R.id.fragment_modificar_datos);
                        break;
                    case 2: // Sobre Nosotros
                        navController.navigate(R.id.fragment_sobre_nosotros);
                        break;
                    case 3: // Cerrar Sesión
                        // Limpiamos la pila de actividades y volvemos al Login
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                        break;
                }
            }
        });

        // Inicializamos el launcher para procesar la imagen seleccionada de la galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            try {
                                // Convertimos la URI en un Bitmap para mostrarlo en el botón
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                                imagenPerfil.setImageBitmap(bitmap);
                                Toast.makeText(requireContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Listener para abrir la galería al pulsar sobre la imagen de perfil
        imagenPerfil.setOnClickListener(v -> abrirGaleria());
    }

    // Esta función se encarga de lanzar el intento para abrir la galería del dispositivo
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // Filtramos para mostrar solo imágenes
        pickImageLauncher.launch(intent);
    }
}