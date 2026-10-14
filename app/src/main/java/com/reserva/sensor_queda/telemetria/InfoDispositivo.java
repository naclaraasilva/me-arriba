package com.reserva.sensor_queda.telemetria;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;

import java.util.UUID;

/** Informacoes do aparelho enviadas na telemetria. */
public final class InfoDispositivo {

    private InfoDispositivo() {
    }

    public static String getModelo() {
        String fabricante = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
        String modelo = Build.MODEL == null ? "" : Build.MODEL;
        String nome = (fabricante + " " + modelo).trim();
        return nome.isEmpty() ? "Android" : nome;
    }

    public static String getIdDispositivo(Context context) {
        try {
            String id = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (id != null && !id.isEmpty()) {
                return id;
            }
        } catch (Exception ignored) {
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences("me_arriba_device", Context.MODE_PRIVATE);
        String id = prefs.getString("device_id", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", id).apply();
        }
        return id;
    }

    public static int getBateria(Context context) {
        try {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) {
                return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
        } catch (Exception ignored) {
        }
        return -1;
    }
}
