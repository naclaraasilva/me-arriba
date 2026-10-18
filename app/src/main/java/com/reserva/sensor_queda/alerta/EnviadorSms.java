package com.reserva.sensor_queda.alerta;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import com.reserva.sensor_queda.dados.ContatoEmergencia;
import com.reserva.sensor_queda.dados.TelefoneUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Envia o alerta de queda por SMS diretamente aos contatos de emergencia,
 * usando o chip do proprio aparelho (SmsManager). Nao depende de API externa
 * nem de internet: o SMS sai pela operadora do numero do dispositivo.
 */
public final class EnviadorSms {

    public static class ResultadoSms {
        /** Contatos que tiveram o SMS aceito para envio. */
        public int enviados;
        /** Total de contatos cadastrados. */
        public int total;
        /** Permissao de envio de SMS concedida pelo usuario. */
        public boolean permissaoConcedida;
        /** Detalhe legivel do resultado. */
        public String detalhe;
    }

    public interface Callback {
        void onResult(ResultadoSms resultado);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private EnviadorSms() {
    }

    /**
     * @param mensagem texto enviado a todos os contatos (pode ser multipart).
     */
    public static void enviar(Context context, List<ContatoEmergencia> contatos, String mensagem,
                              Callback callback) {
        final Context appContext = context.getApplicationContext();
        final ResultadoSms resultado = new ResultadoSms();
        resultado.total = contatos == null ? 0 : contatos.size();
        resultado.permissaoConcedida = ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        if (resultado.total == 0) {
            resultado.detalhe = "Nenhum contato cadastrado";
            MAIN.post(() -> callback.onResult(resultado));
            return;
        }
        if (!resultado.permissaoConcedida) {
            resultado.detalhe = "Permissao de SMS nao concedida";
            MAIN.post(() -> callback.onResult(resultado));
            return;
        }

        final SmsManager smsManager = obterSmsManager(appContext);
        final ArrayList<String> partes = smsManager == null
                ? null
                : smsManager.divideMessage(mensagem);

        int falhas = 0;
        for (ContatoEmergencia contato : contatos) {
            String destino = normalizarTelefone(contato == null ? null : contato.telefone);
            if (smsManager == null || destino.isEmpty()) {
                falhas++;
                continue;
            }
            try {
                if (partes != null && partes.size() > 1) {
                    smsManager.sendMultipartTextMessage(destino, null, partes, null, null);
                } else {
                    smsManager.sendTextMessage(destino, null, mensagem, null, null);
                }
                resultado.enviados++;
            } catch (Exception e) {
                falhas++;
            }
        }

        resultado.detalhe = falhas > 0
                ? ("Falha em " + falhas + " de " + resultado.total + " contato(s)")
                : "SMS enviado";
        MAIN.post(() -> callback.onResult(resultado));
    }

    private static SmsManager obterSmsManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getSystemService(SmsManager.class);
        }
        return SmsManager.getDefault();
    }

    /** Normaliza para E.164; numeros antigos fora do padrao usam melhor esforco. */
    private static String normalizarTelefone(String telefone) {
        TelefoneUtil.Resultado resultado = TelefoneUtil.normalizar(telefone);
        if (resultado.isValido()) {
            return resultado.e164;
        }
        if (telefone == null) {
            return "";
        }
        String texto = telefone.trim();
        StringBuilder sb = new StringBuilder();
        if (texto.startsWith("+")) {
            sb.append('+');
        }
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
