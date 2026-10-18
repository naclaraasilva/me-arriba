package com.reserva.sensor_queda.alerta;

import android.content.Context;

import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.dados.ContatoEmergencia;
import com.reserva.sensor_queda.dados.RepositorioConfiguracoes;
import com.reserva.sensor_queda.telemetria.EnviadorTelemetria;

import java.util.List;
import java.util.Locale;

/**
 * Orquestra o envio de um alerta de queda: coleta localizacao, envia SMS
 * automatico com a localizacao aos contatos de emergencia, monta a amostra
 * de telemetria e escolhe o canal (API real Socorro ou modo demonstracao).
 */
public class ServicoAlerta {

    public interface Callback {
        void onResult(ResultadoAlerta resultado);
    }

    private final Context appContext;

    public ServicoAlerta(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** @param quedaG forca do impacto em G informada pelo detector (0 se panico). */
    public void enviarAlerta(final float quedaG, final Callback callback) {
        ProvedorLocalizacao.obterLocalizacao(appContext, infoLocalizacao -> {
            final RepositorioConfiguracoes configuracoes = RepositorioConfiguracoes.get(appContext);
            final boolean demo = configuracoes.isModoDemo() || configuracoes.getEndpoint().isEmpty();
            final List<ContatoEmergencia> contatos = configuracoes.getContatos();

            final ResultadoAlerta resultadoBase = new ResultadoAlerta();
            resultadoBase.localizacaoOk = infoLocalizacao.ok;
            resultadoBase.textoLocalizacao = infoLocalizacao.text;
            resultadoBase.contatosCadastrados = !contatos.isEmpty();
            resultadoBase.contatosTotal = contatos.size();
            resultadoBase.demo = demo;

            String mensagem = montarMensagemSms(configuracoes.getUsuario(), infoLocalizacao);

            // Canal direto: SMS com a localizacao para cada contato de emergencia.
            EnviadorSms.enviar(appContext, contatos, mensagem, resultadoSms -> {
                resultadoBase.smsTentado = resultadoSms.total > 0;
                resultadoBase.smsPermissaoConcedida = resultadoSms.permissaoConcedida;
                resultadoBase.contatosNotificados = resultadoSms.enviados;
                resultadoBase.mensagemSms = resultadoSms.detalhe;

                if (demo) {
                    new EnviadorAlertaSimulado().enviar(null,
                            resultado -> completar(resultado, resultadoBase, callback));
                    return;
                }

                EnviadorTelemetria.Amostra amostra = new EnviadorTelemetria.Amostra();
                amostra.queda = true;
                amostra.quedaG = quedaG;
                amostra.demo = false;
                if (infoLocalizacao.ok) {
                    amostra.latitude = infoLocalizacao.lat;
                    amostra.longitude = infoLocalizacao.lon;
                    amostra.velocidadeKmh = infoLocalizacao.velocidadeKmh;
                }

                EnviadorTelemetria.enviar(appContext, configuracoes.getEndpoint(), amostra, (ok, msg) -> {
                    ResultadoAlerta resultado = new ResultadoAlerta();
                    resultado.apiSucesso = ok;
                    resultado.mensagemApi = msg;
                    completar(resultado, resultadoBase, callback);
                });
            });
        });
    }

    /** Monta o texto do SMS de emergencia com a localizacao e link do mapa. */
    private String montarMensagemSms(String usuario, ProvedorLocalizacao.InfoLocalizacao info) {
        StringBuilder sb = new StringBuilder();
        sb.append(appContext.getString(R.string.sms_alerta_queda, usuario));
        sb.append('\n');
        if (info.ok) {
            sb.append(appContext.getString(R.string.sms_localizacao_linha, info.text));
            sb.append('\n');
            sb.append(appContext.getString(R.string.sms_mapa_linha, String.format(Locale.US,
                    "https://maps.google.com/?q=%.6f,%.6f", info.lat, info.lon)));
        } else {
            sb.append(appContext.getString(R.string.sms_localizacao_indisponivel));
        }
        return sb.toString();
    }

    private void completar(ResultadoAlerta resultado, ResultadoAlerta resultadoBase, Callback callback) {
        resultado.localizacaoOk = resultadoBase.localizacaoOk;
        resultado.textoLocalizacao = resultadoBase.textoLocalizacao;
        resultado.contatosCadastrados = resultadoBase.contatosCadastrados;
        resultado.contatosTotal = resultadoBase.contatosTotal;
        resultado.demo = resultadoBase.demo;
        resultado.smsTentado = resultadoBase.smsTentado;
        resultado.smsPermissaoConcedida = resultadoBase.smsPermissaoConcedida;
        resultado.contatosNotificados = resultadoBase.contatosNotificados;
        resultado.mensagemSms = resultadoBase.mensagemSms;
        callback.onResult(resultado);
    }
}
