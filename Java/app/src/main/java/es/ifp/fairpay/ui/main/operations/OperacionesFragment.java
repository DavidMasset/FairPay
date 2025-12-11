package es.ifp.fairpay.ui.main.operations;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;
import org.web3j.abi.datatypes.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import es.ifp.fairpay.R;
import es.ifp.fairpay.FairPayService;
import es.ifp.fairpay.activities.MainActivity;
import es.ifp.fairpay.database.DatabaseConnection;

public class OperacionesFragment extends Fragment {

    // Constantes de configuración
    private final String PLATFORM_EMAIL = "firewire80a@gmail.com";
    private String currentUserEmail = "";

    // Componentes de la Interfaz (UI)
    private EditText txtComprador;
    private EditText txtVendedor;
    private DatabaseConnection dbConnection;
    private Button btnConsultar;
    private Button btnCrearDeposito;
    private Button btnEstadoPago;
    private Button btnRecibirPago;
    private Button btnResolverDisputa;
    private TextView tvResultadoLog;
    private Button btnRealizarOperacion;

    // Contenedores de menús
    private LinearLayout layoutMainMenu, layoutBuyerMenu, layoutSellerMenu, layoutDisputeMenu;
    private Button btnSelectBuyer, btnSelectSeller, btnSelectDispute;
    private Button btnBuyerBack, btnSellerBack, btnDisputeBack;

    // Elementos del flujo de Comprador
    private Button btnBuyerNew, btnBuyerExisting;
    private LinearLayout layoutFormsContainer, layoutCreateEscrow, layoutFundEscrow, layoutBuyerApprove, layoutExistingEscrowBuyer;
    private EditText inputPrivateKey, inputSeller;
    private EditText inputEscrowId, inputFundAmount, inputPrivateKeyFund;
    private EditText inputExistingEscrowId;
    private Button btnEnviarEscrow, btnSearchId, btnFormBackBuyer, btnFundEscrow, btnBuyerApprove, btnCheckEscrowStatus, btnExistingBackBuyer;

    // Elementos del flujo de Vendedor
    private EditText inputSellerPrivateKey, inputSellerEscrowId;
    private Button btnSellerReceivePayment, btnSellerCheckStatus;

    // Elementos del flujo de Disputas
    private Button btnOpenDisputeMenu, btnResolveDisputeListMenu;
    private LinearLayout layoutOpenDisputeForm;
    private EditText inputDisputeEscrowId, inputDisputeReason;
    private Spinner spinnerDisputeRole;
    private Button btnSubmitDispute, btnOpenDisputeBack;

    // Lista y detalles de resolución
    private LinearLayout layoutResolveList;
    private LinearLayout containerDisputesButtons;
    private Button btnResolveListBack;

    private LinearLayout layoutResolveAction;
    private LinearLayout containerDisputeHistory;
    private SwipeRefreshLayout swipeRefreshDispute;
    private Button btnGoToResolveForm;
    private Button btnReplyDispute;
    private Button btnRequestPlatformReview;
    private Button btnResolveActionBack;
    private LinearLayout layoutFinalResolveForm;
    private EditText inputResolveId, inputPlatformKey;
    private Spinner spinnerResolveDecision;
    private Button btnExecuteResolve, btnResolveFinalBack;
    private ScrollView scrollDisputeHistory;

    // Variables de estado para persistencia de operaciones
    private String lastTxHash = null;
    private BigInteger lastEscrowId = null;
    private final String SEARCH_BUTTON_ORIGINAL_TEXT = "BUSCAR ID CONFIRMADO";
    private Context mContext;

    // Claves para SharedPreferences
    private static final String PREF_OP_STATE = "OP_STATE";
    private static final String PREF_OP_HASH = "OP_HASH";
    private static final String PREF_OP_ID = "OP_ID";
    private static final String PREF_OP_LOG = "OP_LOG";
    private static final String PREF_OP_USER = "OP_USER";

    private static final String STATE_NONE = "NONE";
    private static final String STATE_WAITING_ID = "WAITING_ID";
    private static final String STATE_WAITING_FUND = "WAITING_FUND";
    private static final String STATE_WAITING_APPROVE = "WAITING_APPROVE";
    private static final String STATE_WAITING_VERIFY = "WAITING_VERIFY";

    // Estructura de datos para agrupar mensajes de disputas
    private Map<String, List<Map<String, String>>> groupedDisputes = new HashMap<>();


    public OperacionesFragment() {
        dbConnection = new DatabaseConnection();
    }

    @Override
    public void onAttach(@NonNull Context context) { super.onAttach(context); mContext = context; }

