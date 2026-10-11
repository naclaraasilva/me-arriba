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
import com.reserva.sensor_queda.alert.LocationProvider;
import com.reserva.sensor_queda.data.EventRepository;
import com.reserva.sensor_queda.data.EventType;
import com.reserva.sensor_queda.data.SettingsRepository;
import com.reserva.sensor_queda.sensor.FallDetector;
import com.reserva.sensor_queda.telemetry.TelemetrySender;
import com.reserva.sensor_queda.ui.AlertStatusActivity;
import com.reserva.sensor_queda.ui.FallDetectedActivity;
import com.reserva.sensor_queda.ui.HistoryActivity;
import com.reserva.sensor_queda.ui.SettingsActivity;
import com.reserva.sensor_queda.widget.RingProgressView;

public class MainActivity extends AppCompatActivity implements FallDetector.Listener {

    private RingProgressView ringStatus;
    private ImageView imgShield;
    private TextView txtStatus;
    private TextView txtStatusSub;
    private TextView txtLocalizacao;
    private TextView txtTransporte;
    private TextView txtClima;
    private TextView txtSensores;
    private MaterialButton btnMonitorar;
    private MaterialButton btnPanico;
    private MaterialButton btnSimular;

    private FallDetector fallDetector;
    private SettingsRepository settings;
    private EventRepository events;

    private boolean monitoramentoAtivo = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable checkInTask = new Runnable() {
        @Override
        public void run() {
            mostrarCheckIn();
            agendarCheckIn();
        }
    };

    private static final long INTERVALO_TELEMETRIA_MS = 15000L;

    private final Runnable telemetriaTask = new Runnable() {
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

        settings = SettingsRepository.get(this);
        events = EventRepository.get(this);
        fallDetector = new FallDetector(this, this);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> atualizarCartaoLocalizacao());

        bindViews();
        configurarAcoes();

