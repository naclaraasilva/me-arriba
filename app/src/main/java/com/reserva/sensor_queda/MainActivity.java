package com.reserva.sensor_queda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.reserva.sensor_queda.alerta.ProvedorLocalizacao;
import com.reserva.sensor_queda.dados.RepositorioEventos;
import com.reserva.sensor_queda.dados.TipoEvento;
import com.reserva.sensor_queda.dados.RepositorioConfiguracoes;
import com.reserva.sensor_queda.sensor.DetectorQueda;
import com.reserva.sensor_queda.telemetria.EnviadorTelemetria;
import com.reserva.sensor_queda.ui.StatusAlertaActivity;
import com.reserva.sensor_queda.ui.QuedaDetectadaActivity;
import com.reserva.sensor_queda.ui.HistoricoActivity;
import com.reserva.sensor_queda.ui.ConfiguracoesActivity;
import com.reserva.sensor_queda.clima.ProvedorClima;
import com.reserva.sensor_queda.componente.VisaoAnelProgresso;

public class MainActivity extends AppCompatActivity implements DetectorQueda.Listener {

    private VisaoAnelProgresso anelStatus;
    private ImageView imgEscudo;
    private TextView txtStatus;
    private TextView txtStatusDetalhe;
    private TextView txtLocalizacao;
    private TextView txtTransporte;
    private TextView txtClima;
    private TextView txtSensores;
    private MaterialButton btnMonitorar;
    private MaterialButton btnPanico;
    private MaterialButton btnSimular;

    private DetectorQueda detectorQueda;
    private RepositorioConfiguracoes configuracoes;
    private RepositorioEventos eventos;

    private boolean monitoramentoAtivo = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tarefaCheckIn = new Runnable() {
        @Override
        public void run() {
            mostrarCheckIn();
            agendarCheckIn();
        }
    };

    private static final long INTERVALO_TELEMETRIA_MS = 15000L;

