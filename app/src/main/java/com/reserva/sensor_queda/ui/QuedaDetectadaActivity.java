package com.reserva.sensor_queda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.dados.RepositorioEventos;
import com.reserva.sensor_queda.dados.TipoEvento;
import com.reserva.sensor_queda.componente.VisaoAnelProgresso;

import java.util.Locale;

/** Tela 2 - Possivel queda detectada com contagem regressiva de 30 segundos. */
public class QuedaDetectadaActivity extends AppCompatActivity {

    public static final String EXTRA_SIMULADO = "extra_simulado";
    public static final String EXTRA_G = "extra_g";

    private static final long DURACAO_MS = 30_000L;

    private VisaoAnelProgresso anelContador;
    private TextView txtContador;
    private TextView txtTeste;

    private CountDownTimer timer;
    private RepositorioEventos eventos;
    private boolean simulado;
    private boolean decidido = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_queda_detectada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventos = RepositorioEventos.get(this);
        simulado = getIntent().getBooleanExtra(EXTRA_SIMULADO, false);

        anelContador = findViewById(R.id.anelContador);
        txtContador = findViewById(R.id.txtContador);
        txtTeste = findViewById(R.id.txtTeste);

        if (simulado) {
            txtTeste.setVisibility(View.VISIBLE);
        }

        anelContador.setCor(ContextCompat.getColor(this, R.color.emergency));
        anelContador.setProgresso(1f);

        MaterialButton btnEstouBem = findViewById(R.id.btnEstouBem);
        MaterialButton btnPrecisoAjuda = findViewById(R.id.btnPrecisoAjuda);

        btnEstouBem.setOnClickListener(v -> cancelarAlerta());
        btnPrecisoAjuda.setOnClickListener(v -> solicitarAjuda());

        iniciarContador();
    }

    private void iniciarContador() {
        timer = new CountDownTimer(DURACAO_MS, 1000L) {
            @Override
            public void onTick(long millisRestantes) {
                long segundos = (millisRestantes + 999) / 1000;
                txtContador.setText(String.format(Locale.getDefault(), "%d", segundos));
                anelContador.setProgresso(millisRestantes / (float) DURACAO_MS);
            }

            @Override
            public void onFinish() {
                txtContador.setText("0");
                anelContador.setProgresso(0f);
                if (!decidido) {
                    decidido = true;
                    abrirStatusAlerta();
                }
            }
        };
        timer.start();
    }

    private void cancelarAlerta() {
        if (decidido) {
            return;
        }
        decidido = true;
        if (timer != null) {
            timer.cancel();
        }
        eventos.registrar(TipoEvento.FALSO_POSITIVO, getString(R.string.evento_falso_positivo),
                simulado ? getString(R.string.queda_teste) : null);
        finish();
    }

    private void solicitarAjuda() {
        if (decidido) {
            return;
        }
        decidido = true;
        if (timer != null) {
            timer.cancel();
        }
        abrirStatusAlerta();
    }

    private void abrirStatusAlerta() {
        Intent intent = new Intent(this, StatusAlertaActivity.class);
        intent.putExtra(EXTRA_G, getIntent().getFloatExtra(EXTRA_G, 0f));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}
