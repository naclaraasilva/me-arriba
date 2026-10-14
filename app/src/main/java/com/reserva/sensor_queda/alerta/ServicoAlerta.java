package com.reserva.sensor_queda.alerta;

import android.content.Context;

import com.reserva.sensor_queda.dados.RepositorioConfiguracoes;
import com.reserva.sensor_queda.telemetria.EnviadorTelemetria;

/**
 * Orquestra o envio de um alerta de queda: coleta localizacao, monta a amostra
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
            RepositorioConfiguracoes configuracoes = RepositorioConfiguracoes.get(appContext);
            boolean demo = configuracoes.isModoDemo() || configuracoes.getEndpoint().isEmpty();

            ResultadoAlerta resultadoBase = new ResultadoAlerta();
            resultadoBase.localizacaoOk = infoLocalizacao.ok;
            resultadoBase.textoLocalizacao = infoLocalizacao.text;
            resultadoBase.contatosCadastrados = !configuracoes.getContatos().isEmpty();
            resultadoBase.demo = demo;

            if (demo) {
                new EnviadorAlertaSimulado().enviar(null, resultado -> completar(resultado, resultadoBase, callback));
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
    }

    private void completar(ResultadoAlerta resultado, ResultadoAlerta resultadoBase, Callback callback) {
        resultado.localizacaoOk = resultadoBase.localizacaoOk;
        resultado.textoLocalizacao = resultadoBase.textoLocalizacao;
        resultado.contatosCadastrados = resultadoBase.contatosCadastrados;
        resultado.demo = resultadoBase.demo;
        callback.onResult(resultado);
    }
}
