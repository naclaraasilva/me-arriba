package com.reserva.sensor_queda.dados;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persistencia local das preferencias e dos contatos de emergencia. */
public class RepositorioConfiguracoes {

    public static final int TRANSPORTE_MOTO = 0;
    public static final int TRANSPORTE_BICICLETA = 1;
    public static final int TRANSPORTE_CAMINHADA = 2;

    /** Endpoint real do sistema de telemetria/emergencia. */
    public static final String DEFAULT_ENDPOINT = "https://ideaberta.com.br/socorro/";

    private static final String PREFS = "me_arriba_prefs";
    private static final String KEY_MONITORING = "monitoring_enabled";
    private static final String KEY_TRANSPORT = "transport_mode";
    private static final String KEY_CHECKIN_ENABLED = "checkin_enabled";
    private static final String KEY_CHECKIN_INTERVAL = "checkin_interval";
    private static final String KEY_DEMO = "demo_mode";
    private static final String KEY_ENDPOINT = "api_endpoint";
    private static final String KEY_USUARIO = "usuario_nome";
    private static final String KEY_CONTACTS = "emergency_contacts";

    private static volatile RepositorioConfiguracoes instancia;

    private final SharedPreferences prefs;

    private RepositorioConfiguracoes(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static RepositorioConfiguracoes get(Context context) {
        if (instancia == null) {
            synchronized (RepositorioConfiguracoes.class) {
                if (instancia == null) {
                    instancia = new RepositorioConfiguracoes(context);
                }
            }
        }
        return instancia;
    }

    public boolean isMonitoramentoAtivo() {
        return prefs.getBoolean(KEY_MONITORING, false);
    }

    public void setMonitoramentoAtivo(boolean enabled) {
        prefs.edit().putBoolean(KEY_MONITORING, enabled).apply();
    }

    public int getModoTransporte() {
        return prefs.getInt(KEY_TRANSPORT, TRANSPORTE_MOTO);
    }

    public void setModoTransporte(int mode) {
        prefs.edit().putInt(KEY_TRANSPORT, mode).apply();
    }

    public boolean isCheckInAtivo() {
        return prefs.getBoolean(KEY_CHECKIN_ENABLED, false);
    }

    public void setCheckInAtivo(boolean enabled) {
        prefs.edit().putBoolean(KEY_CHECKIN_ENABLED, enabled).apply();
    }

    public int getIntervaloCheckIn() {
        return prefs.getInt(KEY_CHECKIN_INTERVAL, 30);
    }

    public void setIntervaloCheckIn(int minutos) {
        prefs.edit().putInt(KEY_CHECKIN_INTERVAL, minutos).apply();
    }

    public boolean isModoDemo() {
        return prefs.getBoolean(KEY_DEMO, false);
    }

    public void setModoDemo(boolean demo) {
        prefs.edit().putBoolean(KEY_DEMO, demo).apply();
    }

    public String getEndpoint() {
        return prefs.getString(KEY_ENDPOINT, DEFAULT_ENDPOINT);
    }

    public void setEndpoint(String endpoint) {
        prefs.edit().putString(KEY_ENDPOINT, endpoint).apply();
    }

    public String getUsuario() {
        return prefs.getString(KEY_USUARIO, "Usuário Me arriba");
    }

    public void setUsuario(String nome) {
        prefs.edit().putString(KEY_USUARIO, nome).apply();
    }

    public List<ContatoEmergencia> getContatos() {
        List<ContatoEmergencia> contatos = new ArrayList<>();
        String raw = prefs.getString(KEY_CONTACTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.optJSONObject(i);
                if (json != null) {
                    contatos.add(ContatoEmergencia.fromJson(json));
                }
            }
        } catch (JSONException ignored) {
        }
        return contatos;
    }

    public void salvarContatos(List<ContatoEmergencia> contatos) {
        JSONArray array = new JSONArray();
        for (ContatoEmergencia contato : contatos) {
            array.put(contato.toJson());
        }
        prefs.edit().putString(KEY_CONTACTS, array.toString()).apply();
    }
}
