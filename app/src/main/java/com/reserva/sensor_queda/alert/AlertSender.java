package com.reserva.sensor_queda.alert;

/** Abstrai o canal de envio do alerta para a API/backend. */
public interface AlertSender {

    interface Callback {
        void onResult(AlertResult result);
    }

    void send(String payload, Callback callback);
}
