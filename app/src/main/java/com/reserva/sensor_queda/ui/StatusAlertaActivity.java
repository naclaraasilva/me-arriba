package com.reserva.sensor_queda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.reserva.sensor_queda.MainActivity;
import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.alerta.ResultadoAlerta;
import com.reserva.sensor_queda.alerta.ServicoAlerta;
import com.reserva.sensor_queda.dados.RepositorioEventos;
import com.reserva.sensor_queda.dados.TipoEvento;

/** Tela 3 - Status real da tentativa de emergencia. */
public class StatusAlertaActivity extends AppCompatActivity {

    private ImageView imgAlertaEstado;
    private TextView txtAlertaTitulo;
    private TextView txtAlertaMsg;
    private TextView txtAlertaDemo;
    private TextView txtStatusApi;
    private TextView txtStatusContatos;
    private TextView txtStatusLocalizacao;
    private TextView txtLocalResumo;
    private MaterialButton btnCompartilhar;
    private MaterialButton btnTentarNovamente;

    private RepositorioEventos eventos;
    private ServicoAlerta servicoAlerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status_alerta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventos = RepositorioEventos.get(this);
        servicoAlerta = new ServicoAlerta(this);

        imgAlertaEstado = findViewById(R.id.imgAlertaEstado);
        txtAlertaTitulo = findViewById(R.id.txtAlertaTitulo);
        txtAlertaMsg = findViewById(R.id.txtAlertaMsg);
        txtAlertaDemo = findViewById(R.id.txtAlertaDemo);
        txtStatusApi = findViewById(R.id.txtStatusApi);
        txtStatusContatos = findViewById(R.id.txtStatusContatos);
        txtStatusLocalizacao = findViewById(R.id.txtStatusLocalizacao);
        txtLocalResumo = findViewById(R.id.txtLocalResumo);
        btnCompartilhar = findViewById(R.id.btnCompartilhar);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamente);

        TextView titulo = findViewById(R.id.txtTituloBarra);
        titulo.setText(R.string.alerta_titulo);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        btnTentarNovamente.setOnClickListener(v -> enviar());
        btnCompartilhar.setOnClickListener(v -> compartilharLocalizacao());
        findViewById(R.id.btnVoltarInicio).setOnClickListener(v -> voltarInicio());

        eventos.registrar(TipoEvento.ALERTA_SOLICITADO, getString(R.string.evento_alerta_solicitado),
                null);

        enviar();
    }

    private void enviar() {
        estadoEnviando();
        float g = getIntent().getFloatExtra(QuedaDetectadaActivity.EXTRA_G, 0f);
        servicoAlerta.enviarAlerta(g, this::aplicarResultado);
    }

    private void estadoEnviando() {
        imgAlertaEstado.setImageResource(R.drawable.ic_clock);
        imgAlertaEstado.setColorFilter(ContextCompat.getColor(this, R.color.secondary_action));
        txtAlertaTitulo.setText(R.string.alerta_enviando);
        txtAlertaMsg.setText(R.string.alerta_enviando_msg);
        txtAlertaDemo.setVisibility(View.GONE);
        txtStatusApi.setText(R.string.alerta_status_pendente);
        txtStatusApi.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        txtStatusContatos.setText(R.string.alerta_status_pendente);
        txtStatusContatos.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        txtStatusLocalizacao.setText(R.string.alerta_status_pendente);
        txtStatusLocalizacao.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        btnTentarNovamente.setVisibility(View.GONE);
    }

    private void aplicarResultado(ResultadoAlerta resultado) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        int corOk = ContextCompat.getColor(this, R.color.safe);
        int corErro = ContextCompat.getColor(this, R.color.emergency);
        int corSec = ContextCompat.getColor(this, R.color.text_secondary);

        if (resultado.apiSucesso) {
            imgAlertaEstado.setImageResource(R.drawable.ic_check_circle);
            imgAlertaEstado.setColorFilter(corOk);
            txtAlertaTitulo.setText(R.string.alerta_enviado);
            txtAlertaMsg.setText(resultado.demo ? R.string.alerta_demo_msg : R.string.alerta_enviado_msg);
            txtStatusApi.setText(R.string.alerta_status_enviado);
            txtStatusApi.setTextColor(corOk);
        } else {
            imgAlertaEstado.setImageResource(R.drawable.ic_error);
            imgAlertaEstado.setColorFilter(corErro);
            txtAlertaTitulo.setText(R.string.alerta_falha);
            txtAlertaMsg.setText(R.string.alerta_falha_msg);
            txtStatusApi.setText(R.string.alerta_status_falha);
            txtStatusApi.setTextColor(corErro);
            btnTentarNovamente.setVisibility(View.VISIBLE);
        }

        txtAlertaDemo.setVisibility(resultado.demo ? View.VISIBLE : View.GONE);

        if (resultado.contatosCadastrados) {
            txtStatusContatos.setText(R.string.alerta_status_sem_confirmacao);
        } else {
            txtStatusContatos.setText(R.string.alerta_status_nenhum_contato);
        }
        txtStatusContatos.setTextColor(corSec);

        if (resultado.localizacaoOk) {
            txtStatusLocalizacao.setText(R.string.alerta_status_disponivel);
            txtStatusLocalizacao.setTextColor(corOk);
            txtLocalResumo.setText(resultado.textoLocalizacao);
            txtLocalResumo.setVisibility(View.VISIBLE);
            btnCompartilhar.setVisibility(View.VISIBLE);
        } else {
            txtStatusLocalizacao.setText(R.string.alerta_status_indisponivel);
            txtStatusLocalizacao.setTextColor(corErro);
            txtLocalResumo.setVisibility(View.GONE);
            btnCompartilhar.setVisibility(View.GONE);
        }

        String detalhe = resultado.mensagemApi == null
                ? (resultado.demo ? "modo demonstração" : "")
                : resultado.mensagemApi;
        eventos.registrar(
                resultado.apiSucesso ? TipoEvento.ALERTA_ENVIADO : TipoEvento.ALERTA_FALHOU,
                getString(resultado.apiSucesso ? R.string.evento_alerta_enviado : R.string.evento_alerta_falhou),
                detalhe);
    }

    private void compartilharLocalizacao() {
        String texto = txtLocalResumo.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, texto);
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.compartilhar_titulo)));
        } catch (Exception e) {
            android.widget.Toast.makeText(this, R.string.sem_app_compartilhar,
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void voltarInicio() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