        monitoramentoAtivo = settings.isMonitoringEnabled();
        atualizarInterface();
        atualizarStatusSensores();
    }

    private void bindViews() {
        ringStatus = findViewById(R.id.ringStatus);
        imgShield = findViewById(R.id.imgShield);
        txtStatus = findViewById(R.id.txtStatus);
        txtStatusSub = findViewById(R.id.txtStatusSub);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtTransporte = findViewById(R.id.txtTransporte);
        txtClima = findViewById(R.id.txtClima);
        txtSensores = findViewById(R.id.txtSensores);
        btnMonitorar = findViewById(R.id.btnMonitorar);
        btnPanico = findViewById(R.id.btnPanico);
        btnSimular = findViewById(R.id.btnSimular);

        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
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
                .setPositiveButton(R.string.confirmar, (d, w) -> fallDetector.simulate())
                .show());
    }

    private void alternarMonitoramento() {
        if (!monitoramentoAtivo) {
            if (!fallDetector.isAvailable()) {
                Toast.makeText(this, R.string.sensor_indisponivel, Toast.LENGTH_LONG).show();
                return;
            }
            monitoramentoAtivo = true;
            settings.setMonitoringEnabled(true);
            atualizarInterface();
            fallDetector.start();
            solicitarPermissaoLocalizacao();
            agendarCheckIn();
            enviarTelemetriaNormal();
            agendarTelemetria();
        } else {
            monitoramentoAtivo = false;
            settings.setMonitoringEnabled(false);
            atualizarInterface();
            fallDetector.stop();
            handler.removeCallbacks(checkInTask);
            handler.removeCallbacks(telemetriaTask);
        }
    }

    private void acionarPanico() {
        Intent intent = new Intent(this, AlertStatusActivity.class);
        startActivity(intent);
    }

    private void atualizarInterface() {
        if (monitoramentoAtivo) {
            ringStatus.setCor(ContextCompat.getColor(this, R.color.safe));
            ringStatus.setProgresso(1f);
            imgShield.setColorFilter(ContextCompat.getColor(this, R.color.safe));
            txtStatus.setText(R.string.monitoring_active);
            txtStatusSub.setText(R.string.detecting_falls);
            btnMonitorar.setText(R.string.parar_monitoramento);
        } else {
            ringStatus.setCor(ContextCompat.getColor(this, R.color.divider));
            ringStatus.setProgresso(1f);
            imgShield.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            txtStatus.setText(R.string.monitoring_inactive);
            txtStatusSub.setText(R.string.monitoring_off_hint);
            btnMonitorar.setText(R.string.iniciar_monitoramento);
        }
        txtTransporte.setText(labelTransporte(settings.getTransportMode()));
        txtClima.setText(R.string.clima_indisponivel);
        atualizarCartaoLocalizacao();
    }

    private String labelTransporte(int modo) {
        switch (modo) {
            case SettingsRepository.TRANSPORTE_BIKE:
                return getString(R.string.transporte_bike);
            case SettingsRepository.TRANSPORTE_CAMINHADA:
                return getString(R.string.transporte_caminhada);
            default:
                return getString(R.string.transporte_moto);
        }
    }

    private void atualizarStatusSensores() {
        if (!fallDetector.isAvailable()) {
            txtSensores.setText(R.string.sensor_indisponivel);
            txtSensores.setTextColor(ContextCompat.getColor(this, R.color.emergency));
        } else if (!fallDetector.hasGyroscope()) {
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
        LocationProvider.getLocation(this, info -> {
            if (info.ok && info.text != null) {
                txtLocalizacao.setText(info.text);
            } else {
                txtLocalizacao.setText(R.string.localizacao_indisponivel);
            }
        });
    }

    private void agendarCheckIn() {
        handler.removeCallbacks(checkInTask);
        if (!monitoramentoAtivo || !settings.isCheckInEnabled()) {
            return;
        }
        long intervalo = settings.getCheckInIntervalMinutes() * 60_000L;
        handler.postDelayed(checkInTask, intervalo);
    }

    private void agendarTelemetria() {
        handler.removeCallbacks(telemetriaTask);
        if (!monitoramentoAtivo || settings.isDemoMode() || settings.getEndpoint().isEmpty()) {
            return;
        }
        handler.postDelayed(telemetriaTask, INTERVALO_TELEMETRIA_MS);
    }

    private void enviarTelemetriaNormal() {
        if (settings.isDemoMode() || settings.getEndpoint().isEmpty()) {
            return;
        }
        LocationProvider.getLocation(this, info -> {
            TelemetrySender.Sample sample = new TelemetrySender.Sample();
            sample.queda = false;
            sample.demo = false;
            if (info.ok) {
                sample.latitude = info.lat;
                sample.longitude = info.lon;
                sample.velocidadeKmh = info.speedKmh;
            }
            sample.temperatura = fallDetector.getUltimaTemperatura();
            sample.pressao = fallDetector.getUltimaPressao();
            TelemetrySender.enviar(this, settings.getEndpoint(), sample,
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
                        events.registrar(EventType.CHECKIN, getString(R.string.evento_checkin),
                                getString(R.string.checkin_sim)))
                .setNegativeButton(R.string.checkin_ajuda, (d, w) -> acionarPanico())
                .show();
    }

    @Override
    public void onPotentialFall(boolean simulated) {
        events.registrar(EventType.QUEDA_DETECTADA, getString(R.string.evento_queda_detectada),
                simulated ? getString(R.string.queda_teste) : null);
        Intent intent = new Intent(this, FallDetectedActivity.class);
        intent.putExtra(FallDetectedActivity.EXTRA_SIMULADO, simulated);
        intent.putExtra(FallDetectedActivity.EXTRA_G, fallDetector.getUltimoImpactoG());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        monitoramentoAtivo = settings.isMonitoringEnabled();
        atualizarInterface();
        if (monitoramentoAtivo) {
            fallDetector.start();
            agendarCheckIn();
            agendarTelemetria();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fallDetector.stop();
        handler.removeCallbacks(checkInTask);
        handler.removeCallbacks(telemetriaTask);
    }
}
