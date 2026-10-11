package com.reserva.sensor_queda.data;

import org.json.JSONException;
import org.json.JSONObject;

/** Contato de emergencia cadastrado pelo usuario. */
public class EmergencyContact {

    public String nome;
    public String telefone;

    public EmergencyContact(String nome, String telefone) {
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

    public static EmergencyContact fromJson(JSONObject json) {
        return new EmergencyContact(
                json.optString("nome", ""),
                json.optString("telefone", "")
        );
    }
}
