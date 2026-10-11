package com.reserva.sensor_queda.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Acesso ao historico de eventos fora da thread de interface. */
public class EventRepository {

    public interface Callback {
        void onResult(List<EventEntity> events);
    }

    private static volatile EventRepository instancia;

    private final EventDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private EventRepository(Context context) {
        this.dao = AppDatabase.getInstance(context).eventDao();
    }

    public static EventRepository get(Context context) {
        if (instancia == null) {
            synchronized (EventRepository.class) {
                if (instancia == null) {
                    instancia = new EventRepository(context.getApplicationContext());
                }
            }
        }
        return instancia;
    }

    public void registrar(String type, String title, String detail) {
        EventEntity evento = new EventEntity(type, System.currentTimeMillis(), title, detail);
        executor.execute(() -> dao.insert(evento));
    }

    public void listar(List<String> types, Callback callback) {
        executor.execute(() -> {
            final List<EventEntity> eventos =
                    (types == null || types.isEmpty()) ? dao.getAll() : dao.findByTypes(types);
            main.post(() -> callback.onResult(eventos));
        });
    }
}
