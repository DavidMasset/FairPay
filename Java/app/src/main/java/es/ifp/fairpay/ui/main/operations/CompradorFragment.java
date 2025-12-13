package es.ifp.fairpay.ui.main.operations;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.web3j.utils.Convert;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.List;

import es.ifp.fairpay.R;
import es.ifp.fairpay.data.service.FairPayService;

public class CompradorFragment extends Fragment {

    private LinearLayout layoutBuyerMenu, layoutFormsContainer, layoutCreateEscrow, layoutFundEscrow, layoutBuyerApprove, layoutExistingEscrowBuyer;
    private Button btnBuyerNew, btnBuyerExisting, btnBuyerBack;
    private EditText inputPrivateKey, inputSeller, inputEscrowId, inputPrivateKeyFund, inputFundAmount, inputExistingEscrowId;
    private Button btnEnviarEscrow, btnSearchId, btnFormBackBuyer, btnFundEscrow, btnBuyerApprove, btnCheckEscrowStatus, btnExistingBackBuyer;
    private TextView tvLog;
    private Context mContext;
    private BigInteger lastEscrowId = null;
    private String lastTxHash = null;

    // Esta función se encarga de adjuntar el contexto del fragmento para asegurar el acceso a recursos
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // Esta función se encarga de inflar el diseño visual del fragmento y configurar la lógica inicial
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comprador, container, false);
        setupViews(view);
        autoFillKeys();

        if (getArguments() != null) {
            String pendingId = getArguments().getString("PENDING_ID");
            int type = getArguments().getInt("PENDING_TYPE");
            if (pendingId != null) {
                handlePendingStart(pendingId, type);
            }

            String walletVendedor = getArguments().getString("WALLET_VENDEDOR");
            if (walletVendedor != null && !walletVendedor.isEmpty()) {
                handleAgendaStart(walletVendedor);
            }
        }
        return view;
    }

    // Esta función se encarga de vincular los elementos visuales del XML con las variables de la clase
    private void setupViews(View view) {
        layoutBuyerMenu = view.findViewById(R.id.layout_buyer_menu);
        layoutFormsContainer = view.findViewById(R.id.layout_forms_container);
        layoutCreateEscrow = view.findViewById(R.id.layout_create_escrow);
        layoutFundEscrow = view.findViewById(R.id.layout_fund_escrow);
        layoutBuyerApprove = view.findViewById(R.id.layout_buyer_approve);
        layoutExistingEscrowBuyer = view.findViewById(R.id.layout_existing_escrow_buyer);
        tvLog = view.findViewById(R.id.tv_resultado_log);

        btnBuyerNew = view.findViewById(R.id.btn_buyer_new);
        btnBuyerExisting = view.findViewById(R.id.btn_buyer_existing);
        btnBuyerBack = view.findViewById(R.id.btn_buyer_back);

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

        setupListeners();
    }

    // Esta función se encarga de configurar los eventos de clic para los botones de las distintas secciones
    private void setupListeners() {
        btnBuyerNew.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutFormsContainer);
            layoutCreateEscrow.setVisibility(View.VISIBLE);
            tvLog.setText("PASO 1: Comprador inicia el depósito.");
        });

        btnBuyerExisting.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutExistingEscrowBuyer);
        });

        btnBuyerBack.setOnClickListener(v -> {
            hideKeyboard();
            if (getParentFragment() instanceof OperacionesFragment) {
                ((OperacionesFragment) getParentFragment()).resetUI();
            }
        });

        btnFormBackBuyer.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutBuyerMenu);
        });
        btnExistingBackBuyer.setOnClickListener(v -> {
            hideKeyboard();
            showView(layoutBuyerMenu);
        });

        btnEnviarEscrow.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKey.getText().toString().trim();
            String seller = inputSeller.getText().toString().trim();
            if (!pk.isEmpty() && !seller.isEmpty()) performCreateEscrow(pk, seller);
        });

        btnSearchId.setOnClickListener(v -> {
            hideKeyboard();
            if (lastTxHash != null) performSearchEscrowId(lastTxHash);
        });

        btnFundEscrow.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKeyFund.getText().toString().trim();
            String amount = inputFundAmount.getText().toString().trim();
            if (!pk.isEmpty() && lastEscrowId != null) {
                try {
                    performFundEscrow(pk, lastEscrowId, Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger());
                } catch (Exception e) {
                    Toast.makeText(mContext, "Monto inválido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBuyerApprove.setOnClickListener(v -> {
            hideKeyboard();
            String pk = inputPrivateKey.getText().toString().trim();
            if (pk.isEmpty()) pk = inputPrivateKeyFund.getText().toString().trim();
            if (btnBuyerApprove.getText().toString().equals("Aprobar recepción")) {
                if (!pk.isEmpty() && lastEscrowId != null) performApprove(lastEscrowId, pk);
            } else {
                performVerifyCompletion(pk, lastEscrowId);
            }
        });

        btnCheckEscrowStatus.setOnClickListener(v -> {
            hideKeyboard();
            String id = inputExistingEscrowId.getText().toString().trim();
            if (!id.isEmpty()) performCheckStatus(inputPrivateKey.getText().toString(), id);
        });
    }

    // Esta función se encarga de configurar la vista cuando se accede desde una operación pendiente
    private void handlePendingStart(String id, int type) {
        showView(layoutFormsContainer);
        layoutBuyerMenu.setVisibility(View.GONE);
        lastEscrowId = new BigInteger(id);
        inputEscrowId.setText(id);

        if (type == 1) {
            layoutFundEscrow.setVisibility(View.VISIBLE);
            tvLog.setText("Depósito Creado ID: " + id + ". Haz el pago.");
        } else if (type == 2) {
            layoutBuyerApprove.setVisibility(View.VISIBLE);
            btnBuyerApprove.setText("Aprobar recepción");
            tvLog.setText("Depósito Pagado ID: " + id + ". Confirma la recepción.");
        }
    }

    // Esta función se encarga de configurar la vista cuando se accede desde la agenda para enviar dinero
    private void handleAgendaStart(String walletVendedor) {
        showView(layoutFormsContainer);
        layoutCreateEscrow.setVisibility(View.VISIBLE);
        if (inputSeller != null) {
            inputSeller.setText(walletVendedor);
        }
        if (tvLog != null) {
            tvLog.setText("INICIAR PAGO A CONTACTO:\nWallet vendedor cargada correctamente.\nPASO 1: Comprador inicia el depósito.");
        }
    }

    // Esta función auxiliar se encarga de mostrar una vista específica y ocultar las demás
    private void showView(View view) {
        layoutBuyerMenu.setVisibility(View.GONE);
        layoutFormsContainer.setVisibility(View.GONE);
        layoutExistingEscrowBuyer.setVisibility(View.GONE);
        layoutCreateEscrow.setVisibility(View.GONE);
        layoutFundEscrow.setVisibility(View.GONE);
        layoutBuyerApprove.setVisibility(View.GONE);
        if (view != null) view.setVisibility(View.VISIBLE);
    }

    // Esta función se encarga de ejecutar la creación del contrato escrow en la blockchain
    private void performCreateEscrow(String pk, String seller) {
        tvLog.setText("Creando...");
        btnEnviarEscrow.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.createEscrow(seller);
                lastTxHash = hash;
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Hash: " + hash + "\nEsperando...");
                    startCountdownTimer();
                    btnEnviarEscrow.setEnabled(true);
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnEnviarEscrow.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de iniciar un temporizador visual mientras se espera la confirmación
    private void startCountdownTimer() {
        btnSearchId.setVisibility(View.VISIBLE);
        btnSearchId.setEnabled(false);
        new CountDownTimer(15000, 1000) {
            public void onTick(long m) {
                btnSearchId.setText("Procesando... " + m / 1000);
            }
            public void onFinish() {
                btnSearchId.setText("BUSCAR ID CONFIRMADO");
                btnSearchId.setEnabled(true);
            }
        }.start();
    }

    // Esta función se encarga de buscar el ID del contrato escrow una vez minado
    private void performSearchEscrowId(String hash) {
        tvLog.setText("Buscando ID...");
        btnSearchId.setEnabled(false);
        new Thread(() -> {
            try {
                String pk = inputPrivateKey.getText().toString();
                if (pk.isEmpty()) pk = "0x01";
                FairPayService service = new FairPayService(pk);
                BigInteger id = service.waitForEscrowId(hash);
                lastEscrowId = id;
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("ID encontrado: " + id);
                    inputEscrowId.setText(id.toString());
                    layoutCreateEscrow.setVisibility(View.GONE);
                    layoutFundEscrow.setVisibility(View.VISIBLE);
                    btnSearchId.setVisibility(View.GONE);
                    autoFillKeys();
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnSearchId.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de enviar los fondos al contrato escrow
    private void performFundEscrow(String pk, BigInteger id, BigInteger amountWei) {
        tvLog.setText("Enviando pago...");
        btnFundEscrow.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.fundEscrow(id, amountWei);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Pago enviado. Confirmando...");
                    new CountDownTimer(19000, 1000) {
                        public void onTick(long m) {
                            btnFundEscrow.setText("Procesando... " + m / 1000);
                        }
                        public void onFinish() {
                            tvLog.setText("Pago confirmado. Pendiente aprobar.");
                            btnFundEscrow.setEnabled(true);
                            layoutFundEscrow.setVisibility(View.GONE);
                            layoutBuyerApprove.setVisibility(View.VISIBLE);
                            btnBuyerApprove.setText("Aprobar recepción");
                        }
                    }.start();
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnFundEscrow.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de aprobar la liberación de fondos al vendedor
    private void performApprove(BigInteger id, String pk) {
        tvLog.setText("Aprobando...");
        btnBuyerApprove.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.approveRelease(id);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Aprobación enviada. Hash: " + hash);
                    new CountDownTimer(19000, 1000) {
                        public void onTick(long m) {
                            btnBuyerApprove.setText("Procesando... " + m / 1000);
                        }
                        public void onFinish() {
                            btnBuyerApprove.setText("Comprobar");
                            btnBuyerApprove.setEnabled(true);
                        }
                    }.start();
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnBuyerApprove.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de verificar si el contrato ha finalizado tras la aprobación
    private void performVerifyCompletion(String pk, BigInteger id) {
        tvLog.setText("Verificando...");
        btnBuyerApprove.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                List<Type> details = service.getEscrowDetails(id);
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    btnBuyerApprove.setEnabled(true);
                    if (approvals.compareTo(BigInteger.ZERO) > 0) {
                        showView(null);
                        tvLog.setVisibility(View.VISIBLE);
                        tvLog.setText("FINALIZADO: Depósito completado.");
                    } else {
                        tvLog.setText("Aún no confirmado.");
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnBuyerApprove.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de comprobar el estado de un depósito existente
    private void performCheckStatus(String pk, String idStr) {
        new Thread(() -> {
            try {
                BigInteger id = new BigInteger(idStr);
                lastEscrowId = id;
                String tempPk = (pk == null || pk.isEmpty()) ? "0x01" : pk;
                FairPayService service = new FairPayService(tempPk);
                List<Type> details = service.getEscrowDetails(id);
                BigInteger amount = (BigInteger) details.get(2).getValue();
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();

                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    if (isFunded && approvals.compareTo(BigInteger.ZERO) > 0) {
                        showView(null);
                        tvLog.setText("Estado: Finalizado.");
                        return;
                    }
                    showView(layoutFormsContainer);
                    if (!isFunded && amount.equals(BigInteger.ZERO)) {
                        tvLog.setText(Html.fromHtml("Estado: Creado pero no pagado. Haz el pago."));
                        layoutFundEscrow.setVisibility(View.VISIBLE);
                        inputEscrowId.setText(idStr);
                    } else if (isFunded && approvals.equals(BigInteger.ZERO)) {
                        tvLog.setText(Html.fromHtml("Estado: Pagado. Aprueba recepción."));
                        layoutBuyerApprove.setVisibility(View.VISIBLE);
                        btnBuyerApprove.setText("Aprobar recepción");
                    } else {
                        tvLog.setText("Estado desconocido.");
                        showView(layoutBuyerMenu);
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> tvLog.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // Esta función se encarga de rellenar automáticamente la clave privada desde las preferencias
    private void autoFillKeys() {
        String myKey = "";
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (!myKey.isEmpty()) {
            if (inputPrivateKey != null) inputPrivateKey.setText(myKey);
            if (inputPrivateKeyFund != null) inputPrivateKeyFund.setText(myKey);
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