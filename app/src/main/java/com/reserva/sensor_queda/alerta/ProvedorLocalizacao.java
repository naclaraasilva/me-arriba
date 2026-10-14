package com.reserva.sensor_queda.alerta;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Obtem a ultima localizacao conhecida e um resumo legivel. */
public class ProvedorLocalizacao {

    public static class InfoLocalizacao {
        public boolean ok;
        public double lat;
        public double lon;
        public String text;
        public Double velocidadeKmh;
    }

    public interface Callback {
        void onResult(InfoLocalizacao info);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void obterLocalizacao(Context context, Callback callback) {
        final Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            InfoLocalizacao info = new InfoLocalizacao();
            boolean permitido = ContextCompat.checkSelfPermission(appContext,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(appContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (permitido) {
                LocationManager lm = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
                Location melhor = null;
                if (lm != null) {
                    for (String provider : new String[]{
                            LocationManager.GPS_PROVIDER,
                            LocationManager.NETWORK_PROVIDER,
                            LocationManager.PASSIVE_PROVIDER}) {
                        try {
                            Location l = lm.getLastKnownLocation(provider);
                            if (l != null && (melhor == null || l.getTime() > melhor.getTime())) {
                                melhor = l;
                            }
                        } catch (SecurityException ignored) {
                        }
                    }
                }
                if (melhor != null) {
                    info.ok = true;
                    info.lat = melhor.getLatitude();
                    info.lon = melhor.getLongitude();
                    if (melhor.hasSpeed()) {
                        info.velocidadeKmh = melhor.getSpeed() * 3.6;
                    }
                    info.text = formatar(appContext, info.lat, info.lon);
                }
            }

            final InfoLocalizacao resultado = info;
            MAIN.post(() -> callback.onResult(resultado));
        });
    }

    private static String formatar(Context context, double lat, double lon) {
        try {
            if (Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(context, new Locale("pt", "BR"));
                List<Address> enderecos = geocoder.getFromLocation(lat, lon, 1);
                if (enderecos != null && !enderecos.isEmpty()) {
                    Address endereco = enderecos.get(0);
                    StringBuilder sb = new StringBuilder();
                    if (endereco.getThoroughfare() != null) {
                        sb.append(endereco.getThoroughfare());
                    }
                    if (endereco.getSubThoroughfare() != null) {
                        sb.append(", ").append(endereco.getSubThoroughfare());
                    }
                    if (endereco.getLocality() != null) {
                        if (sb.length() > 0) {
                            sb.append(" - ");
                        }
                        sb.append(endereco.getLocality());
                    }
                    if (sb.length() > 0) {
                        return sb.toString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return String.format(Locale.US, "%.5f, %.5f", lat, lon);
    }
}
