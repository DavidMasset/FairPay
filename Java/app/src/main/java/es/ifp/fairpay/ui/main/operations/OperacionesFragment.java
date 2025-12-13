package es.ifp.fairpay.ui.main.operations;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.web3j.crypto.Credentials;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ifp.fairpay.R;
import es.ifp.fairpay.data.service.FairPayService;

/**
 * ESTA CLASE ACTÚA COMO EL "HUB" O CONTROLADOR PRINCIPAL DEL MÓDULO DE OPERACIONES.
 *
 * Su función es centralizar la navegación y el estado inicial de las operaciones.
 * En lugar de contener toda la lógica (compra, venta, disputas) en un solo archivo gigante,
 * esta clase gestiona los menús principales y delega la funcionalidad específica a tres fragmentos hijos:
 *
 * 1. CompradorFragment: Gestiona la creación de depósitos, pagos y aprobaciones por parte del comprador.
 * 2. VendedorFragment: Gestiona la consulta de estado y la recepción de fondos por parte del vendedor.
 * 3. DisputasFragment: Gestiona todo el ciclo de vida de las disputas (apertura, chat, resolución).
 *
 * OperacionesFragment mantiene la lógica de "Escaneo de Operaciones Pendientes" porque es el punto de entrada
 * común para detectar en qué estado se encuentran los contratos inteligentes del usuario.
 */
public class OperacionesFragment extends Fragment {

    // UI Elements del Hub
    private Button btnRealizarOperacion;
    private Button btnPendingOps;
    private Button btnMainDisputes;

    private LinearLayout layoutMainMenu;
    private Button btnSelectBuyer, btnSelectSeller;

    // Elementos de Pendientes
    private LinearLayout layoutPendingSubcategories;
    private ScrollView scrollPendingResults;
    private LinearLayout layoutPendingContainer;
    private TextView tvPendingStatus;
    private Button btnCatCreated, btnCatFunded, btnCatApproved;

    private CardView cardMainLog;
    private TextView tvResultadoLog;

    private Context mContext;
    private static final String PREF_COMPLETED_IDS = "COMPLETED_IDS";

