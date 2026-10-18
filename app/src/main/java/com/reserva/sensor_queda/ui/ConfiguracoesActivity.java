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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.dados.ContatoEmergencia;
import com.reserva.sensor_queda.dados.RepositorioConfiguracoes;
import com.reserva.sensor_queda.dados.TelefoneUtil;
import com.reserva.sensor_queda.sensor.DetectorQueda;

import java.util.List;

/** Tela 4 - Configuracoes. */
public class ConfiguracoesActivity extends AppCompatActivity {

    private static final int[] OPCOES_INTERVALO = {1, 5, 10, 15, 30, 60};

    private RepositorioConfiguracoes configuracoes;
    private DetectorQueda detectorQueda;

    private LinearLayout containerContatos;
    private TextView txtContatosVazio;
    private TextView txtStatusSensores;
    private TextView txtStatusPermissao;
    private TextView txtIntervalo;
    private View linhaIntervalo;
    private MaterialSwitch switchMonitoramento;
    private MaterialSwitch switchCheckin;
    private MaterialSwitch switchDemonstracao;
    private RadioGroup radioTransporte;
    private TextInputEditText inputEndpoint;
    private TextInputEditText inputUsuario;

    private List<ContatoEmergencia> contatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracoes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configuracoes = RepositorioConfiguracoes.get(this);
        detectorQueda = new DetectorQueda(this, null);

