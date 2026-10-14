package com.reserva.sensor_queda.alerta;

/** Abstrai o canal de envio do alerta para a API/backend. */
public interface EnviadorAlerta {

    interface Callback {
        void onResult(ResultadoAlerta resultado);
    }

    void enviar(String payload, Callback callback);
}
