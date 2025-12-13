package es.ifp.fairpay.ui.main.operations;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ifp.fairpay.R;
import es.ifp.fairpay.data.service.FairPayService;

public class VendedorFragment extends Fragment {

    private EditText inputSellerPrivateKey, inputSellerEscrowId;
    private Button btnSellerCheckStatus, btnSellerReceivePayment, btnSellerBack;
    private TextView tvLog;
    private Context mContext;
    private static final String PREF_COMPLETED_IDS = "COMPLETED_IDS";

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
        View view = inflater.inflate(R.layout.fragment_vendedor, container, false);

        inputSellerPrivateKey = view.findViewById(R.id.input_seller_private_key);
        inputSellerEscrowId = view.findViewById(R.id.input_seller_escrow_id);
        btnSellerCheckStatus = view.findViewById(R.id.btn_seller_check_status);
        btnSellerReceivePayment = view.findViewById(R.id.btn_seller_receive);
        btnSellerBack = view.findViewById(R.id.btn_seller_back);
        tvLog = view.findViewById(R.id.tv_resultado_log);

        setupListeners();
        autoFillKeys();

        if (getArguments() != null) {
            String pendingId = getArguments().getString("PENDING_ID");
            if (pendingId != null) {
                inputSellerEscrowId.setText(pendingId);
                tvLog.setText("Depósito Aprobado ID: " + pendingId + ". Libera los fondos.");
            }
        }

        return view;
    }

    // Esta función se encarga de configurar los eventos de clic para los botones de las distintas secciones
    private void setupListeners() {
        btnSellerBack.setOnClickListener(v -> {
            hideKeyboard();
            if (getParentFragment() instanceof OperacionesFragment) {
                ((OperacionesFragment) getParentFragment()).resetUI();
            }
        });

        btnSellerCheckStatus.setOnClickListener(v -> {
            hideKeyboard();
            String idStr = inputSellerEscrowId.getText().toString().trim();
            if (!idStr.isEmpty()) {
                performCheck(inputSellerPrivateKey.getText().toString().trim(), new BigInteger(idStr));
            }
        });

        btnSellerReceivePayment.setOnClickListener(v -> {
            hideKeyboard();
            String idStr = inputSellerEscrowId.getText().toString().trim();
            if (!idStr.isEmpty()) {
                performReceive(inputSellerPrivateKey.getText().toString().trim(), new BigInteger(idStr));
            }
        });
    }

    // Esta función se encarga de comprobar el estado del depósito en la blockchain
    private void performCheck(String pk, BigInteger id) {
        tvLog.setText("Consultando...");
        btnSellerCheckStatus.setEnabled(false);
        new Thread(() -> {
            try {
                String tempPk = pk.isEmpty() ? "0x01" : pk;
                FairPayService service = new FairPayService(tempPk);
                List<Type> details = service.getEscrowDetails(id);
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    btnSellerCheckStatus.setEnabled(true);
                    if (isFunded && approvals.equals(BigInteger.ONE))
                        tvLog.setText("Listo para recibir pago.");
                    else if (!isFunded) tvLog.setText("No pagado aún.");
                    else tvLog.setText("Pagado, esperando aprobación.");
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnSellerCheckStatus.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de iniciar el proceso de recepción de fondos si las condiciones se cumplen
    private void performReceive(String pk, BigInteger id) {
        tvLog.setText("Reclamando...");
        btnSellerReceivePayment.setEnabled(false);
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                List<Type> details = service.getEscrowDetails(id);
                boolean isFunded = (Boolean) details.get(3).getValue();
                BigInteger approvals = (BigInteger) details.get(4).getValue();
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    if (isFunded && approvals.equals(BigInteger.ONE)) {
                        performApproveRelease(id, pk);
                    } else {
                        tvLog.setText("No puedes reclamar aún.");
                        btnSellerReceivePayment.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Error: " + e.getMessage());
                    btnSellerReceivePayment.setEnabled(true);
                });
            }
        }).start();
    }

    // Esta función se encarga de ejecutar la liberación final de los fondos en la blockchain
    private void performApproveRelease(BigInteger id, String pk) {
        new Thread(() -> {
            try {
                FairPayService service = new FairPayService(pk);
                String hash = service.approveRelease(id);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    tvLog.setText("Aprobación enviada. Hash: " + hash);
                    SharedPreferences prefs = mContext.getSharedPreferences("FairPayState", Context.MODE_PRIVATE);
                    Set<String> ids = prefs.getStringSet(PREF_COMPLETED_IDS, new HashSet<>());
                    Set<String> newIds = new HashSet<>(ids);
                    newIds.add(id.toString());
                    prefs.edit().putStringSet(PREF_COMPLETED_IDS, newIds).apply();

                    new CountDownTimer(19000, 1000) {
                        public void onTick(long m) {
                            btnSellerReceivePayment.setText("Procesando... " + m / 1000);
                        }
                        public void onFinish() {
                            btnSellerReceivePayment.setText("Recibir pago");
                            btnSellerReceivePayment.setEnabled(true);
                            tvLog.setText("FINALIZADO. Depósito liberado.");
                        }
                    }.start();
                });
            } catch (Exception e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> tvLog.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // Esta función se encarga de rellenar automáticamente la clave privada desde las preferencias
    private void autoFillKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences("FairPayPrefs", Context.MODE_PRIVATE);
        String myKey = prefs.getString("CURRENT_USER_PRIVATE_KEY", "");
        if (!myKey.isEmpty() && inputSellerPrivateKey != null)
            inputSellerPrivateKey.setText(myKey);
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