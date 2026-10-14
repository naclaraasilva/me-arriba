package com.reserva.sensor_queda.clima;

import android.os.Handler;
import android.os.Looper;

import com.reserva.sensor_queda.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Consulta o clima atual na OpenWeather (Current Weather API). */
public class ProvedorClima {

    /** Chave da API do OpenWeather (definida em local.properties). */
    private static final String API_KEY = BuildConfig.OPENWEATHER_API_KEY;

    private static final String ENDPOINT =
            "https://api.openweathermap.org/data/2.5/weather";

    public static class InfoClima {
        public boolean ok;
        public String descricao;
        public double temperaturaC;
        public String icone;
        public String erro;
    }

    public interface Callback {
        void onResult(InfoClima info);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ProvedorClima() {
    }

    public static boolean temChaveApi() {
        return API_KEY != null && !API_KEY.isEmpty();
    }

    public static void obterClima(double lat, double lon, Callback callback) {
        EXECUTOR.execute(() -> {
            InfoClima info = new InfoClima();
            if (!temChaveApi()) {
                info.erro = "Chave da API nao configurada";
                entregar(info, callback);
                return;
            }

            HttpURLConnection conexao = null;
            try {
                String url = ENDPOINT
                        + "?lat=" + String.format(Locale.US, "%.6f", lat)
                        + "&lon=" + String.format(Locale.US, "%.6f", lon)
                        + "&units=metric&lang=pt_br&appid="
                        + URLEncoder.encode(API_KEY, "UTF-8");
                conexao = (HttpURLConnection) new URL(url).openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(8000);
                int codigo = conexao.getResponseCode();
                String corpo = lerResposta(conexao);
                if (codigo >= 200 && codigo < 300) {
                    preencher(corpo, info);
                    if (!info.ok) {
                        info.erro = "Resposta sem dados de clima";
                    }
                } else {
                    info.erro = "HTTP " + codigo;
                }
            } catch (Exception e) {
                info.erro = e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
            entregar(info, callback);
        });
    }

    private static void entregar(InfoClima info, Callback callback) {
        MAIN.post(() -> callback.onResult(info));
    }

    private static void preencher(String corpo, InfoClima info) throws Exception {
        JSONObject json = new JSONObject(corpo);
        JSONObject main = json.optJSONObject("main");
        if (main != null) {
            info.temperaturaC = main.optDouble("temp");
        }
        JSONArray weather = json.optJSONArray("weather");
        if (weather != null && weather.length() > 0) {
            JSONObject item = weather.getJSONObject(0);
            info.descricao = capitalizar(item.optString("description"));
            info.icone = item.optString("icon");
        }
        info.ok = info.descricao != null && !info.descricao.isEmpty();
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
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