    // Esta función se encarga de inflar la vista del fragmento e inicializar la interfaz
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operaciones, container, false);
        setupViews(view);
        setupListeners();
        return view;
    }

    // Esta función se encarga de configurar la lógica inicial, recuperar el estado y establecer los listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Identificar usuario actual
        getCurrentUserContext();

        // 2. Pre-cargar claves privadas si están disponibles
        autoFillKeys();

        // 3. Recuperar estado de operación interrumpida
        restoreOperationState();

        // 4. Gestionar navegación entrante desde la Agenda (enviar dinero a contacto)
        if (getArguments() != null) {
            String walletVendedor = getArguments().getString("WALLET_VENDEDOR");
            if (walletVendedor != null && !walletVendedor.isEmpty()) {
                clearOperationState();
                if (inputSeller != null) inputSeller.setText(walletVendedor);
                if (btnRealizarOperacion != null) {
                    btnRealizarOperacion.setVisibility(View.VISIBLE);
                    btnRealizarOperacion.setText("Cerrar operaciones");
                }
                if (layoutMainMenu != null) layoutMainMenu.setVisibility(View.GONE);
                if (layoutFormsContainer != null) {
                    layoutFormsContainer.setVisibility(View.VISIBLE);
                    layoutFormsContainer.setBackgroundColor(Color.parseColor("#B8E4C9"));
                }
                if (layoutCreateEscrow != null) {
                    layoutCreateEscrow.setVisibility(View.VISIBLE);
                }
                if (tvResultadoLog != null) {
                    tvResultadoLog.setText("PASO 1: Comprador inicia el depósito.");
                    tvResultadoLog.setTextColor(Color.DKGRAY);
                }
            }
        }
    }

    // Esta función se encarga de obtener el email del usuario logueado desde las preferencias
    private void getCurrentUserContext() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
            currentUserEmail = prefs.getString("CURRENT_USER_EMAIL", "");
        }
    }

    // --- MÉTODOS DE PERSISTENCIA ---

    // Esta función se encarga de guardar el estado actual de la operación para recuperarlo si la app se cierra
    private void saveOperationState(String state, String hash, String id, String logMsg) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_OP_STATE, state);
        if (hash != null) editor.putString(PREF_OP_HASH, hash);
        if (id != null) editor.putString(PREF_OP_ID, id);
        if (logMsg != null) editor.putString(PREF_OP_LOG, logMsg);

        // Vinculamos el estado al usuario actual para evitar mezclar sesiones
        editor.putString(PREF_OP_USER, currentUserEmail);

        editor.apply();
    }

    // Esta función se encarga de limpiar cualquier estado de operación guardado
    private void clearOperationState() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        lastTxHash = null;
        lastEscrowId = null;
    }

    // Esta función se encarga de restaurar la interfaz al estado donde se quedó el usuario
    private void restoreOperationState() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("FairPayState", Context.MODE_PRIVATE);

        String savedUser = prefs.getString(PREF_OP_USER, "");
        if (!savedUser.equals(currentUserEmail)) {
            clearOperationState();
            return;
        }

        String state = prefs.getString(PREF_OP_STATE, STATE_NONE);

        // Si estábamos en fases finales, preferimos reiniciar para evitar confusiones
        if (state.equals(STATE_WAITING_APPROVE) || state.equals(STATE_WAITING_VERIFY)) {
            clearOperationState();
            return;
        }

        String savedHash = prefs.getString(PREF_OP_HASH, null);
        String savedId = prefs.getString(PREF_OP_ID, null);
        String savedLog = prefs.getString(PREF_OP_LOG, "");

        if (state.equals(STATE_NONE)) return;

        lastTxHash = savedHash;
        if (savedId != null) lastEscrowId = new BigInteger(savedId);

        hideKeyboard();
        showView(layoutFormsContainer);

        if (btnRealizarOperacion != null) {
            btnRealizarOperacion.setVisibility(View.VISIBLE);
            btnRealizarOperacion.setText("Cerrar operaciones");
        }

        layoutFormsContainer.setBackgroundColor(Color.parseColor("#B8E4C9"));

        if (tvResultadoLog != null) {
            tvResultadoLog.setText(savedLog);
            tvResultadoLog.setTextColor(Color.DKGRAY);
        }

        switch (state) {
            case STATE_WAITING_ID:
                layoutCreateEscrow.setVisibility(View.VISIBLE);
                btnSearchId.setVisibility(View.VISIBLE);
                btnSearchId.setEnabled(true);
                btnSearchId.setText(SEARCH_BUTTON_ORIGINAL_TEXT);
                break;
            case STATE_WAITING_FUND:
                layoutFundEscrow.setVisibility(View.VISIBLE);
                if (savedId != null) inputEscrowId.setText(savedId);
                btnFundEscrow.setVisibility(View.VISIBLE);
                btnFundEscrow.setEnabled(true);
                break;
        }
        autoFillKeys();
    }

    // Esta función se encarga de rellenar los campos de clave privada automáticamente si ya está en sesión
    private void autoFillKeys() {
        String myKey = "";
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            myKey = main.getUsuarioClavePrivada();
        }
        if (myKey == null || myKey.isEmpty()) {
            if (getContext() != null) {
                SharedPreferences prefs = getContext().getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
                myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
            }
        }
        if (myKey != null && !myKey.isEmpty()) {
            if (inputPrivateKey != null) inputPrivateKey.setText(myKey);
            if (inputPrivateKeyFund != null) inputPrivateKeyFund.setText(myKey);
            if (inputSellerPrivateKey != null) inputSellerPrivateKey.setText(myKey);
            if (inputPlatformKey != null) inputPlatformKey.setText(myKey);
        }
    }

    // Esta función se encarga de vincular todos los elementos visuales del XML con las variables del fragmento
    private void setupViews(View view) {
        tvResultadoLog = view.findViewById(R.id.tv_resultado_log);
        btnRealizarOperacion = view.findViewById(R.id.button_realizar_operacion);

        layoutMainMenu = view.findViewById(R.id.layout_main_menu);
        layoutBuyerMenu = view.findViewById(R.id.layout_buyer_menu);
        layoutSellerMenu = view.findViewById(R.id.layout_seller_menu);
        layoutDisputeMenu = view.findViewById(R.id.layout_dispute_menu);

        btnSelectBuyer = view.findViewById(R.id.btn_select_buyer);
        btnSelectSeller = view.findViewById(R.id.btn_select_seller);
        btnSelectDispute = view.findViewById(R.id.btn_select_dispute);

        btnBuyerBack = view.findViewById(R.id.btn_buyer_back);
        btnSellerBack = view.findViewById(R.id.btn_seller_back);
        btnDisputeBack = view.findViewById(R.id.btn_dispute_back);

        btnBuyerNew = view.findViewById(R.id.btn_buyer_new);
        btnBuyerExisting = view.findViewById(R.id.btn_buyer_existing);
        layoutFormsContainer = view.findViewById(R.id.layout_forms_container);
        layoutCreateEscrow = view.findViewById(R.id.layout_create_escrow);
        layoutFundEscrow = view.findViewById(R.id.layout_fund_escrow);
        layoutBuyerApprove = view.findViewById(R.id.layout_buyer_approve);
        layoutExistingEscrowBuyer = view.findViewById(R.id.layout_existing_escrow_buyer);

        inputPrivateKey = view.findViewById(R.id.input_private_key);
        inputSeller = view.findViewById(R.id.input_seller);
        inputEscrowId = view.findViewById(R.id.input_escrow_id);
        inputPrivateKeyFund = view.findViewById(R.id.input_private_key_fund);
        inputFundAmount = view.findViewById(R.id.input_fund_amount);
        inputExistingEscrowId = view.findViewById(R.id.input_existing_escrow_id);

        btnEnviarEscrow = view.findViewById(R.id.button_enviar_create_escrow);
        btnSearchId = view.findViewById(R.id.button_search_id);
        btnFormBackBuyer = view.findViewById(R.id.btn_form_back_buyer);
        btnFundEscrow = view.findViewById(R.id.button_fund_escrow);
        btnBuyerApprove = view.findViewById(R.id.button_buyer_approve);
        btnCheckEscrowStatus = view.findViewById(R.id.btn_check_escrow_status);
        btnExistingBackBuyer = view.findViewById(R.id.btn_existing_back_buyer);

        inputSellerPrivateKey = view.findViewById(R.id.input_seller_private_key);
        inputSellerEscrowId = view.findViewById(R.id.input_seller_escrow_id);
        btnSellerCheckStatus = view.findViewById(R.id.btn_seller_check_status);
        btnSellerReceivePayment = view.findViewById(R.id.btn_seller_receive);

        btnOpenDisputeMenu = view.findViewById(R.id.btn_open_dispute_menu);
        btnResolveDisputeListMenu = view.findViewById(R.id.btn_resolve_dispute_list_menu);
        layoutOpenDisputeForm = view.findViewById(R.id.layout_open_dispute_form);
        inputDisputeEscrowId = view.findViewById(R.id.input_dispute_escrow_id);
        inputDisputeReason = view.findViewById(R.id.input_dispute_reason);
        spinnerDisputeRole = view.findViewById(R.id.spinner_dispute_role);
        btnSubmitDispute = view.findViewById(R.id.btn_submit_dispute);
        btnOpenDisputeBack = view.findViewById(R.id.btn_open_dispute_back);

        // Configuración personalizada del Spinner de Roles (estética visual)
        String[] roles = {"COMPRADOR", "VENDEDOR"};
        ArrayAdapter<String> adapterRole = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, roles) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                view.setLayoutParams(params);
                TextView tv = (TextView) view;
                applyCustomStyle(tv, getItem(position));
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                applyCustomStyle(tv, getItem(position));
                return view;
            }
            private void applyCustomStyle(TextView tv, String text) {
                int bgColor = Color.WHITE;
                int textColor = Color.BLACK;

                if ("COMPRADOR".equals(text)) {
                    bgColor = Color.parseColor("#B8E4C9");
                    textColor = Color.parseColor("#1D3B2F");
                } else if ("VENDEDOR".equals(text)) {
                    bgColor = Color.parseColor("#F7C6DA");
                    textColor = Color.parseColor("#3F1F2A");
                }

                tv.setBackgroundColor(bgColor);
                tv.setTextColor(textColor);
                tv.setPadding(30, 30, 30, 30);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                CircleArrowDrawable icon = new CircleArrowDrawable(bgColor, textColor);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                tv.setCompoundDrawablePadding(20);
            }
        };
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisputeRole.setAdapter(adapterRole);
        spinnerDisputeRole.setBackground(null);
        spinnerDisputeRole.setPadding(0, 0, 0, 0);

        layoutResolveList = view.findViewById(R.id.layout_resolve_list);
        containerDisputesButtons = view.findViewById(R.id.container_disputes_buttons);
        btnResolveListBack = view.findViewById(R.id.btn_resolve_list_back);

        layoutResolveAction = view.findViewById(R.id.layout_resolve_action);
        containerDisputeHistory = view.findViewById(R.id.container_dispute_history);
        btnGoToResolveForm = view.findViewById(R.id.btn_goto_resolve_form);
        btnReplyDispute = view.findViewById(R.id.btn_reply_dispute);
        btnRequestPlatformReview = view.findViewById(R.id.btn_request_platform_review);
        btnResolveActionBack = view.findViewById(R.id.btn_resolve_action_back);
        scrollDisputeHistory = view.findViewById(R.id.scroll_dispute_history);
        swipeRefreshDispute = view.findViewById(R.id.swipe_refresh_dispute);

        layoutFinalResolveForm = view.findViewById(R.id.layout_final_resolve_form);
        inputResolveId = view.findViewById(R.id.input_resolve_id);
        inputPlatformKey = view.findViewById(R.id.input_platform_key);
        spinnerResolveDecision = view.findViewById(R.id.spinner_resolve_decision);
        btnExecuteResolve = view.findViewById(R.id.btn_execute_resolve);
        btnResolveFinalBack = view.findViewById(R.id.btn_resolve_final_back);

        // Configuración personalizada del Spinner de Decisiones
        String[] decisionOptions = {"Devolver a Comprador", "Liberar a Vendedor"};
        ArrayAdapter<String> adapterDecision = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, decisionOptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                view.setLayoutParams(params);
                TextView tv = (TextView) view;
                applyCustomStyle(tv, getItem(position));
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                applyCustomStyle(tv, getItem(position));
                return view;
            }
            private void applyCustomStyle(TextView tv, String text) {
                int bgColor = Color.WHITE;
                int textColor = Color.BLACK;

                if (text.contains("Comprador")) {
                    bgColor = Color.parseColor("#B8E4C9");
                    textColor = Color.parseColor("#1D3B2F");
                } else if (text.contains("Vendedor")) {
                    bgColor = Color.parseColor("#F7C6DA");
                    textColor = Color.parseColor("#3F1F2A");
                }

                tv.setBackgroundColor(bgColor);
                tv.setTextColor(textColor);
                tv.setPadding(30, 30, 30, 30);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                CircleArrowDrawable icon = new CircleArrowDrawable(bgColor, textColor);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                tv.setCompoundDrawablePadding(20);
            }
        };
        adapterDecision.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolveDecision.setAdapter(adapterDecision);
        spinnerResolveDecision.setBackground(null);
        spinnerResolveDecision.setPadding(0, 0, 0, 0);
    }

    // Esta función se encarga de definir el comportamiento de los botones y elementos interactivos
    private void setupListeners() {
        btnRealizarOperacion.setOnClickListener(v -> {
            hideKeyboard();
            if (layoutMainMenu.getVisibility() == View.VISIBLE) {
                showView(null);
                btnRealizarOperacion.setText("Iniciar operaciones");
                tvResultadoLog.setText("");
                clearOperationState();
            } else {
                showView(layoutMainMenu);
                btnRealizarOperacion.setText("Cerrar operaciones");
                tvResultadoLog.setText("");
            }
        });

        // Configuración del gesto Pull-to-Refresh para recargar disputas
        swipeRefreshDispute.setOnRefreshListener(() -> {
            String currentId = inputResolveId.getText().toString();
            if (currentId != null && !currentId.isEmpty()) {
                loadAndRenderDisputes(currentId);
            } else {
                swipeRefreshDispute.setRefreshing(false);
            }
        });

        btnSelectBuyer.setOnClickListener(v -> { hideKeyboard(); showView(layoutBuyerMenu); autoFillKeys(); });
        btnSelectSeller.setOnClickListener(v -> { hideKeyboard(); showView(layoutSellerMenu); autoFillKeys(); tvResultadoLog.setText("VENDEDOR: Introduce tus datos para recibir el pago."); });
        btnSelectDispute.setOnClickListener(v -> { hideKeyboard(); showView(layoutDisputeMenu); tvResultadoLog.setText(""); });

        btnBuyerBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutMainMenu); tvResultadoLog.setText(""); });
        btnSellerBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutMainMenu); tvResultadoLog.setText(""); });
        btnDisputeBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutMainMenu); tvResultadoLog.setText(""); });

        btnBuyerNew.setOnClickListener(v -> { hideKeyboard(); showView(layoutFormsContainer); layoutCreateEscrow.setVisibility(View.VISIBLE); autoFillKeys(); tvResultadoLog.setText("PASO 1: Comprador inicia el depósito."); });
        btnBuyerExisting.setOnClickListener(v -> { hideKeyboard(); showView(layoutExistingEscrowBuyer); tvResultadoLog.setText(""); });
        btnFormBackBuyer.setOnClickListener(v -> { hideKeyboard(); showView(layoutBuyerMenu); tvResultadoLog.setText(""); });
        btnExistingBackBuyer.setOnClickListener(v -> { hideKeyboard(); showView(layoutBuyerMenu); tvResultadoLog.setText(""); });

        // Acciones que interactúan con Blockchain
        btnEnviarEscrow.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKey.getText().toString().trim();
            String seller = inputSeller.getText().toString().trim();
            if (pk.isEmpty() || seller.isEmpty()) return;
            performCreateEscrow(pk, seller);
        });

        btnSearchId.setOnClickListener(v -> { hideKeyboard(); if (lastTxHash != null) performSearchEscrowId(lastTxHash); });

        btnFundEscrow.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKeyFund.getText().toString().trim();
            if (pk.isEmpty()) { Toast.makeText(mContext, "Introduce tu clave privada", Toast.LENGTH_LONG).show(); return; }
            if (lastEscrowId == null) return;
            String amountStr = inputFundAmount.getText().toString().trim();
            try {
                performFundEscrow(pk, lastEscrowId, Convert.toWei(new BigDecimal(amountStr), Convert.Unit.ETHER).toBigInteger());
            } catch (Exception e) { Toast.makeText(mContext, "Monto inválido", Toast.LENGTH_SHORT).show(); }
        });

        btnBuyerApprove.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKey.getText().toString().trim();
            if(pk.isEmpty()) pk = inputPrivateKeyFund.getText().toString().trim();
            if (btnBuyerApprove.getText().toString().equals("Aprobar recepción")) {
                if (pk.isEmpty()) { Toast.makeText(mContext, "Introduce tu clave privada", Toast.LENGTH_LONG).show(); return; }
                if (lastEscrowId != null) performApprove(lastEscrowId, pk, "COMPRADOR");
            } else {
                performVerifyCompletion(pk, lastEscrowId);
            }
        });

        btnCheckEscrowStatus.setOnClickListener(v -> {
            hideKeyboard();
            String idStr = inputExistingEscrowId.getText().toString().trim();
            if (idStr.isEmpty()) return;
            String currentKey = "";
            if (getActivity() instanceof MainActivity) currentKey = ((MainActivity) getActivity()).getUsuarioClavePrivada();
            if (currentKey == null || currentKey.isEmpty()) {
                if (getContext() != null) {
                    SharedPreferences prefs = getContext().getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
                    currentKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
                }
            }
            performCheckStatus(currentKey, idStr);
        });

        btnSellerCheckStatus.setOnClickListener(v -> { hideKeyboard(); performSellerCheckOnly(inputSellerPrivateKey.getText().toString().trim(), new BigInteger(inputSellerEscrowId.getText().toString().trim())); });
        btnSellerReceivePayment.setOnClickListener(v -> { hideKeyboard(); performSellerCheckAndClaim(inputSellerPrivateKey.getText().toString().trim(), new BigInteger(inputSellerEscrowId.getText().toString().trim())); });

        // --- LÓGICA DE DISPUTAS ---
        btnOpenDisputeMenu.setOnClickListener(v -> {
            hideKeyboard();
            inputDisputeEscrowId.setText("");
            inputDisputeEscrowId.setEnabled(true);
            inputDisputeReason.setText("");
            showView(layoutOpenDisputeForm);
        });
        btnOpenDisputeBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutDisputeMenu); });

        btnSubmitDispute.setOnClickListener(v -> {
            hideKeyboard();
            String idStr = inputDisputeEscrowId.getText().toString().trim();
            String r = inputDisputeReason.getText().toString().trim();
            String role = spinnerDisputeRole.getSelectedItem().toString();

            if(idStr.isEmpty() || r.isEmpty() || currentUserEmail.isEmpty()) {
                Toast.makeText(mContext, "Rellene ID, Motivo y asegúrese de estar logueado.", Toast.LENGTH_LONG).show();
                return;
            }

            String myKey = "";
            if(!inputPrivateKey.getText().toString().isEmpty()) myKey = inputPrivateKey.getText().toString();
            else if(!inputSellerPrivateKey.getText().toString().isEmpty()) myKey = inputSellerPrivateKey.getText().toString();
            else if(!inputPrivateKeyFund.getText().toString().isEmpty()) myKey = inputPrivateKeyFund.getText().toString();

            if(myKey.isEmpty()) {
                SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
                myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
            }

            if(myKey.isEmpty()) {
                Toast.makeText(mContext, "Error: No se encuentra clave privada para validar la operación.", Toast.LENGTH_LONG).show();
                return;
            }

            final String finalKey = myKey;

            tvResultadoLog.setText("Verificando tu participación en el depósito...");
            btnSubmitDispute.setEnabled(false);

            // Verificación asíncrona contra Blockchain antes de registrar en BD
            new Thread(() -> {
                try {
                    Credentials credentials = Credentials.create(finalKey);
                    String myAddress = credentials.getAddress();

                    FairPayService service = new FairPayService(finalKey);
                    List<Type> details = service.getEscrowDetails(new BigInteger(idStr));

                    String contractBuyer = details.get(0).getValue().toString();
                    String contractSeller = details.get(1).getValue().toString();

                    boolean isBuyer = myAddress.equalsIgnoreCase(contractBuyer);
                    boolean isSeller = myAddress.equalsIgnoreCase(contractSeller);

                    if (!isBuyer && !isSeller) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                tvResultadoLog.setText("");
                                btnSubmitDispute.setEnabled(true);
                                Toast.makeText(mContext, "ERROR: Tu wallet no participa en el depósito " + idStr, Toast.LENGTH_LONG).show();
                            });
                        }
                        return;
                    }

                    if (isBuyer && !role.equalsIgnoreCase("COMPRADOR")) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                tvResultadoLog.setText("");
                                btnSubmitDispute.setEnabled(true);
                                Toast.makeText(mContext, "ERROR: En este depósito eres el COMPRADOR. Cambia el rol.", Toast.LENGTH_LONG).show();
                            });
                        }
                        return;
                    }

                    if (isSeller && !role.equalsIgnoreCase("VENDEDOR")) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                tvResultadoLog.setText("");
                                btnSubmitDispute.setEnabled(true);
                                Toast.makeText(mContext, "ERROR: En este depósito eres el VENDEDOR. Cambia el rol.", Toast.LENGTH_LONG).show();
                            });
                        }
                        return;
                    }

                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            tvResultadoLog.setText("Validación OK. Registrando mensaje...");
                            dbConnection.abrirDisputa(currentUserEmail, idStr, role, r, contractBuyer, contractSeller, new DatabaseConnection.OperacionListener() {
                                @Override
                                public void onOperacionExito(String mensaje) {
                                    FragmentActivity act = getActivity();
                                    if (act != null) {
                                        act.runOnUiThread(() -> {
                                            Toast.makeText(mContext, mensaje, Toast.LENGTH_LONG).show();

                                            inputDisputeReason.setText("");
                                            inputDisputeEscrowId.setText("");
                                            inputDisputeEscrowId.setEnabled(true);
                                            btnSubmitDispute.setEnabled(true);
                                            tvResultadoLog.setText("");

                                            loadAndRenderDisputes(idStr);
                                        });
                                    }
                                }
                                @Override
                                public void onOperacionFallo(String error) {
                                    FragmentActivity act = getActivity();
                                    if (act != null) {
                                        act.runOnUiThread(() -> {
                                            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
                                            btnSubmitDispute.setEnabled(true);
                                        });
                                    }
                                }
                            });
                        });
                    }

                } catch (Exception e) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            tvResultadoLog.setText("");
                            btnSubmitDispute.setEnabled(true);
                            Toast.makeText(mContext, "Error al verificar ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        });

        btnResolveDisputeListMenu.setOnClickListener(v -> { hideKeyboard(); loadAndRenderDisputes(null); });
        btnResolveListBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutDisputeMenu); });

        btnGoToResolveForm.setOnClickListener(v -> {
            btnGoToResolveForm.setVisibility(View.GONE);
            btnReplyDispute.setVisibility(View.GONE);
            btnRequestPlatformReview.setVisibility(View.GONE);
            btnResolveActionBack.setVisibility(View.GONE);
            layoutFinalResolveForm.setVisibility(View.VISIBLE);
            autoFillKeys();
        });

        btnReplyDispute.setOnClickListener(v -> {
            hideKeyboard();
            String idActual = inputResolveId.getText().toString();
            if (idActual.isEmpty()) return;
            performCheckRoleAndOpenForm(idActual);
        });

        // Diálogo de confirmación para solicitar revisión manual a la plataforma
        btnRequestPlatformReview.setOnClickListener(v -> {
            String idActual = inputResolveId.getText().toString();
            if (idActual.isEmpty()) return;

            new AlertDialog.Builder(mContext)
                    .setTitle("Solicitar Revisión")
                    .setMessage("¿Estás seguro? Ya no se podrá enviar más mensajes, y solicitarás a FairPay que emita una resolución. Esta acción es irreversible.")
                    .setPositiveButton("Sí, solicitar", (dialog, which) -> {
                        dbConnection.solicitarRevision(idActual, new DatabaseConnection.OperacionListener() {
                            @Override
                            public void onOperacionExito(String mensaje) {
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(() -> {
                                        Toast.makeText(mContext, mensaje, Toast.LENGTH_LONG).show();
                                        loadAndRenderDisputes(idActual);
                                    });
                                }
                            }
                            @Override
                            public void onOperacionFallo(String error) {
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(() -> Toast.makeText(mContext, "Error: " + error, Toast.LENGTH_SHORT).show());
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        btnResolveActionBack.setOnClickListener(v -> { hideKeyboard(); showView(layoutResolveList); });

        btnResolveFinalBack.setOnClickListener(v -> {
            hideKeyboard();
            layoutFinalResolveForm.setVisibility(View.GONE);
            containerDisputeHistory.setVisibility(View.VISIBLE);
            if(scrollDisputeHistory != null) scrollDisputeHistory.setVisibility(View.VISIBLE);
            btnGoToResolveForm.setVisibility(View.VISIBLE);

            if (!currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL)) {
                btnReplyDispute.setVisibility(View.VISIBLE);
            }
            btnResolveActionBack.setVisibility(View.VISIBLE);
        });

        btnExecuteResolve.setOnClickListener(v -> {
            String pk = inputPlatformKey.getText().toString();
            String id = inputResolveId.getText().toString();
            String decision = spinnerResolveDecision.getSelectedItem().toString();
            boolean refundBuyer = decision.contains("Comprador");

            if(!pk.isEmpty() && !id.isEmpty() && currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL)) {
                performResolveDispute(pk, new BigInteger(id), refundBuyer, decision);
            } else {
                Toast.makeText(mContext, "Acceso denegado: Solo la Plataforma puede resolver.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- MÉTODOS UI AUXILIARES ---

    // Esta función se encarga de validar el rol del usuario en la Blockchain antes de permitirle contestar
    private void performCheckRoleAndOpenForm(String idStr) {
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");

        if (myKey.isEmpty()) {
            Toast.makeText(mContext, "No se encontró clave privada. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        btnReplyDispute.setEnabled(false);
        btnReplyDispute.setText("Cargando...");

        new Thread(() -> {
            try {
                Credentials credentials = Credentials.create(myKey);
                String myAddress = credentials.getAddress();

                FairPayService service = new FairPayService(myKey);
                List<Type> details = service.getEscrowDetails(new BigInteger(idStr));

                String contractBuyer = details.get(0).getValue().toString();
                String contractSeller = details.get(1).getValue().toString();
                BigInteger approvals = (BigInteger) details.get(4).getValue();

                boolean isBuyer = myAddress.equalsIgnoreCase(contractBuyer);
                boolean isSeller = myAddress.equalsIgnoreCase(contractSeller);
                boolean isResolved = approvals.compareTo(BigInteger.ZERO) > 0;

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        btnReplyDispute.setEnabled(true);
                        btnReplyDispute.setText("Responder / Añadir comentario");

                        if (isResolved) {
                            Toast.makeText(mContext, "Esta disputa ya está resuelta. No se admiten más mensajes.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (isBuyer) {
                            openDisputeFormPreFilled(idStr, 0);
                        } else if (isSeller) {
                            openDisputeFormPreFilled(idStr, 1);
                        } else {
                            Toast.makeText(mContext, "Tu wallet no participa en este depósito.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        btnReplyDispute.setEnabled(true);
                        btnReplyDispute.setText("Responder / Añadir comentario");
                        Toast.makeText(mContext, "Error al verificar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void openDisputeFormPreFilled(String id, int spinnerIndex) {
        showView(layoutOpenDisputeForm);
        inputDisputeEscrowId.setText(id);
        inputDisputeEscrowId.setEnabled(false);
        spinnerDisputeRole.setSelection(spinnerIndex);
        inputDisputeReason.setText("");
        inputDisputeReason.requestFocus();
    }

    // Esta función se encarga de obtener las disputas desde la base de datos y mostrarlas en la lista
    private void loadAndRenderDisputes(@Nullable String targetIdToOpen) {
        if (currentUserEmail.isEmpty()) {
            tvResultadoLog.setText("Error: No se puede cargar la lista sin email de usuario.");
            return;
        }

        tvResultadoLog.setText("Cargando disputas...");

        dbConnection.obtenerDisputas(currentUserEmail, new DatabaseConnection.DataListener<Map<String, String>>() {
            @Override
            public void onDataSuccess(List<Map<String, String>> data) {
                groupedDisputes.clear();
                for (Map<String, String> dispute : data) {
                    String escrowId = dispute.get("escrowId");
                    if (!groupedDisputes.containsKey(escrowId)) {
                        groupedDisputes.put(escrowId, new ArrayList<>());
                    }
                    groupedDisputes.get(escrowId).add(dispute);
                }

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        renderDisputesButtons();
                        if (targetIdToOpen != null && groupedDisputes.containsKey(targetIdToOpen)) {
                            showOpenDisputeDetails(targetIdToOpen);
                        }

                        if (swipeRefreshDispute != null) {
                            swipeRefreshDispute.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onDataFailure(String error) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        tvResultadoLog.setText("Error al cargar disputas: " + error);
                        if (swipeRefreshDispute != null) {
                            swipeRefreshDispute.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    // Esta función se encarga de generar los botones de la lista de disputas dinámicamente
    private void renderDisputesButtons() {
        showView(layoutResolveList);
        containerDisputesButtons.removeAllViews();
        tvResultadoLog.setText("");

        if (groupedDisputes.isEmpty()) {
            tvResultadoLog.setText("No hay disputas abiertas asociadas a tu cuenta.");
            return;
        }

        for (String id : groupedDisputes.keySet()) {
            List<Map<String, String>> entries = groupedDisputes.get(id);
            if (entries == null || entries.isEmpty()) continue;

            Map<String, String> firstEntry = entries.get(0);
            String rolCreador = firstEntry.get("rol");

            Map<String, String> latestEntry = entries.get(entries.size() - 1);
            String estado = latestEntry.get("estado");
            String decision = latestEntry.get("decision");

            Button btn = new Button(mContext);

            String buttonText = "";
            int bgColor;
            int textColor;

            if (estado.equalsIgnoreCase("RESUELTA")) {
                buttonText = "ID Depósito: " + id + " (RESUELTA: " + decision + ")";
                if (decision != null && decision.toUpperCase().contains("COMPRADOR")) {
                    bgColor = Color.parseColor("#B8E4C9");
                    textColor = Color.parseColor("#1D3B2F");
                } else if (decision != null && decision.toUpperCase().contains("VENDEDOR")) {
                    bgColor = Color.parseColor("#F7C6DA");
                    textColor = Color.parseColor("#3F1F2A");
                } else {
                    bgColor = Color.parseColor("#009688");
                    textColor = Color.WHITE;
                }
            } else if (estado.equalsIgnoreCase("REVISION")) {
                buttonText = "ID Depósito: " + id + " (EN REVISIÓN)";
                bgColor = Color.parseColor("#FF9800");
                textColor = Color.WHITE;
            } else {
                if (currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL)) {
                    buttonText = "ID Depósito: " + id + " (PENDIENTE PLATAFORMA)";
                    bgColor = Color.parseColor("#FF9800");
                    textColor = Color.WHITE;
                } else {
                    if (rolCreador != null && rolCreador.equalsIgnoreCase("COMPRADOR")) {
                        bgColor = Color.parseColor("#B8E4C9");
                        textColor = Color.parseColor("#1D3B2F");
                        buttonText = "ID Depósito: " + id + " Disputa del comprador";
                    } else if (rolCreador != null && rolCreador.equalsIgnoreCase("VENDEDOR")) {
                        bgColor = Color.parseColor("#F7C6DA");
                        textColor = Color.parseColor("#3F1F2A");
                        buttonText = "ID Depósito: " + id + " Disputa del vendedor";
                    } else {
                        bgColor = Color.parseColor("#103044");
                        textColor = Color.parseColor("#AEDDFF");
                        buttonText = "ID Depósito: " + id + " (ABIERTAS: " + entries.size() + " msg)";
                    }
                }
            }

            btn.setText(buttonText);
            btn.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            btn.setTextColor(textColor);
            btn.setAllCaps(false);

            containerDisputesButtons.addView(btn);
            btn.setOnClickListener(btnView -> showOpenDisputeDetails(id));
        }
    }

    // Esta función se encarga de mostrar el detalle del chat y las acciones disponibles para una disputa
    private void showOpenDisputeDetails(String escrowId) {
        showView(layoutResolveAction);
        containerDisputeHistory.removeAllViews();

        btnGoToResolveForm.setVisibility(View.VISIBLE);
        btnReplyDispute.setVisibility(View.VISIBLE);
        btnRequestPlatformReview.setVisibility(View.GONE);
        btnResolveActionBack.setVisibility(View.VISIBLE);
        layoutFinalResolveForm.setVisibility(View.GONE);
        if(scrollDisputeHistory != null) scrollDisputeHistory.setVisibility(View.VISIBLE);
        inputResolveId.setText(escrowId);

        List<Map<String, String>> entries = groupedDisputes.get(escrowId);
        boolean isResolved = false;
        boolean isUnderReview = false;
        String finalDecision = "";
        String creatorRole = "";

        if (entries != null && !entries.isEmpty()) {
            creatorRole = entries.get(0).get("rol");

            for (Map<String, String> entry : entries) {
                String rol = entry.get("rol");
                String motivo = entry.get("motivo");
                String estado = entry.get("estado");
                String decision = entry.get("decision");

                TextView msgView = new TextView(mContext);
                msgView.setText(Html.fromHtml("<b>" + rol + ":</b> " + motivo));
                msgView.setTextSize(16f);

                int bgColor = Color.WHITE;
                int textColor = Color.BLACK;

                if (rol != null && rol.equalsIgnoreCase("COMPRADOR")) {
                    bgColor = Color.parseColor("#B8E4C9");
                    textColor = Color.parseColor("#1D3B2F");
                } else if (rol != null && rol.equalsIgnoreCase("VENDEDOR")) {
                    bgColor = Color.parseColor("#F7C6DA");
                    textColor = Color.parseColor("#3F1F2A");
                }

                msgView.setBackgroundColor(bgColor);
                msgView.setTextColor(textColor);
                msgView.setPadding(30, 30, 30, 30);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 20);
                msgView.setLayoutParams(params);

                containerDisputeHistory.addView(msgView);

                if (estado != null && estado.equalsIgnoreCase("RESUELTA")) {
                    isResolved = true;
                    finalDecision = decision;
                } else if (estado != null && estado.equalsIgnoreCase("REVISION")) {
                    isUnderReview = true;
                }
            }

            if (isResolved) {
                TextView resView = new TextView(mContext);
                resView.setText("RESOLUCIÓN FINAL: " + finalDecision);
                resView.setBackgroundColor(Color.BLACK);
                resView.setTextColor(Color.WHITE);
                resView.setPadding(30,30,30,30);
                resView.setGravity(android.view.Gravity.CENTER);

                LinearLayout.LayoutParams resParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                resParams.setMargins(0, 20, 0, 0);
                resView.setLayoutParams(resParams);

                containerDisputeHistory.addView(resView);
            }
            else if (isUnderReview) {
                TextView revView = new TextView(mContext);
                revView.setText("En revisión...");
                revView.setBackgroundColor(Color.parseColor("#FF9800"));
                revView.setTextColor(Color.WHITE);
                revView.setPadding(30,30,30,30);
                revView.setGravity(android.view.Gravity.CENTER);

                LinearLayout.LayoutParams revParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                revParams.setMargins(0, 20, 0, 0);
                revView.setLayoutParams(revParams);

                containerDisputeHistory.addView(revView);

                btnReplyDispute.setVisibility(View.GONE);
            }
        }

        if(scrollDisputeHistory != null) {
            scrollDisputeHistory.post(() -> scrollDisputeHistory.fullScroll(View.FOCUS_DOWN));
        }

        if (currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL)) {
            btnGoToResolveForm.setVisibility(isResolved ? View.GONE : View.VISIBLE);
            btnReplyDispute.setVisibility(View.GONE);
            btnRequestPlatformReview.setVisibility(View.GONE);
        } else {
            btnGoToResolveForm.setVisibility(View.GONE);
            btnReplyDispute.setVisibility((isResolved || isUnderReview) ? View.GONE : View.VISIBLE);

            if (!isResolved && !isUnderReview) {
                checkIfUserIsCreatorAndShowButton(escrowId, creatorRole);
            } else {
                btnRequestPlatformReview.setVisibility(View.GONE);
            }
        }
    }

    // Esta función se encarga de verificar si el usuario es el creador de la disputa para permitirle solicitar revisión
    private void checkIfUserIsCreatorAndShowButton(String escrowId, String creatorRole) {
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (myKey.isEmpty()) return;

        new Thread(() -> {
            try {
                Credentials credentials = Credentials.create(myKey);
                String myAddress = credentials.getAddress();

                FairPayService service = new FairPayService(myKey);
                List<Type> details = service.getEscrowDetails(new BigInteger(escrowId));

                String contractBuyer = details.get(0).getValue().toString();
                String contractSeller = details.get(1).getValue().toString();

                boolean isBuyer = myAddress.equalsIgnoreCase(contractBuyer);
                boolean isSeller = myAddress.equalsIgnoreCase(contractSeller);

                boolean iAmTheCreator = false;
                if (isBuyer && "COMPRADOR".equalsIgnoreCase(creatorRole)) iAmTheCreator = true;
                if (isSeller && "VENDEDOR".equalsIgnoreCase(creatorRole)) iAmTheCreator = true;

                if (iAmTheCreator) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> btnRequestPlatformReview.setVisibility(View.VISIBLE));
                    }
                }
            } catch (Exception e) {
            }
        }).start();
    }

    private void hideKeyboard() { if (getActivity() != null) { View v = getActivity().getCurrentFocus(); if (v != null) { InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); imm.hideSoftInputFromWindow(v.getWindowToken(), 0); } } }

    private void showView(View view) {
        layoutMainMenu.setVisibility(View.GONE); layoutBuyerMenu.setVisibility(View.GONE); layoutSellerMenu.setVisibility(View.GONE); layoutDisputeMenu.setVisibility(View.GONE);
        layoutFormsContainer.setVisibility(View.GONE); layoutExistingEscrowBuyer.setVisibility(View.GONE); layoutOpenDisputeForm.setVisibility(View.GONE);
        layoutResolveList.setVisibility(View.GONE); layoutResolveAction.setVisibility(View.GONE);
        layoutCreateEscrow.setVisibility(View.GONE); layoutFundEscrow.setVisibility(View.GONE); layoutBuyerApprove.setVisibility(View.GONE);
        if (view != null) view.setVisibility(View.VISIBLE);
    }

    // --- MÉTODOS DE INTERACCIÓN CON BLOCKCHAIN ---

    // Esta función se encarga de ejecutar la resolución final de una disputa en la Blockchain (solo Admin)
    private void performResolveDispute(String pk, BigInteger id, boolean refundBuyer, String decisionText) {
        tvResultadoLog.setText("Resolviendo..."); btnExecuteResolve.setEnabled(false);
        String decisionFinal = refundBuyer ? "COMPRADOR" : "VENDEDOR";

        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.resolveDispute(id, refundBuyer);

                dbConnection.marcarDisputaResuelta(id.toString(), decisionFinal, new DatabaseConnection.OperacionListener() {
                    @Override
                    public void onOperacionExito(String mensaje) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                tvResultadoLog.setText("FINALIZADO: Hash: " + hash + "\n" + mensaje);
                                btnExecuteResolve.setEnabled(true);
                                loadAndRenderDisputes(id.toString());
                            });
                        }
                    }
                    @Override
                    public void onOperacionFallo(String error) {
                        FragmentActivity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                tvResultadoLog.setText("Error en BD (Contrato OK): " + error);
                                btnExecuteResolve.setEnabled(true);
                            });
                        }
                    }
                });
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        tvResultadoLog.setText("Error BlockChain: " + e.getMessage());
                        btnExecuteResolve.setEnabled(true);
                    });
                }
            }
        }).start();
    }

    // Esta función se encarga de interactuar con el contrato inteligente para crear un nuevo depósito de garantía
    private void performCreateEscrow(String pk, String seller) {
        tvResultadoLog.setText("Creando..."); btnEnviarEscrow.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.createEscrow(seller);
                lastTxHash = hash;
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        String msg = "Hash: " + hash + "\nEsperando...";
                        tvResultadoLog.setText(msg);
                        startCountdownTimer();
                        btnEnviarEscrow.setEnabled(true);
                        saveOperationState(STATE_WAITING_ID, hash, null, msg);
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnEnviarEscrow.setEnabled(true); });
                }
            }
        }).start();
    }

    private void startCountdownTimer() {
        btnSearchId.setVisibility(View.VISIBLE); btnSearchId.setEnabled(false);
        new CountDownTimer(15000, 1000) {
            public void onTick(long m) { btnSearchId.setText("Procesando... " + m/1000); }
            public void onFinish() { btnSearchId.setText(SEARCH_BUTTON_ORIGINAL_TEXT); btnSearchId.setEnabled(true); }
        }.start();
    }

    // Esta función se encarga de sondear la Blockchain hasta encontrar el ID del depósito recién creado
    private void performSearchEscrowId(String hash) {
        tvResultadoLog.setText("Buscando ID..."); btnSearchId.setEnabled(false);
        new Thread(() -> {
            try {
                String pk = inputPrivateKey.getText().toString();
                if(pk.isEmpty()) pk = "0x0000000000000000000000000000000000000000000000000000000000000001";
                FairPayService service = new FairPayService(pk);
                BigInteger id = service.waitForEscrowId(hash);
                lastEscrowId = id;
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        String msg = "ID encontrado: " + id + "\nIntroduce clave privada para pagar.";
                        tvResultadoLog.setText(msg);
                        inputEscrowId.setText(id.toString());
                        autoFillKeys();
                        layoutCreateEscrow.setVisibility(View.GONE);
                        layoutFundEscrow.setVisibility(View.VISIBLE);
                        btnSearchId.setVisibility(View.GONE);
                        saveOperationState(STATE_WAITING_FUND, hash, id.toString(), msg);
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnSearchId.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de enviar los fondos (ETH) al contrato inteligente para asegurar el depósito
    private void performFundEscrow(String pk, BigInteger id, BigInteger amountWei) {
        tvResultadoLog.setText("Enviando pago..."); btnFundEscrow.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.fundEscrow(id, amountWei);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        tvResultadoLog.setText("Pago enviado. Confirmando...");
                        new CountDownTimer(19000, 1000) {
                            public void onTick(long m) { btnFundEscrow.setText("Procesando... " + m/1000); }
                            public void onFinish() {
                                String msg = "Pago confirmado. Pendiente aprobar.";
                                btnFundEscrow.setText("Hacer pago");
                                btnFundEscrow.setEnabled(true);
                                tvResultadoLog.setText(msg);
                                layoutFundEscrow.setVisibility(View.GONE);
                                layoutBuyerApprove.setVisibility(View.VISIBLE);
                                btnBuyerApprove.setText("Aprobar recepción");
                                saveOperationState(STATE_WAITING_APPROVE, hash, id.toString(), msg);
                            }
                        }.start();
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnFundEscrow.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de autorizar la liberación de fondos (aprobar) una vez cumplidas las condiciones
    private void performApprove(BigInteger id, String pk, String rol) {
        tvResultadoLog.setText("Aprobando...");
        Button btn = rol.equals("COMPRADOR") ? btnBuyerApprove : btnSellerReceivePayment;
        btn.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.approveRelease(id);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        tvResultadoLog.setText("Aprobación enviada. Hash: " + hash);
                        if (rol.equals("COMPRADOR")) {
                            saveOperationState(STATE_WAITING_VERIFY, hash, id.toString(), "Aprobación enviada. Pendiente verificación.");
                        }
                        new CountDownTimer(19000, 1000) {
                            public void onTick(long m) { btn.setText("Procesando... " + m/1000); }
                            public void onFinish() {
                                btn.setText(rol.equals("VENDEDOR") ? "Recibir pago" : "Comprobar");
                                btn.setEnabled(true);
                                if(rol.equals("VENDEDOR")) tvResultadoLog.setText("FINALIZADO.");
                            }
                        }.start();
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btn.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de verificar si la transacción ha sido completada consultando el estado en la Blockchain
    private void performVerifyCompletion(String pk, BigInteger id) {
        if (id == null) {
            tvResultadoLog.setText("Error: No hay ID de operación activo.\nRecarga la operación.");
            return;
        }

        tvResultadoLog.setText("Verificando..."); btnBuyerApprove.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                List<Type> details = service.getEscrowDetails(id);
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        btnBuyerApprove.setEnabled(true);
                        if (approvals.compareTo(BigInteger.ZERO) > 0) {
                            showView(null);
                            tvResultadoLog.setVisibility(View.VISIBLE);
                            tvResultadoLog.setText("FINALIZADO: Depósito completado.");
                            clearOperationState();
                        }
                        else { tvResultadoLog.setText("Aún no confirmado."); }
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnBuyerApprove.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de recuperar el estado actual de un depósito existente mediante su ID
    private void performCheckStatus(String pk, String idStr) {
        btnCheckEscrowStatus.setEnabled(false);
        new Thread(() -> {
            try {
                BigInteger id = new BigInteger(idStr);
                lastEscrowId = id;
                String tempPk = pk.isEmpty() ? "0x0000000000000000000000000000000000000000000000000000000000000001" : pk;
                FairPayService service = new FairPayService(tempPk);
                List<Type> details = service.getEscrowDetails(id);
                BigInteger amount = (BigInteger) details.get(2).getValue();
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        btnCheckEscrowStatus.setEnabled(true);

                        if (btnRealizarOperacion != null) {
                            btnRealizarOperacion.setVisibility(View.VISIBLE);
                            btnRealizarOperacion.setText("Cerrar operaciones");
                        }

                        if (isFunded && approvals.compareTo(BigInteger.ZERO) > 0) { showView(null); tvResultadoLog.setVisibility(View.VISIBLE); tvResultadoLog.setText("Estado finalizado:\n\nSu depósito ya fue creado, pagado y aprobado."); return; }
                        showView(layoutFormsContainer); layoutExistingEscrowBuyer.setVisibility(View.GONE); layoutCreateEscrow.setVisibility(View.GONE);
                        if (!isFunded && amount.equals(BigInteger.ZERO)) {
                            tvResultadoLog.setText(Html.fromHtml("Estado: Creado pero no pagado.<br><br><i>Para continuar:</i> Introduce tu clave privada y pulsa <b>Hacer pago</b>."));
                            layoutFundEscrow.setVisibility(View.VISIBLE); inputEscrowId.setText(idStr); btnFundEscrow.setVisibility(View.VISIBLE); btnFundEscrow.setEnabled(true);
                        } else if (isFunded && approvals.equals(BigInteger.ZERO)) {
                            tvResultadoLog.setText(Html.fromHtml("Estado: Pagado. Pendiente de aprobación.<br><br><i>Para continuar:</i> Introduce tu clave privada y pulsa <b>Aprobar recepción</b>."));
                            layoutBuyerApprove.setVisibility(View.VISIBLE); btnBuyerApprove.setVisibility(View.VISIBLE); btnBuyerApprove.setEnabled(true); btnBuyerApprove.setText("Aprobar recepción");
                        } else { tvResultadoLog.setText("Estado: Desconocido o ID inválido."); showView(layoutMainMenu); }
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnCheckEscrowStatus.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de consultar si un vendedor tiene un pago pendiente
    private void performSellerCheckOnly(String pk, BigInteger id) {
        tvResultadoLog.setText("Consultando..."); btnSellerCheckStatus.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk.isEmpty() ? "0x0000000000000000000000000000000000000000000000000000000000000001" : pk);
                List<Type> details = service.getEscrowDetails(id);
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        btnSellerCheckStatus.setEnabled(true);
                        if (isFunded && approvals.equals(BigInteger.ONE)) tvResultadoLog.setText("Listo para recibir pago.");
                        else if (!isFunded) tvResultadoLog.setText("No pagado aún.");
                        else tvResultadoLog.setText("Pagado, esperando aprobación.");
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnSellerCheckStatus.setEnabled(true); });
                }
            }
        }).start();
    }

    // Esta función se encarga de que el vendedor reclame su pago aprobando la transacción desde su lado
    private void performSellerCheckAndClaim(String pk, BigInteger id) {
        tvResultadoLog.setText("Reclamando...");
        btnSellerReceivePayment.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                List<Type> details = service.getEscrowDetails(id);
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (isFunded && approvals.equals(BigInteger.ONE)) {
                            tvResultadoLog.setText("Reclamando..."); btnSellerReceivePayment.setEnabled(true);
                            performApprove(id, pk, "VENDEDOR");
                        } else {
                            tvResultadoLog.setText("No puedes reclamar aún."); btnSellerReceivePayment.setEnabled(true);
                        }
                    });
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> { tvResultadoLog.setText("Error: " + e.getMessage()); btnSellerReceivePayment.setEnabled(true); });
                }
            }
        }).start();
    }

    // --- CLASES INTERNAS (Visuales) ---
    private class CircleArrowDrawable extends Drawable {
        private final Paint circlePaint;
        private final Paint arrowPaint;
        private final Path arrowPath;
        private final int size;

        public CircleArrowDrawable(int bgColor, int arrowColor) {
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setColor(bgColor);
            arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            arrowPaint.setColor(arrowColor);
            arrowPaint.setStyle(Paint.Style.FILL);
            arrowPath = new Path();
            size = (int) (24 * getResources().getDisplayMetrics().density);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            float cx = getBounds().exactCenterX();
            float cy = getBounds().exactCenterY();
            float radius = Math.min(getBounds().width(), getBounds().height()) / 2f;
            canvas.drawCircle(cx, cy, radius, circlePaint);
            float triangleSize = radius * 0.8f;
            arrowPath.reset();
            float halfW = triangleSize / 2f;
            float halfH = triangleSize / 2f;
            arrowPath.moveTo(cx - halfW, cy - halfH/2);
            arrowPath.lineTo(cx + halfW, cy - halfH/2);
            arrowPath.lineTo(cx, cy + halfH);
            arrowPath.close();
            canvas.drawPath(arrowPath, arrowPaint);
        }

        @Override
        public void setAlpha(int alpha) { circlePaint.setAlpha(alpha); arrowPaint.setAlpha(alpha); }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) { circlePaint.setColorFilter(colorFilter); arrowPaint.setColorFilter(colorFilter); }

        @Override
        public int getOpacity() { return PixelFormat.TRANSLUCENT; }

        @Override
        public int getIntrinsicWidth() { return size; }

        @Override
        public int getIntrinsicHeight() { return size; }
    }
}