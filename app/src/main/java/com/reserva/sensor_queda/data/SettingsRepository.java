package com.reserva.sensor_queda.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persistencia local das preferencias e dos contatos de emergencia. */
public class SettingsRepository {

    public static final int TRANSPORTE_MOTO = 0;
    public static final int TRANSPORTE_BIKE = 1;
    public static final int TRANSPORTE_CAMINHADA = 2;

    /** Endpoint real do sistema de telemetria/emergencia. */
    public static final String DEFAULT_ENDPOINT = "https://ideaberta.com.br/socorro/";

    private static final String PREFS = "saferide_prefs";
    private static final String KEY_MONITORING = "monitoring_enabled";
    private static final String KEY_TRANSPORT = "transport_mode";
    private static final String KEY_CHECKIN_ENABLED = "checkin_enabled";
    private static final String KEY_CHECKIN_INTERVAL = "checkin_interval";
    private static final String KEY_DEMO = "demo_mode";
    private static final String KEY_ENDPOINT = "api_endpoint";
    private static final String KEY_USUARIO = "usuario_nome";
    private static final String KEY_CONTACTS = "emergency_contacts";

    private static volatile SettingsRepository instancia;

    private final SharedPreferences prefs;

    private SettingsRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static SettingsRepository get(Context context) {
        if (instancia == null) {
            synchronized (SettingsRepository.class) {
                if (instancia == null) {
                    instancia = new SettingsRepository(context);
                }
            }
        }
        return instancia;
    }

    public boolean isMonitoringEnabled() {
        return prefs.getBoolean(KEY_MONITORING, false);
    }

    public void setMonitoringEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MONITORING, enabled).apply();
    }

    public int getTransportMode() {
        return prefs.getInt(KEY_TRANSPORT, TRANSPORTE_MOTO);
    }

    public void setTransportMode(int mode) {
        prefs.edit().putInt(KEY_TRANSPORT, mode).apply();
    }

    public boolean isCheckInEnabled() {
        return prefs.getBoolean(KEY_CHECKIN_ENABLED, false);
    }

    public void setCheckInEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_CHECKIN_ENABLED, enabled).apply();
    }

    public int getCheckInIntervalMinutes() {
        return prefs.getInt(KEY_CHECKIN_INTERVAL, 30);
    }

    public void setCheckInIntervalMinutes(int minutos) {
        prefs.edit().putInt(KEY_CHECKIN_INTERVAL, minutos).apply();
    }

    public boolean isDemoMode() {
        return prefs.getBoolean(KEY_DEMO, false);
    }

    public void setDemoMode(boolean demo) {
        prefs.edit().putBoolean(KEY_DEMO, demo).apply();
    }

    public String getEndpoint() {
        return prefs.getString(KEY_ENDPOINT, DEFAULT_ENDPOINT);
    }

    public void setEndpoint(String endpoint) {
        prefs.edit().putString(KEY_ENDPOINT, endpoint).apply();
    }

    public String getUsuario() {
        return prefs.getString(KEY_USUARIO, "Usuário SafeRide");
    }

    public void setUsuario(String nome) {
        prefs.edit().putString(KEY_USUARIO, nome).apply();
    }

    public List<EmergencyContact> getContacts() {
        List<EmergencyContact> contatos = new ArrayList<>();
        String raw = prefs.getString(KEY_CONTACTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.optJSONObject(i);
                if (json != null) {
                    contatos.add(EmergencyContact.fromJson(json));
                }
            }
        } catch (JSONException ignored) {
        }
        return contatos;
    }

    public void saveContacts(List<EmergencyContact> contatos) {
        JSONArray array = new JSONArray();
        for (EmergencyContact contato : contatos) {
            array.put(contato.toJson());
        }
        prefs.edit().putString(KEY_CONTACTS, array.toString()).apply();
    }
}
