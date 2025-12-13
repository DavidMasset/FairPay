package es.ifp.fairpay.ui.main.operations;

import android.app.AlertDialog;
import android.content.Context;
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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.web3j.crypto.Credentials;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ifp.fairpay.R;
import es.ifp.fairpay.data.database.DatabaseConnection;
import es.ifp.fairpay.data.service.FairPayService;

public class DisputasFragment extends Fragment {

    private Button btnOpenDisputeMenu, btnResolveDisputeListMenu, btnDisputeBack;
    private LinearLayout layoutDisputeMenu, layoutOpenDisputeForm, layoutResolveList, layoutResolveAction, layoutFinalResolveForm;
    private EditText inputDisputeEscrowId, inputDisputeReason, inputResolveId, inputPlatformKey;
    private Spinner spinnerDisputeRole, spinnerResolveDecision;
    private Button btnSubmitDispute, btnOpenDisputeBack, btnResolveListBack, btnGoToResolveForm, btnReplyDispute, btnRequestPlatformReview, btnResolveActionBack, btnExecuteResolve, btnResolveFinalBack;
    private LinearLayout containerDisputesButtons, containerDisputeHistory;
    private ScrollView scrollDisputeHistory;
    private SwipeRefreshLayout swipeRefreshDispute;
    private TextView tvLog;

    private DatabaseConnection dbConnection;
    private Context mContext;
    private String currentUserEmail = "";
    private final String PLATFORM_EMAIL = "firewire80a@gmail.com";
    private Map<String, List<Map<String, String>>> groupedDisputes = new HashMap<>();

    public DisputasFragment() {
        dbConnection = new DatabaseConnection();
    }

