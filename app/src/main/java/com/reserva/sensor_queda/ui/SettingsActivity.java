package com.reserva.sensor_queda.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.data.EmergencyContact;
import com.reserva.sensor_queda.data.SettingsRepository;
import com.reserva.sensor_queda.sensor.FallDetector;

import java.util.List;

/** Tela 4 - Configuracoes. */
public class SettingsActivity extends AppCompatActivity {

    private static final int[] OPCOES_INTERVALO = {5, 10, 15, 30, 60};

    private SettingsRepository settings;
    private FallDetector fallDetector;

    private LinearLayout containerContatos;
    private TextView txtContatosVazio;
    private TextView txtStatusSensores;
    private TextView txtStatusPermissao;
    private TextView txtIntervalo;
    private View rowIntervalo;
    private MaterialSwitch switchMonitoramento;
    private MaterialSwitch switchCheckin;
    private MaterialSwitch switchDemo;
    private RadioGroup radioTransporte;
    private TextInputEditText inputEndpoint;
    private TextInputEditText inputUsuario;

    private List<EmergencyContact> contatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        settings = SettingsRepository.get(this);
        fallDetector = new FallDetector(this, null);

        TextView titulo = findViewById(R.id.txtToolbarTitle);
        titulo.setText(R.string.configuracoes);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        containerContatos = findViewById(R.id.containerContatos);
        txtContatosVazio = findViewById(R.id.txtContatosVazio);
        txtStatusSensores = findViewById(R.id.txtStatusSensores);
        txtStatusPermissao = findViewById(R.id.txtStatusPermissao);
        txtIntervalo = findViewById(R.id.txtIntervalo);
        rowIntervalo = findViewById(R.id.rowIntervalo);
        switchMonitoramento = findViewById(R.id.switchMonitoramento);
        switchCheckin = findViewById(R.id.switchCheckin);
        switchDemo = findViewById(R.id.switchDemo);
        radioTransporte = findViewById(R.id.radioTransporte);
        inputEndpoint = findViewById(R.id.inputEndpoint);
        inputUsuario = findViewById(R.id.inputUsuario);

