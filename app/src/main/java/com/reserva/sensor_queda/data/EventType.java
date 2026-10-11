package com.reserva.sensor_queda.data;

/** Tipos de evento persistidos no historico. */
public final class EventType {

    public static final String QUEDA_DETECTADA = "queda_detectada";
    public static final String FALSO_POSITIVO = "falso_positivo";
    public static final String ALERTA_SOLICITADO = "alerta_solicitado";
    public static final String ALERTA_ENVIADO = "alerta_enviado";
    public static final String ALERTA_FALHOU = "alerta_falhou";
    public static final String CHECKIN = "checkin";

    private EventType() {
    }
}
