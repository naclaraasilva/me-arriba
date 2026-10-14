package com.reserva.sensor_queda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.reserva.sensor_queda.MainActivity;
import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.dados.EventoEntidade;
import com.reserva.sensor_queda.dados.RepositorioEventos;
import com.reserva.sensor_queda.dados.TipoEvento;

import java.util.Arrays;
import java.util.List;

/** Tela 5 - Historico de eventos. */
public class HistoricoActivity extends AppCompatActivity {

    private AdaptadorEvento adapter;
    private RepositorioEventos eventos;
    private View estadoVazio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventos = RepositorioEventos.get(this);
        estadoVazio = findViewById(R.id.estadoVazio);

        RecyclerView recycler = findViewById(R.id.recyclerEventos);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdaptadorEvento(this::mostrarDetalhes);
        recycler.setAdapter(adapter);

        Chip chipTodos = findViewById(R.id.chipTodos);
        Chip chipQuedas = findViewById(R.id.chipQuedas);
        Chip chipAlertas = findViewById(R.id.chipAlertas);

        chipTodos.setOnClickListener(v -> carregar(null));
        chipQuedas.setOnClickListener(v -> carregar(Arrays.asList(
                TipoEvento.QUEDA_DETECTADA, TipoEvento.FALSO_POSITIVO)));
        chipAlertas.setOnClickListener(v -> carregar(Arrays.asList(
                TipoEvento.ALERTA_SOLICITADO, TipoEvento.ALERTA_ENVIADO, TipoEvento.ALERTA_FALHOU)));

        BottomNavigationView navegacaoInferior = findViewById(R.id.navegacaoInferior);
        navegacaoInferior.setSelectedItemId(R.id.nav_historico);
        navegacaoInferior.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_inicio) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return false;
            }
            return true;
        });

        carregar(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar(null);
        ((Chip) findViewById(R.id.chipTodos)).setChecked(true);
    }

    private void carregar(List<String> filtro) {
        eventos.listar(filtro, eventos -> {
            adapter.setItens(eventos);
            estadoVazio.setVisibility(eventos.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void mostrarDetalhes(EventoEntidade evento) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append(UtilitariosEvento.formatarDataHora(evento.timestamp));
        if (evento.detail != null && !evento.detail.isEmpty()) {
            mensagem.append("\n\n").append(evento.detail);
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(UtilitariosEvento.tituloRes(evento.type))
                .setMessage(mensagem.toString())
                .setPositiveButton(R.string.fechar, null)
                .show();
    }
}
