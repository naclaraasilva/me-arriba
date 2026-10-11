package com.reserva.sensor_queda.alert;

import android.os.Handler;
import android.os.Looper;

/**
 * Envio simulado usado enquanto a API real nao esta configurada.
 * Deixa explicito que se trata de MODO DEMONSTRACAO.
 */
public class MockAlertSender implements AlertSender {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void send(String payload, Callback callback) {
        handler.postDelayed(() -> {
            AlertResult result = new AlertResult();
            result.demo = true;
            result.apiOk = true;
            result.apiMessage = "Envio simulado (modo demonstração)";
            callback.onResult(result);
        }, 1500L);
    }
}
