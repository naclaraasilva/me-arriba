package com.reserva.sensor_queda.dados;

import org.json.JSONException;
import org.json.JSONObject;

/** Contato de emergencia cadastrado pelo usuario. */
public class ContatoEmergencia {

    public String nome;
    public String telefone;

    public ContatoEmergencia(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("nome", nome);
            json.put("telefone", telefone);
        } catch (JSONException ignored) {
        }
        return json;
    }

    public static ContatoEmergencia fromJson(JSONObject json) {
        return new ContatoEmergencia(
                json.optString("nome", ""),
                json.optString("telefone", "")
        );
    }
}
