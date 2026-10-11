package com.reserva.sensor_queda.alert;

/** Resultado real da tentativa de envio de um alerta. */
public class AlertResult {

    /** A API/servidor confirmou o recebimento. */
    public boolean apiOk;

    /** Indica envio simulado (modo demonstracao), sem integracao real. */
    public boolean demo;

    /** Ha localizacao disponivel para compartilhar. */
    public boolean locationOk;

    public String locationText;

    public String apiMessage;

    public boolean contatosCadastrados;
}
