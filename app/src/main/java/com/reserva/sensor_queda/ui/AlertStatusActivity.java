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
import com.reserva.sensor_queda.alert.AlertResult;
import com.reserva.sensor_queda.alert.AlertService;
import com.reserva.sensor_queda.data.EventRepository;
import com.reserva.sensor_queda.data.EventType;

/** Tela 3 - Status real da tentativa de emergencia. */
public class AlertStatusActivity extends AppCompatActivity {

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

    private EventRepository events;
    private AlertService alertService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        events = EventRepository.get(this);
        alertService = new AlertService(this);

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

        TextView titulo = findViewById(R.id.txtToolbarTitle);
        titulo.setText(R.string.alerta_titulo);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnTentarNovamente.setOnClickListener(v -> enviar());
        btnCompartilhar.setOnClickListener(v -> compartilharLocalizacao());
        findViewById(R.id.btnVoltarInicio).setOnClickListener(v -> voltarInicio());

        events.registrar(EventType.ALERTA_SOLICITADO, getString(R.string.evento_alerta_solicitado),
                null);

        enviar();
    }

    private void enviar() {
        estadoEnviando();
        float g = getIntent().getFloatExtra(FallDetectedActivity.EXTRA_G, 0f);
        alertService.enviarAlerta(g, this::aplicarResultado);
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

    private void aplicarResultado(AlertResult resultado) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        int corOk = ContextCompat.getColor(this, R.color.safe);
        int corErro = ContextCompat.getColor(this, R.color.emergency);
        int corSec = ContextCompat.getColor(this, R.color.text_secondary);

        if (resultado.apiOk) {
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

        if (resultado.locationOk) {
            txtStatusLocalizacao.setText(R.string.alerta_status_disponivel);
            txtStatusLocalizacao.setTextColor(corOk);
            txtLocalResumo.setText(resultado.locationText);
            txtLocalResumo.setVisibility(View.VISIBLE);
            btnCompartilhar.setVisibility(View.VISIBLE);
        } else {
            txtStatusLocalizacao.setText(R.string.alerta_status_indisponivel);
            txtStatusLocalizacao.setTextColor(corErro);
            txtLocalResumo.setVisibility(View.GONE);
            btnCompartilhar.setVisibility(View.GONE);
        }

        String detalhe = resultado.apiMessage == null
                ? (resultado.demo ? "modo demonstração" : "")
                : resultado.apiMessage;
        events.registrar(
                resultado.apiOk ? EventType.ALERTA_ENVIADO : EventType.ALERTA_FALHOU,
                getString(resultado.apiOk ? R.string.evento_alerta_enviado : R.string.evento_alerta_falhou),
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
