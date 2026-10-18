package com.reserva.sensor_queda.alerta;

/** Resultado real da tentativa de envio de um alerta. */
public class ResultadoAlerta {

    /** A API/servidor confirmou o recebimento. */
    public boolean apiSucesso;

    /** Indica envio simulado (modo demonstracao), sem integracao real. */
    public boolean demo;

    /** Ha localizacao disponivel para compartilhar. */
    public boolean localizacaoOk;

    public String textoLocalizacao;

    public String mensagemApi;

    public boolean contatosCadastrados;

    /** Houve tentativa de envio de SMS (existe ao menos um contato). */
    public boolean smsTentado;

    /** Permissao de envio de SMS concedida pelo usuario. */
    public boolean smsPermissaoConcedida;

    /** Quantidade de contatos que receberam o SMS. */
    public int contatosNotificados;

    /** Total de contatos cadastrados. */
    public int contatosTotal;

    /** Detalhe legivel do envio por SMS. */
    public String mensagemSms;
}