    // Esta función se encarga de adjuntar el contexto del fragmento para asegurar el acceso a recursos
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // Esta función se encarga de inflar el diseño visual del fragmento y configurar la vista inicial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operaciones, container, false);
        setupViews(view);
        setupListeners();
        return view;
    }

    // Esta función se encarga de gestionar la navegación entrante, como cuando venimos de la agenda con una wallet
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String walletVendedor = getArguments().getString("WALLET_VENDEDOR");
            if (walletVendedor != null && !walletVendedor.isEmpty()) {
                hideAllSections();

                btnRealizarOperacion.setVisibility(View.VISIBLE);
                btnRealizarOperacion.setText("Cerrar operaciones");

                Bundle args = new Bundle();
                args.putString("WALLET_VENDEDOR", walletVendedor);

                CompradorFragment fragment = new CompradorFragment();
                fragment.setArguments(args);

                loadFragment(fragment, "CompradorDirect");
            }
        }
    }

    // Esta función se encarga de vincular los elementos visuales del XML con las variables de la clase
    private void setupViews(View view) {
        btnRealizarOperacion = view.findViewById(R.id.button_realizar_operacion);
        btnPendingOps = view.findViewById(R.id.btn_pending_ops);
        btnMainDisputes = view.findViewById(R.id.btn_main_disputes);

        layoutMainMenu = view.findViewById(R.id.layout_main_menu);
        btnSelectBuyer = view.findViewById(R.id.btn_select_buyer);
        btnSelectSeller = view.findViewById(R.id.btn_select_seller);

        layoutPendingSubcategories = view.findViewById(R.id.layout_pending_subcategories);
        scrollPendingResults = view.findViewById(R.id.scroll_pending_results);
        layoutPendingContainer = view.findViewById(R.id.layout_pending_container);
        tvPendingStatus = view.findViewById(R.id.tv_pending_status);

        btnCatCreated = view.findViewById(R.id.btn_cat_created);
        btnCatFunded = view.findViewById(R.id.btn_cat_funded);
        btnCatApproved = view.findViewById(R.id.btn_cat_approved);

        cardMainLog = view.findViewById(R.id.card_result_log);
        tvResultadoLog = view.findViewById(R.id.tv_resultado_log);
    }

    // Esta función se encarga de configurar los eventos de clic para los botones principales y de navegación
    private void setupListeners() {
        // INICIAR
        btnRealizarOperacion.setOnClickListener(v -> {
            hideKeyboard();
            if (btnRealizarOperacion.getText().toString().startsWith("Cerrar")) {
                resetUI();
            } else {
                hideAllSections();
                btnRealizarOperacion.setVisibility(View.VISIBLE);
                btnRealizarOperacion.setText("Cerrar operaciones");
                layoutMainMenu.setVisibility(View.VISIBLE);
            }
        });

        // SELECCIÓN DE ROL
        btnSelectBuyer.setOnClickListener(v -> loadFragment(new CompradorFragment(), "Comprador"));
        btnSelectSeller.setOnClickListener(v -> loadFragment(new VendedorFragment(), "Vendedor"));

        // PENDIENTES
        btnPendingOps.setOnClickListener(v -> {
            hideKeyboard();
            if (btnPendingOps.getText().toString().startsWith("Cerrar")) {
                resetUI();
            } else {
                hideAllSections();
                btnPendingOps.setVisibility(View.VISIBLE);
                btnPendingOps.setText("Cerrar operaciones pendientes");
                layoutPendingSubcategories.setVisibility(View.VISIBLE);
                closeChildFragment();
            }
        });

        btnCatCreated.setOnClickListener(v -> { scrollPendingResults.setVisibility(View.VISIBLE); performScanAndPopulate(true, 1); });
        btnCatFunded.setOnClickListener(v -> { scrollPendingResults.setVisibility(View.VISIBLE); performScanAndPopulate(true, 2); });
        btnCatApproved.setOnClickListener(v -> { scrollPendingResults.setVisibility(View.VISIBLE); performScanAndPopulate(true, 3); });

        // DISPUTAS
        btnMainDisputes.setOnClickListener(v -> {
            hideKeyboard();
            if (btnMainDisputes.getText().toString().startsWith("Cerrar")) {
                resetUI();
            } else {
                hideAllSections();
                btnMainDisputes.setVisibility(View.VISIBLE);
                btnMainDisputes.setText("Cerrar disputas");
                loadFragment(new DisputasFragment(), "Disputas");
            }
        });
    }

    // Esta función se encarga de cargar un fragmento hijo dentro del contenedor principal
    private void loadFragment(Fragment fragment, String tag) {
        layoutMainMenu.setVisibility(View.GONE);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    // Esta función se encarga de eliminar el fragmento hijo actual si existe, para limpiar la vista
    private void closeChildFragment() {
        Fragment current = getChildFragmentManager().findFragmentById(R.id.fragment_container);
        if (current != null) {
            getChildFragmentManager().beginTransaction().remove(current).commit();
        }
    }

    // Esta función se encarga de restaurar la interfaz principal a su estado inicial
    public void resetUI() {
        hideKeyboard();
        closeChildFragment();

        btnRealizarOperacion.setVisibility(View.VISIBLE);
        btnRealizarOperacion.setText("Iniciar operaciones");

        btnPendingOps.setVisibility(View.VISIBLE);
        btnPendingOps.setText("Operaciones pendientes");

        btnMainDisputes.setVisibility(View.VISIBLE);
        btnMainDisputes.setText("Disputas");

        layoutMainMenu.setVisibility(View.GONE);
        layoutPendingSubcategories.setVisibility(View.GONE);
        scrollPendingResults.setVisibility(View.GONE);
        tvResultadoLog.setText("");
    }

    // Esta función se encarga de ocultar los botones del menú principal temporalmente
    private void hideAllSections() {
        btnRealizarOperacion.setVisibility(View.GONE);
        btnPendingOps.setVisibility(View.GONE);
        btnMainDisputes.setVisibility(View.GONE);
    }

    // Esta función se encarga de escanear la blockchain en busca de operaciones pendientes y mostrarlas
    private void performScanAndPopulate(boolean isPending, int filterType) {
        LinearLayout container = layoutPendingContainer;
        TextView statusView = tvPendingStatus;
        container.removeAllViews();
        container.addView(statusView);
        statusView.setText("Escaneando depósitos...");
        statusView.setVisibility(View.VISIBLE);

        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String pk = prefs.getString("CURRENT_USER_PRIVATE_KEY", "0x01");
        SharedPreferences statePrefs = mContext.getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
        Set<String> completedIds = statePrefs.getStringSet(PREF_COMPLETED_IDS, new HashSet<>());

        new Thread(() -> {
            FairPayService service = new FairPayService(pk);
            String myAddress;
            try { myAddress = Credentials.create(pk).getAddress(); } catch (Exception e) { updateStatus(statusView, "Error clave."); return; }

            int foundCount = 0;
            int consecutiveFailures = 0;
            int currentId = 0;

            while (consecutiveFailures < 5) {
                try {
                    BigInteger id = BigInteger.valueOf(currentId);
                    List<Type> details = service.getEscrowDetails(id);
                    if (details != null && !details.isEmpty()) {
                        consecutiveFailures = 0;
                        String buyer = details.get(0).getValue().toString();
                        String seller = details.get(1).getValue().toString();
                        BigInteger amount = (BigInteger) details.get(2).getValue();
                        boolean isFunded = (Boolean) details.get(3).getValue();
                        BigInteger approvals = (BigInteger) details.get(4).getValue();

                        boolean amIBuyer = buyer.equalsIgnoreCase(myAddress);
                        boolean amISeller = seller.equalsIgnoreCase(myAddress);

                        if ((amIBuyer || amISeller) && isPending && !completedIds.contains(id.toString())) {
                            if (filterType == 1 && amIBuyer && amount.equals(BigInteger.ZERO) && !isFunded && approvals.equals(BigInteger.ZERO)) {
                                addPendingButton(container, "Depósito CREADO ID: " + id, id.toString(), 1); foundCount++;
                            } else if (filterType == 2 && amIBuyer && amount.compareTo(BigInteger.ZERO) > 0 && isFunded && approvals.equals(BigInteger.ZERO)) {
                                addPendingButton(container, "Depósito PAGADO ID: " + id, id.toString(), 2); foundCount++;
                            } else if (filterType == 3 && amISeller && amount.compareTo(BigInteger.ZERO) > 0 && isFunded && approvals.equals(BigInteger.ONE)) {
                                addPendingButton(container, "Depósito APROBADO (1/2) ID: " + id, id.toString(), 3); foundCount++;
                            }
                        }
                    } else { consecutiveFailures++; }
                } catch (Exception e) { consecutiveFailures++; }
                currentId++;
                if (currentId % 5 == 0) updateStatus(statusView, "Escaneando ID: " + currentId + "...");
            }
            int finalFound = foundCount;
            if (getActivity() != null) getActivity().runOnUiThread(() -> { if (finalFound == 0) statusView.setText("No se encontraron operaciones."); else statusView.setVisibility(View.GONE); });
        }).start();
    }

    // Esta función auxiliar se encarga de actualizar el texto de estado en el hilo principal
    private void updateStatus(TextView tv, String msg) {
        if (getActivity() != null) getActivity().runOnUiThread(() -> tv.setText(msg));
    }

    // Esta función se encarga de crear y añadir dinámicamente un botón para cada operación pendiente
    private void addPendingButton(LinearLayout container, String text, String id, int type) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Button btn = new Button(mContext);
                btn.setText(text);
                btn.setAllCaps(false);
                if (type == 1 || type == 2) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B8E4C9")));
                    btn.setTextColor(Color.BLACK);
                } else if (type == 3) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F7C6DA")));
                    btn.setTextColor(Color.BLACK);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 5, 0, 5);
                btn.setLayoutParams(params);
                btn.setOnClickListener(v -> handlePendingAction(id, type));
                container.addView(btn);
            });
        }
    }

    // Esta función se encarga de gestionar la acción al pulsar un botón de operación pendiente
    private void handlePendingAction(String id, int type) {
        hideKeyboard();
        layoutPendingSubcategories.setVisibility(View.GONE);
        scrollPendingResults.setVisibility(View.GONE);

        Bundle args = new Bundle();
        args.putString("PENDING_ID", id);
        args.putInt("PENDING_TYPE", type);

        Fragment targetFragment = null;
        if (type == 1 || type == 2) targetFragment = new CompradorFragment();
        else if (type == 3) targetFragment = new VendedorFragment();

        if (targetFragment != null) {
            targetFragment.setArguments(args);
            hideAllSections();
            btnRealizarOperacion.setVisibility(View.VISIBLE);
            btnRealizarOperacion.setText("Cerrar operaciones");
            loadFragment(targetFragment, "PendingAction");
        }
    }

    // Esta función se encarga de ocultar el teclado virtual si está visible
    private void hideKeyboard() {
        if (getActivity() != null) {
            View v = getActivity().getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}