    // Esta función se encarga de adjuntar el contexto del fragmento para asegurar el acceso a recursos
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // Esta función se encarga de inflar el diseño visual del fragmento y cargar el email del usuario actual
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disputas, container, false);

        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        currentUserEmail = prefs.getString("CURRENT_USER_EMAIL", "");

        setupViews(view);
        setupListeners();
        setupSpinners();

        return view;
    }

    // Esta función se encarga de vincular los elementos visuales del XML con las variables de la clase
    private void setupViews(View view) {
        layoutDisputeMenu = view.findViewById(R.id.layout_dispute_menu);
        layoutOpenDisputeForm = view.findViewById(R.id.layout_open_dispute_form);
        layoutResolveList = view.findViewById(R.id.layout_resolve_list);
        layoutResolveAction = view.findViewById(R.id.layout_resolve_action);
        layoutFinalResolveForm = view.findViewById(R.id.layout_final_resolve_form);

        btnOpenDisputeMenu = view.findViewById(R.id.btn_open_dispute_menu);
        btnResolveDisputeListMenu = view.findViewById(R.id.btn_resolve_dispute_list_menu);
        btnDisputeBack = view.findViewById(R.id.btn_dispute_back);

        inputDisputeEscrowId = view.findViewById(R.id.input_dispute_escrow_id);
        inputDisputeReason = view.findViewById(R.id.input_dispute_reason);
        spinnerDisputeRole = view.findViewById(R.id.spinner_dispute_role);
        btnSubmitDispute = view.findViewById(R.id.btn_submit_dispute);
        btnOpenDisputeBack = view.findViewById(R.id.btn_open_dispute_back);

        containerDisputesButtons = view.findViewById(R.id.container_disputes_buttons);
        btnResolveListBack = view.findViewById(R.id.btn_resolve_list_back);

        containerDisputeHistory = view.findViewById(R.id.container_dispute_history);
        swipeRefreshDispute = view.findViewById(R.id.swipe_refresh_dispute);
        scrollDisputeHistory = view.findViewById(R.id.scroll_dispute_history);
        btnGoToResolveForm = view.findViewById(R.id.btn_goto_resolve_form);
        btnReplyDispute = view.findViewById(R.id.btn_reply_dispute);
        btnRequestPlatformReview = view.findViewById(R.id.btn_request_platform_review);
        btnResolveActionBack = view.findViewById(R.id.btn_resolve_action_back);

        inputResolveId = view.findViewById(R.id.input_resolve_id);
        inputPlatformKey = view.findViewById(R.id.input_platform_key);
        spinnerResolveDecision = view.findViewById(R.id.spinner_resolve_decision);
        btnExecuteResolve = view.findViewById(R.id.btn_execute_resolve);
        btnResolveFinalBack = view.findViewById(R.id.btn_resolve_final_back);

        tvLog = view.findViewById(R.id.tv_resultado_log);
    }

    // Esta función se encarga de configurar los eventos de clic para los botones de las distintas secciones
    private void setupListeners() {
        btnDisputeBack.setOnClickListener(v -> {
            hideKeyboard();
            if (getParentFragment() instanceof OperacionesFragment) {
                ((OperacionesFragment) getParentFragment()).resetUI();
            }
        });

        btnOpenDisputeMenu.setOnClickListener(v -> {
            hideKeyboard();
            inputDisputeEscrowId.setText("");
            inputDisputeEscrowId.setEnabled(true);
            showView(layoutOpenDisputeForm);
        });

        btnResolveDisputeListMenu.setOnClickListener(v -> {
            hideKeyboard();
            loadAndRenderDisputes(null);
        });

        // Listener para actualizar al deslizar hacia abajo
        swipeRefreshDispute.setOnRefreshListener(() -> {
            String currentId = inputResolveId.getText().toString();
            // Si estamos viendo un detalle, recargamos y volvemos a ese detalle, si no, solo la lista
            loadAndRenderDisputes(!currentId.isEmpty() ? currentId : null);
        });

        btnOpenDisputeBack.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutDisputeMenu);
        });

        btnResolveListBack.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutDisputeMenu);
        });

        btnResolveActionBack.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutResolveList);
        });

        btnGoToResolveForm.setOnClickListener(v -> {
            hideKeyboard();
            btnGoToResolveForm.setVisibility(View.GONE);
            btnReplyDispute.setVisibility(View.GONE);
            btnRequestPlatformReview.setVisibility(View.GONE);
            btnResolveActionBack.setVisibility(View.GONE);
            layoutFinalResolveForm.setVisibility(View.VISIBLE);
            autoFillKeys();
        });

        btnResolveFinalBack.setOnClickListener(v -> {
            hideKeyboard();
            layoutFinalResolveForm.setVisibility(View.GONE);
            btnGoToResolveForm.setVisibility(View.VISIBLE);
            if (!currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL)) {
                btnReplyDispute.setVisibility(View.VISIBLE);
            }
            btnResolveActionBack.setVisibility(View.VISIBLE);
        });

        btnSubmitDispute.setOnClickListener(v -> {
            hideKeyboard();
            submitDispute();
        });

        // Detección inteligente de rol al pulsar responder
        btnReplyDispute.setOnClickListener(v -> {
            hideKeyboard();
            String id = inputResolveId.getText().toString();
            if (!id.isEmpty()) {
                detectRoleAndOpenForm(id);
            }
        });

        btnExecuteResolve.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPlatformKey.getText().toString();
            String id = inputResolveId.getText().toString();
            String dec = spinnerResolveDecision.getSelectedItem().toString();
            if (!pk.isEmpty() && currentUserEmail.equalsIgnoreCase(PLATFORM_EMAIL))
                performResolveDispute(pk, new BigInteger(id), dec.contains("Comprador"), dec);
        });

        btnRequestPlatformReview.setOnClickListener(v -> {
            hideKeyboard();
            new AlertDialog.Builder(mContext)
                    .setTitle("Solicitar Revisión")
                    .setMessage("¿Estás seguro? Ya no se podrá enviar más mensajes...")
                    .setPositiveButton("Sí", (d, w) -> {
                        String id = inputResolveId.getText().toString();
                        if (id.isEmpty()) return;
                        dbConnection.solicitarRevision(id, new DatabaseConnection.OperacionListener() {
                            @Override
                            public void onOperacionExito(String m) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(mContext, m, Toast.LENGTH_LONG).show();
                                        loadAndRenderDisputes(id);
                                    });
                                }
                            }
                            @Override
                            public void onOperacionFallo(String e) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() ->
                                            Toast.makeText(mContext, e, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    // Esta función se encarga de rellenar automáticamente la clave privada desde las preferencias
    private void autoFillKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (!myKey.isEmpty() && inputPlatformKey != null) {
            inputPlatformKey.setText(myKey);
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

    // Esta función se encarga de configurar los selectores desplegables con un diseño personalizado
    private void setupSpinners() {
        String[] roles = {"COMPRADOR", "VENDEDOR"};
        ArrayAdapter<String> adapterRole = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, roles) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                applyFullWidthAndStyle(view, getItem(position));
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                applyFullWidthAndStyle(view, getItem(position));
                return view;
            }
            private void applyFullWidthAndStyle(View view, String text) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                view.setLayoutParams(params);

                TextView tv = (TextView) view;
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

                CircleArrowDrawable icon = new CircleArrowDrawable(bgColor, Color.BLACK);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                tv.setCompoundDrawablePadding(20);
            }
        };
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisputeRole.setAdapter(adapterRole);
        spinnerDisputeRole.setBackground(null);
        spinnerDisputeRole.setPadding(0, 0, 0, 0);

        String[] decisionOptions = {"Devolver a Comprador", "Liberar a Vendedor"};
        ArrayAdapter<String> adapterDecision = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, decisionOptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                applyFullWidthAndStyle(view, getItem(position));
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                applyFullWidthAndStyle(view, getItem(position));
                return view;
            }
            private void applyFullWidthAndStyle(View view, String text) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                } else {
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                view.setLayoutParams(params);

                TextView tv = (TextView) view;
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

                CircleArrowDrawable icon = new CircleArrowDrawable(bgColor, Color.BLACK);
                tv.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                tv.setCompoundDrawablePadding(20);
            }
        };
        adapterDecision.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolveDecision.setAdapter(adapterDecision);
        spinnerResolveDecision.setBackground(null);
        spinnerResolveDecision.setPadding(0, 0, 0, 0);
    }

    // Esta función se encarga de renderizar la lista de botones de disputas activas
    private void renderDisputesButtons() {
        showView(layoutResolveList);
        containerDisputesButtons.removeAllViews();
        tvLog.setText("");
        if (groupedDisputes.isEmpty()) {
            tvLog.setText("No hay disputas abiertas asociadas a tu cuenta.");
            if (getActivity() != null) getActivity().runOnUiThread(() -> swipeRefreshDispute.setRefreshing(false));
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

    // Esta función se encarga de mostrar el detalle y el historial de una disputa específica
    private void showOpenDisputeDetails(String escrowId) {
        showView(layoutResolveAction);
        containerDisputeHistory.removeAllViews();
        btnGoToResolveForm.setVisibility(View.VISIBLE);
        btnReplyDispute.setVisibility(View.VISIBLE);
        btnRequestPlatformReview.setVisibility(View.GONE);
        btnResolveActionBack.setVisibility(View.VISIBLE);
        layoutFinalResolveForm.setVisibility(View.GONE);
        if (scrollDisputeHistory != null) scrollDisputeHistory.setVisibility(View.VISIBLE);
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
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                resView.setPadding(30, 30, 30, 30);
                resView.setGravity(android.view.Gravity.CENTER);
                LinearLayout.LayoutParams resParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                resParams.setMargins(0, 20, 0, 0);
                resView.setLayoutParams(resParams);
                containerDisputeHistory.addView(resView);
            } else if (isUnderReview) {
                TextView revView = new TextView(mContext);
                revView.setText("En revisión...");
                revView.setBackgroundColor(Color.parseColor("#FF9800"));
                revView.setTextColor(Color.WHITE);
                revView.setPadding(30, 30, 30, 30);
                revView.setGravity(android.view.Gravity.CENTER);
                LinearLayout.LayoutParams revParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                revParams.setMargins(0, 20, 0, 0);
                revView.setLayoutParams(revParams);
                containerDisputeHistory.addView(revView);
                btnReplyDispute.setVisibility(View.GONE);
            }
        }

        if (scrollDisputeHistory != null) {
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

    // Esta función se encarga de verificar si el usuario actual es el creador de la disputa para permitirle solicitar revisión
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
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> btnRequestPlatformReview.setVisibility(View.VISIBLE));
                }
            } catch (Exception e) {
            }
        }).start();
    }

    // Esta función se encarga de enviar una nueva disputa a la base de datos
    private void submitDispute() {
        String idStr = inputDisputeEscrowId.getText().toString().trim();
        String r = inputDisputeReason.getText().toString().trim();
        String role = spinnerDisputeRole.getSelectedItem().toString();

        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (myKey.isEmpty()) return;

        btnSubmitDispute.setEnabled(false);
        new Thread(() -> {
            try {
                Credentials credentials = Credentials.create(myKey);
                String myAddress = credentials.getAddress();
                FairPayService service = new FairPayService(myKey);
                List<Type> details = service.getEscrowDetails(new BigInteger(idStr));
                String buyer = details.get(0).getValue().toString();
                String seller = details.get(1).getValue().toString();
                boolean isBuyer = myAddress.equalsIgnoreCase(buyer);
                boolean isSeller = myAddress.equalsIgnoreCase(seller);

                if (!isBuyer && !isSeller)
                    throw new Exception("Tu wallet no participa en el depósito " + idStr);
                if (role.equalsIgnoreCase("COMPRADOR") && !isBuyer)
                    throw new Exception("Tu rol real es Vendedor.");
                if (role.equalsIgnoreCase("VENDEDOR") && !isSeller)
                    throw new Exception("Tu rol real es Comprador.");

                dbConnection.abrirDisputa(currentUserEmail, idStr, role, r, buyer, seller, new DatabaseConnection.OperacionListener() {
                    @Override
                    public void onOperacionExito(String m) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(mContext, m, Toast.LENGTH_LONG).show();
                                btnSubmitDispute.setEnabled(true);
                                loadAndRenderDisputes(idStr);
                            });
                        }
                    }

                    @Override
                    public void onOperacionFallo(String e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(mContext, e, Toast.LENGTH_LONG).show();
                                btnSubmitDispute.setEnabled(true);
                            });
                        }
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmitDispute.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de abrir el formulario para responder a una disputa existente
    private void performCheckRoleAndOpenForm(String id) {
        showView(layoutOpenDisputeForm);
        inputDisputeEscrowId.setText(id);
        inputDisputeEscrowId.setEnabled(false);
    }

    // Detecta si eres Comprador o Vendedor en el contrato y abre el formulario preseleccionando el rol
    private void detectRoleAndOpenForm(String id) {
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (myKey.isEmpty()) {
            performCheckRoleAndOpenForm(id);
            return;
        }

        new Thread(() -> {
            try {
                Credentials credentials = Credentials.create(myKey);
                String myAddress = credentials.getAddress();
                FairPayService service = new FairPayService(myKey);
                List<Type> details = service.getEscrowDetails(new BigInteger(id));
                // details: 0=buyer, 1=seller
                String sellerAddr = details.get(1).getValue().toString();

                // Si soy el vendedor, seleccionamos index 1 (VENDEDOR), si no por defecto 0 (COMPRADOR)
                int indexToSelect = 0;
                if (myAddress.equalsIgnoreCase(sellerAddr)) {
                    indexToSelect = 1;
                }

                int finalIndex = indexToSelect;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        performCheckRoleAndOpenForm(id);
                        spinnerDisputeRole.setSelection(finalIndex);
                    });
                }
            } catch (Exception e) {
                // Si falla la red, abrimos igual con el valor por defecto
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> performCheckRoleAndOpenForm(id));
                }
            }
        }).start();
    }

    // Esta función se encarga de ejecutar la resolución final de la disputa en la blockchain (solo FairPay)
    private void performResolveDispute(String pk, BigInteger id, boolean refundBuyer, String decision) {
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                service.resolveDispute(id, refundBuyer);
                dbConnection.marcarDisputaResuelta(id.toString(), refundBuyer ? "COMPRADOR" : "VENDEDOR", new DatabaseConnection.OperacionListener() {
                    @Override
                    public void onOperacionExito(String m) {
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> loadAndRenderDisputes(id.toString()));
                    }

                    @Override
                    public void onOperacionFallo(String e) {
                    }
                });
            } catch (Exception e) {
            }
        }).start();
    }

    // Esta función se encarga de cargar la lista de disputas desde la base de datos
    private void loadAndRenderDisputes(String targetId) {
        showView(layoutResolveList);
        containerDisputesButtons.removeAllViews();
        tvLog.setText("Cargando disputas...");
        dbConnection.obtenerDisputas(currentUserEmail, new DatabaseConnection.DataListener<Map<String, String>>() {
            @Override
            public void onDataSuccess(List<Map<String, String>> data) {
                groupedDisputes.clear();
                for (Map<String, String> d : data) {
                    String eid = d.get("escrowId");
                    if (!groupedDisputes.containsKey(eid))
                        groupedDisputes.put(eid, new ArrayList<>());
                    groupedDisputes.get(eid).add(d);
                }
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    renderDisputesButtons();
                    swipeRefreshDispute.setRefreshing(false); // Detener animación de carga
                    if (targetId != null && groupedDisputes.containsKey(targetId))
                        showOpenDisputeDetails(targetId);
                });
            }

            @Override
            public void onDataFailure(String e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e);
                    swipeRefreshDispute.setRefreshing(false); // Detener animación de carga
                });
            }
        });
    }

    // Esta función auxiliar se encarga de mostrar una vista específica y ocultar las demás
    private void showView(View v) {
        layoutDisputeMenu.setVisibility(View.GONE);
        layoutOpenDisputeForm.setVisibility(View.GONE);
        layoutResolveList.setVisibility(View.GONE);
        layoutResolveAction.setVisibility(View.GONE);
        if (v != null) v.setVisibility(View.VISIBLE);
    }

    // Clase interna para dibujar un triángulo en los selectores desplegables
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
            arrowPath.moveTo(cx - halfW, cy - halfH / 2);
            arrowPath.lineTo(cx + halfW, cy - halfH / 2);
            arrowPath.lineTo(cx, cy + halfH);
            arrowPath.close();
            canvas.drawPath(arrowPath, arrowPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            circlePaint.setAlpha(alpha);
            arrowPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            circlePaint.setColorFilter(colorFilter);
            arrowPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return size;
        }

        @Override
        public int getIntrinsicHeight() {
            return size;
        }
    }
}