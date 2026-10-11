package com.reserva.sensor_queda.telemetry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.reserva.sensor_queda.data.SettingsRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Envia telemetria/alertas para a API Socorro
 * (https://ideaberta.com.br/socorro/) via POST x-www-form-urlencoded.
 *
 * <p>Campos aceitos pela API: usuario, modelo, device_id, bateria, latitude,
 * longitude, tipo (1=Normal, 2=Queda), queda (forca em G), velocidade (km/h),
 * temperatura (C) e pressao (hPa).</p>
 */
public final class TelemetrySender {

    public interface Callback {
        void onResult(boolean ok, String message);
    }

    /** Amostra de dados a enviar. Campos opcionais podem ser nulos. */
    public static class Sample {
        public boolean queda;
        public float quedaG;
        public Double velocidadeKmh;
        public Float temperatura;
        public Float pressao;
        public Double latitude;
        public Double longitude;
        public boolean demo;
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private TelemetrySender() {
    }

    public static void enviar(Context context, String endpoint, Sample sample, Callback callback) {
        final Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            boolean ok;
            String msg;
            HttpURLConnection conexao = null;
            try {
                String corpo = codificar(montarParametros(appContext, sample));
                conexao = (HttpURLConnection) new URL(endpoint).openConnection();
                conexao.setRequestMethod("POST");
                conexao.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=utf-8");
                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(8000);
                conexao.setDoOutput(true);
                try (OutputStream os = conexao.getOutputStream()) {
                    os.write(corpo.getBytes(StandardCharsets.UTF_8));
                }
                int codigo = conexao.getResponseCode();
                String resposta = lerResposta(conexao);
                ok = codigo >= 200 && codigo < 300 && resposta.contains("\"status\":\"sucesso\"");
                msg = ok ? ("HTTP " + codigo) : ("HTTP " + codigo + " " + resposta);
            } catch (Exception e) {
                ok = false;
                msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
            final boolean okFinal = ok;
            final String msgFinal = msg;
            MAIN.post(() -> callback.onResult(okFinal, msgFinal));
        });
    }

    private static Map<String, String> montarParametros(Context context, Sample sample) {
        SettingsRepository settings = SettingsRepository.get(context);
        Map<String, String> params = new LinkedHashMap<>();

        params.put("usuario", (sample.demo ? "[DEMO] " : "") + settings.getUsuario());
        params.put("modelo", DeviceInfo.getModelo());
        params.put("device_id", DeviceInfo.getDeviceId(context));

        int bateria = DeviceInfo.getBateria(context);
        if (bateria >= 0) {
            params.put("bateria", String.valueOf(bateria));
        }

        params.put("tipo", sample.queda ? "2" : "1");

        if (sample.queda) {
            params.put("queda", String.format(Locale.US, "%.2f", sample.quedaG));
        }
        if (sample.velocidadeKmh != null) {
            params.put("velocidade", String.format(Locale.US, "%.1f", sample.velocidadeKmh));
        }
        if (sample.temperatura != null) {
            params.put("temperatura", String.format(Locale.US, "%.1f", sample.temperatura));
        }
        if (sample.pressao != null) {
            params.put("pressao", String.format(Locale.US, "%.0f", sample.pressao));
        }
        if (sample.latitude != null && sample.longitude != null) {
            params.put("latitude", String.format(Locale.US, "%.6f", sample.latitude));
            params.put("longitude", String.format(Locale.US, "%.6f", sample.longitude));
        }
        return params;
    }

    private static String codificar(Map<String, String> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entrada : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entrada.getKey(), "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(entrada.getValue(), "UTF-8"));
        }
        return sb.toString();
    }

    private static String lerResposta(HttpURLConnection conexao) {
        try {
            InputStream is = conexao.getResponseCode() >= 400
                    ? conexao.getErrorStream()
                    : conexao.getInputStream();
            if (is == null) {
                return "";
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) {
                    sb.append(linha);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "";
        }
    }
}