    private final Runnable tarefaTelemetria = new Runnable() {
        @Override
        public void run() {
            enviarTelemetriaNormal();
            agendarTelemetria();
        }
    };

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configuracoes = RepositorioConfiguracoes.get(this);
        eventos = RepositorioEventos.get(this);
        detectorQueda = new DetectorQueda(this, this);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                resultado -> atualizarCartaoLocalizacao());

        mapearViews();
        configurarAcoes();

        monitoramentoAtivo = configuracoes.isMonitoramentoAtivo();
        atualizarInterface();
        atualizarStatusSensores();
    }

    private void mapearViews() {
        anelStatus = findViewById(R.id.anelStatus);
        imgEscudo = findViewById(R.id.imgEscudo);
        txtStatus = findViewById(R.id.txtStatus);
        txtStatusDetalhe = findViewById(R.id.txtStatusDetalhe);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtTransporte = findViewById(R.id.txtTransporte);
        txtClima = findViewById(R.id.txtClima);
        txtSensores = findViewById(R.id.txtSensores);
        btnMonitorar = findViewById(R.id.btnMonitorar);
        btnPanico = findViewById(R.id.btnPanico);
        btnSimular = findViewById(R.id.btnSimular);

        ImageButton btnConfiguracoes = findViewById(R.id.btnConfiguracoes);
        btnConfiguracoes.setOnClickListener(v ->
                startActivity(new Intent(this, ConfiguracoesActivity.class)));

        BottomNavigationView navegacaoInferior = findViewById(R.id.navegacaoInferior);
        navegacaoInferior.setSelectedItemId(R.id.nav_inicio);
        navegacaoInferior.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
                overridePendingTransition(0, 0);
                return false;
            }
            return true;
        });
    }

    private void configurarAcoes() {
        btnMonitorar.setOnClickListener(v -> alternarMonitoramento());

        btnPanico.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirmar_panico_titulo)
                .setMessage(R.string.confirmar_panico_msg)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.confirmar, (d, w) -> acionarPanico())
                .show());

        btnPanico.setOnLongClickListener(v -> {
            acionarPanico();
            return true;
        });

        btnSimular.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirmar_simular)
                .setMessage(R.string.simular_msg)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.confirmar, (d, w) -> detectorQueda.simular())
                .show());
    }

    private void alternarMonitoramento() {
        if (!monitoramentoAtivo) {
            if (!detectorQueda.estaDisponivel()) {
                Toast.makeText(this, R.string.sensor_indisponivel, Toast.LENGTH_LONG).show();
                return;
            }
            monitoramentoAtivo = true;
            configuracoes.setMonitoramentoAtivo(true);
            atualizarInterface();
            detectorQueda.iniciar();
            solicitarPermissaoLocalizacao();
            agendarCheckIn();
            enviarTelemetriaNormal();
            agendarTelemetria();
        } else {
            monitoramentoAtivo = false;
            configuracoes.setMonitoramentoAtivo(false);
            atualizarInterface();
            detectorQueda.parar();
            handler.removeCallbacks(tarefaCheckIn);
            handler.removeCallbacks(tarefaTelemetria);
        }
    }

    private void acionarPanico() {
        Intent intent = new Intent(this, StatusAlertaActivity.class);
        startActivity(intent);
    }

    private void atualizarInterface() {
        if (monitoramentoAtivo) {
            anelStatus.setCor(ContextCompat.getColor(this, R.color.safe));
            anelStatus.setProgresso(1f);
            imgEscudo.setColorFilter(ContextCompat.getColor(this, R.color.safe));
            txtStatus.setText(R.string.monitoring_active);
            txtStatusDetalhe.setText(R.string.detecting_falls);
            btnMonitorar.setText(R.string.parar_monitoramento);
        } else {
            anelStatus.setCor(ContextCompat.getColor(this, R.color.divider));
            anelStatus.setProgresso(1f);
            imgEscudo.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            txtStatus.setText(R.string.monitoring_inactive);
            txtStatusDetalhe.setText(R.string.monitoring_off_hint);
            btnMonitorar.setText(R.string.iniciar_monitoramento);
        }
        txtTransporte.setText(rotuloTransporte(configuracoes.getModoTransporte()));
        atualizarCartaoLocalizacao();
    }

    private String rotuloTransporte(int modo) {
        switch (modo) {
            case RepositorioConfiguracoes.TRANSPORTE_BICICLETA:
                return getString(R.string.transporte_bike);
            case RepositorioConfiguracoes.TRANSPORTE_CAMINHADA:
                return getString(R.string.transporte_caminhada);
            default:
                return getString(R.string.transporte_moto);
        }
    }

    private void atualizarStatusSensores() {
        if (!detectorQueda.estaDisponivel()) {
            txtSensores.setText(R.string.sensor_indisponivel);
            txtSensores.setTextColor(ContextCompat.getColor(this, R.color.emergency));
        } else if (!detectorQueda.temGiroscopio()) {
            txtSensores.setText(R.string.sensores_ok);
            txtSensores.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        } else {
            txtSensores.setText(R.string.sensores_ok);
            txtSensores.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void solicitarPermissaoLocalizacao() {
        boolean concedida = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!concedida) {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            atualizarCartaoLocalizacao();
        }
    }

    private void atualizarCartaoLocalizacao() {
        ProvedorLocalizacao.obterLocalizacao(this, info -> {
            if (info.ok && info.text != null) {
                txtLocalizacao.setText(info.text);
                carregarClima(info.lat, info.lon);
            } else {
                txtLocalizacao.setText(R.string.localizacao_indisponivel);
                txtClima.setText(R.string.clima_indisponivel);
            }
        });
    }

    private void carregarClima(double lat, double lon) {
        if (!ProvedorClima.temChaveApi()) {
            txtClima.setText(R.string.clima_indisponivel);
            return;
        }
        txtClima.setText(R.string.clima_carregando);
        ProvedorClima.obterClima(lat, lon, info -> {
            if (isFinishing()) {
                return;
            }
            if (info.ok) {
                txtClima.setText(getString(R.string.clima_formato, info.descricao,
                        Math.round(info.temperaturaC)));
            } else {
                txtClima.setText(R.string.clima_indisponivel);
            }
        });
    }

    private void agendarCheckIn() {
        handler.removeCallbacks(tarefaCheckIn);
        if (!monitoramentoAtivo || !configuracoes.isCheckInAtivo()) {
            return;
        }
        long intervalo = configuracoes.getIntervaloCheckIn() * 60_000L;
        handler.postDelayed(tarefaCheckIn, intervalo);
    }

    private void agendarTelemetria() {
        handler.removeCallbacks(tarefaTelemetria);
        if (!monitoramentoAtivo || configuracoes.isModoDemo() || configuracoes.getEndpoint().isEmpty()) {
            return;
        }
        handler.postDelayed(tarefaTelemetria, INTERVALO_TELEMETRIA_MS);
    }

    private void enviarTelemetriaNormal() {
        if (configuracoes.isModoDemo() || configuracoes.getEndpoint().isEmpty()) {
            return;
        }
        ProvedorLocalizacao.obterLocalizacao(this, info -> {
            EnviadorTelemetria.Amostra amostra = new EnviadorTelemetria.Amostra();
            amostra.queda = false;
            amostra.demo = false;
            if (info.ok) {
                amostra.latitude = info.lat;
                amostra.longitude = info.lon;
                amostra.velocidadeKmh = info.velocidadeKmh;
            }
            amostra.temperatura = detectorQueda.getUltimaTemperatura();
            amostra.pressao = detectorQueda.getUltimaPressao();
            EnviadorTelemetria.enviar(this, configuracoes.getEndpoint(), amostra,
                    (ok, msg) -> { /* telemetria periodica silenciosa */ });
        });
    }

    private void mostrarCheckIn() {
        if (isFinishing()) {
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.checkin_titulo)
                .setMessage(R.string.checkin_pergunta)
                .setCancelable(false)
                .setPositiveButton(R.string.checkin_sim, (d, w) ->
                        eventos.registrar(TipoEvento.CHECKIN, getString(R.string.evento_checkin),
                                getString(R.string.checkin_sim)))
                .setNegativeButton(R.string.checkin_ajuda, (d, w) -> acionarPanico())
                .show();
    }

    @Override
    public void aoPotencialQueda(boolean simulated) {
        eventos.registrar(TipoEvento.QUEDA_DETECTADA, getString(R.string.evento_queda_detectada),
                simulated ? getString(R.string.queda_teste) : null);
        Intent intent = new Intent(this, QuedaDetectadaActivity.class);
        intent.putExtra(QuedaDetectadaActivity.EXTRA_SIMULADO, simulated);
        intent.putExtra(QuedaDetectadaActivity.EXTRA_G, detectorQueda.getUltimoImpactoG());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        monitoramentoAtivo = configuracoes.isMonitoramentoAtivo();
        atualizarInterface();
        if (monitoramentoAtivo) {
            detectorQueda.iniciar();
            agendarCheckIn();
            agendarTelemetria();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detectorQueda.parar();
        handler.removeCallbacks(tarefaCheckIn);
        handler.removeCallbacks(tarefaTelemetria);
    }
}