        configurarListeners();
        carregarPreferencias();
        renderizarContatos();
        atualizarStatusSensores();
    }

    private void configurarListeners() {
        findViewById(R.id.btnAddContato).setOnClickListener(v -> abrirDialogoContato(-1));

        switchMonitoramento.setOnCheckedChangeListener((b, checked) ->
                settings.setMonitoringEnabled(checked));

        switchCheckin.setOnCheckedChangeListener((b, checked) -> {
            settings.setCheckInEnabled(checked);
            rowIntervalo.setAlpha(checked ? 1f : 0.5f);
        });

        switchDemo.setOnCheckedChangeListener((b, checked) ->
                settings.setDemoMode(checked));

        inputEndpoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                settings.setEndpoint(s.toString().trim());
            }
        });

        inputUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                settings.setUsuario(s.toString().trim());
            }
        });

        radioTransporte.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBike) {
                settings.setTransportMode(SettingsRepository.TRANSPORTE_BIKE);
            } else if (checkedId == R.id.radioCaminhada) {
                settings.setTransportMode(SettingsRepository.TRANSPORTE_CAMINHADA);
            } else {
                settings.setTransportMode(SettingsRepository.TRANSPORTE_MOTO);
            }
        });

        rowIntervalo.setOnClickListener(v -> escolherIntervalo());

        findViewById(R.id.btnPermissoes).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
    }

    private void carregarPreferencias() {
        switchMonitoramento.setChecked(settings.isMonitoringEnabled());
        switchCheckin.setChecked(settings.isCheckInEnabled());
        switchDemo.setChecked(settings.isDemoMode());
        rowIntervalo.setAlpha(settings.isCheckInEnabled() ? 1f : 0.5f);
        inputEndpoint.setText(settings.getEndpoint());
        inputUsuario.setText(settings.getUsuario());
        atualizarLabelIntervalo();

        switch (settings.getTransportMode()) {
            case SettingsRepository.TRANSPORTE_BIKE:
                radioTransporte.check(R.id.radioBike);
                break;
            case SettingsRepository.TRANSPORTE_CAMINHADA:
                radioTransporte.check(R.id.radioCaminhada);
                break;
            default:
                radioTransporte.check(R.id.radioMoto);
                break;
        }
    }

    private void atualizarLabelIntervalo() {
        txtIntervalo.setText(getString(R.string.intervalo_checkin_valor,
                settings.getCheckInIntervalMinutes()));
    }

    private void escolherIntervalo() {
        final int atual = settings.getCheckInIntervalMinutes();
        String[] rotulos = new String[OPCOES_INTERVALO.length];
        int selecionado = 0;
        for (int i = 0; i < OPCOES_INTERVALO.length; i++) {
            rotulos[i] = getString(R.string.intervalo_checkin_valor, OPCOES_INTERVALO[i]);
            if (OPCOES_INTERVALO[i] == atual) {
                selecionado = i;
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.intervalo_checkin)
                .setSingleChoiceItems(rotulos, selecionado, (dialog, which) -> {
                    settings.setCheckInIntervalMinutes(OPCOES_INTERVALO[which]);
                    atualizarLabelIntervalo();
                    dialog.dismiss();
                })
                .show();
    }

    private void renderizarContatos() {
        contatos = settings.getContacts();
        containerContatos.removeAllViews();
        txtContatosVazio.setVisibility(contatos.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < contatos.size(); i++) {
            final int index = i;
            EmergencyContact contato = contatos.get(i);
            View view = inflater.inflate(R.layout.item_contact, containerContatos, false);
            ((TextView) view.findViewById(R.id.txtContatoNome)).setText(contato.nome);
            ((TextView) view.findViewById(R.id.txtContatoTelefone)).setText(contato.telefone);
            view.findViewById(R.id.btnEditarContato).setOnClickListener(v ->
                    abrirDialogoContato(index));
            view.findViewById(R.id.btnRemoverContato).setOnClickListener(v ->
                    confirmarRemocao(index));
            containerContatos.addView(view);
        }
    }

    private void abrirDialogoContato(int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contact, null);
        TextInputEditText inputNome = view.findViewById(R.id.inputNome);
        TextInputEditText inputTelefone = view.findViewById(R.id.inputTelefone);

        final boolean edicao = index >= 0;
        if (edicao) {
            EmergencyContact contato = contatos.get(index);
            inputNome.setText(contato.nome);
            inputTelefone.setText(contato.telefone);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(edicao ? R.string.editar_contato : R.string.adicionar_contato)
                .setView(view)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, (dialog, which) -> {
                    String nome = inputNome.getText() == null ? "" : inputNome.getText().toString().trim();
                    String telefone = inputTelefone.getText() == null ? "" : inputTelefone.getText().toString().trim();
                    if (nome.isEmpty() || telefone.isEmpty()) {
                        Toast.makeText(this, R.string.toast_campos_obrigatorios, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (edicao) {
                        contatos.get(index).nome = nome;
                        contatos.get(index).telefone = telefone;
                    } else {
                        contatos.add(new EmergencyContact(nome, telefone));
                    }
                    settings.saveContacts(contatos);
                    renderizarContatos();
                    Toast.makeText(this, R.string.toast_contato_adicionado, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void confirmarRemocao(int index) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.remover)
                .setMessage(contatos.get(index).nome)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.remover, (dialog, which) -> {
                    contatos.remove(index);
                    settings.saveContacts(contatos);
                    renderizarContatos();
                    Toast.makeText(this, R.string.toast_contato_removido, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void atualizarStatusSensores() {
        if (!fallDetector.isAvailable()) {
            txtStatusSensores.setText(R.string.sensor_indisponivel);
            txtStatusSensores.setTextColor(ContextCompat.getColor(this, R.color.emergency));
        } else {
            txtStatusSensores.setText(R.string.sensores_ok);
            txtStatusSensores.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean concedida = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        txtStatusPermissao.setText(concedida
                ? R.string.permissao_ok
                : R.string.localizacao_permissao);
    }
}
