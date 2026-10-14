package com.reserva.sensor_queda.dados;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Acesso ao historico de eventos fora da thread de interface. */
public class RepositorioEventos {

    public interface Callback {
        void onResult(List<EventoEntidade> eventos);
    }

    private static volatile RepositorioEventos instancia;

    private final EventoDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private RepositorioEventos(Context context) {
        this.dao = BancoDados.obterInstancia(context).eventoDao();
    }

    public static RepositorioEventos get(Context context) {
        if (instancia == null) {
            synchronized (RepositorioEventos.class) {
                if (instancia == null) {
                    instancia = new RepositorioEventos(context.getApplicationContext());
                }
            }
        }
        return instancia;
    }

    public void registrar(String tipo, String titulo, String detalhe) {
        EventoEntidade evento = new EventoEntidade(tipo, System.currentTimeMillis(), titulo, detalhe);
        executor.execute(() -> dao.inserir(evento));
    }

    public void listar(List<String> tipos, Callback callback) {
        executor.execute(() -> {
            final List<EventoEntidade> eventos =
                    (tipos == null || tipos.isEmpty()) ? dao.listarTodos() : dao.buscarPorTipos(tipos);
            main.post(() -> callback.onResult(eventos));
        });
    }
}