        TextView titulo = findViewById(R.id.txtTituloBarra);
        titulo.setText(R.string.configuracoes);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        containerContatos = findViewById(R.id.containerContatos);
        txtContatosVazio = findViewById(R.id.txtContatosVazio);
        txtStatusSensores = findViewById(R.id.txtStatusSensores);
        txtStatusPermissao = findViewById(R.id.txtStatusPermissao);
        txtIntervalo = findViewById(R.id.txtIntervalo);
        linhaIntervalo = findViewById(R.id.linhaIntervalo);
        switchMonitoramento = findViewById(R.id.switchMonitoramento);
        switchCheckin = findViewById(R.id.switchCheckin);
        switchDemonstracao = findViewById(R.id.switchDemonstracao);
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
                configuracoes.setMonitoramentoAtivo(checked));

        switchCheckin.setOnCheckedChangeListener((b, checked) -> {
            configuracoes.setCheckInAtivo(checked);
            linhaIntervalo.setAlpha(checked ? 1f : 0.5f);
        });

        switchDemonstracao.setOnCheckedChangeListener((b, checked) ->
                configuracoes.setModoDemo(checked));

        inputEndpoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                configuracoes.setEndpoint(s.toString().trim());
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
                configuracoes.setUsuario(s.toString().trim());
            }
        });

        radioTransporte.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBicicleta) {
                configuracoes.setModoTransporte(RepositorioConfiguracoes.TRANSPORTE_BICICLETA);
            } else if (checkedId == R.id.radioCaminhada) {
                configuracoes.setModoTransporte(RepositorioConfiguracoes.TRANSPORTE_CAMINHADA);
            } else {
                configuracoes.setModoTransporte(RepositorioConfiguracoes.TRANSPORTE_MOTO);
            }
        });

        linhaIntervalo.setOnClickListener(v -> escolherIntervalo());

        findViewById(R.id.btnPermissoes).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
    }

    private void carregarPreferencias() {
        switchMonitoramento.setChecked(configuracoes.isMonitoramentoAtivo());
        switchCheckin.setChecked(configuracoes.isCheckInAtivo());
        switchDemonstracao.setChecked(configuracoes.isModoDemo());
        linhaIntervalo.setAlpha(configuracoes.isCheckInAtivo() ? 1f : 0.5f);
        inputEndpoint.setText(configuracoes.getEndpoint());
        inputUsuario.setText(configuracoes.getUsuario());
        atualizarLabelIntervalo();

        switch (configuracoes.getModoTransporte()) {
            case RepositorioConfiguracoes.TRANSPORTE_BICICLETA:
                radioTransporte.check(R.id.radioBicicleta);
                break;
            case RepositorioConfiguracoes.TRANSPORTE_CAMINHADA:
                radioTransporte.check(R.id.radioCaminhada);
                break;
            default:
                radioTransporte.check(R.id.radioMoto);
                break;
        }
    }

    private void atualizarLabelIntervalo() {
        int minutos = configuracoes.getIntervaloCheckIn();
        txtIntervalo.setText(getResources().getQuantityString(
                R.plurals.intervalo_checkin_valor, minutos, minutos));
    }

    private void escolherIntervalo() {
        final int atual = configuracoes.getIntervaloCheckIn();
        String[] rotulos = new String[OPCOES_INTERVALO.length];
        int selecionado = 0;
        for (int i = 0; i < OPCOES_INTERVALO.length; i++) {
            rotulos[i] = getResources().getQuantityString(
                    R.plurals.intervalo_checkin_valor, OPCOES_INTERVALO[i], OPCOES_INTERVALO[i]);
            if (OPCOES_INTERVALO[i] == atual) {
                selecionado = i;
            }
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.intervalo_checkin)
                .setSingleChoiceItems(rotulos, selecionado, (dialog, which) -> {
                    configuracoes.setIntervaloCheckIn(OPCOES_INTERVALO[which]);
                    atualizarLabelIntervalo();
                    dialog.dismiss();
                })
                .show();
    }

    private void renderizarContatos() {
        contatos = configuracoes.getContatos();
        containerContatos.removeAllViews();
        txtContatosVazio.setVisibility(contatos.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < contatos.size(); i++) {
            final int index = i;
            ContatoEmergencia contato = contatos.get(i);
            View view = inflater.inflate(R.layout.item_contato, containerContatos, false);
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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_contato, null);
        TextInputLayout layoutNome = view.findViewById(R.id.layoutNome);
        TextInputLayout layoutTelefone = view.findViewById(R.id.layoutTelefone);
        TextInputEditText inputNome = view.findViewById(R.id.inputNome);
        TextInputEditText inputTelefone = view.findViewById(R.id.inputTelefone);

        final boolean edicao = index >= 0;
        if (edicao) {
            ContatoEmergencia contato = contatos.get(index);
            inputNome.setText(contato.nome);
            inputTelefone.setText(contato.telefone);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(edicao ? R.string.editar_contato : R.string.adicionar_contato)
                .setView(view)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, null)
                .create();

        // Sobrescreve o botao para validar sem fechar o dialogo em caso de erro.
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    layoutNome.setError(null);
                    layoutTelefone.setError(null);

                    String nome = inputNome.getText() == null
                            ? "" : inputNome.getText().toString().trim();
                    String telefone = inputTelefone.getText() == null
                            ? "" : inputTelefone.getText().toString().trim();

                    boolean valido = true;
                    if (nome.isEmpty()) {
                        layoutNome.setError(getString(R.string.nome_erro_vazio));
                        valido = false;
                    }

                    TelefoneUtil.Resultado resultado = TelefoneUtil.normalizar(telefone);
                    if (!resultado.isValido()) {
                        layoutTelefone.setError(mensagemErroTelefone(resultado.motivo));
                        valido = false;
                    }
                    if (!valido) {
                        return;
                    }

                    if (edicao) {
                        contatos.get(index).nome = nome;
                        contatos.get(index).telefone = resultado.e164;
                    } else {
                        contatos.add(new ContatoEmergencia(nome, resultado.e164));
                    }
                    configuracoes.salvarContatos(contatos);
                    renderizarContatos();
                    Toast.makeText(this, R.string.toast_contato_adicionado, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private String mensagemErroTelefone(int motivo) {
        switch (motivo) {
            case TelefoneUtil.VAZIO:
                return getString(R.string.telefone_erro_vazio);
            case TelefoneUtil.DDD:
                return getString(R.string.telefone_erro_ddd);
            default:
                return getString(R.string.telefone_erro_tamanho);
        }
    }

    private void confirmarRemocao(int index) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.remover)
                .setMessage(contatos.get(index).nome)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.remover, (dialog, which) -> {
                    contatos.remove(index);
                    configuracoes.salvarContatos(contatos);
                    renderizarContatos();
                    Toast.makeText(this, R.string.toast_contato_removido, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void atualizarStatusSensores() {
        if (!detectorQueda.estaDisponivel()) {
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
        boolean localizacao = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean sms = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        if (localizacao && sms) {
            txtStatusPermissao.setText(R.string.permissao_ok_ambas);
        } else if (!localizacao && !sms) {
            txtStatusPermissao.setText(R.string.permissao_ambas_pendente);
        } else if (!localizacao) {
            txtStatusPermissao.setText(R.string.localizacao_permissao);
        } else {
            txtStatusPermissao.setText(R.string.permissao_sms);
        }
    }
}
