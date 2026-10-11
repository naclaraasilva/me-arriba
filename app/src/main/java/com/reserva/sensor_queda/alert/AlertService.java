package com.reserva.sensor_queda.alert;

import android.content.Context;

import com.reserva.sensor_queda.data.SettingsRepository;
import com.reserva.sensor_queda.telemetry.TelemetrySender;

/**
 * Orquestra o envio de um alerta de queda: coleta localizacao, monta a amostra
 * de telemetria e escolhe o canal (API real Socorro ou modo demonstracao).
 */
public class AlertService {

    public interface Callback {
        void onResult(AlertResult result);
    }

    private final Context appContext;

    public AlertService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** @param quedaG forca do impacto em G informada pelo detector (0 se panico). */
    public void enviarAlerta(final float quedaG, final Callback callback) {
        LocationProvider.getLocation(appContext, locationInfo -> {
            SettingsRepository settings = SettingsRepository.get(appContext);
            boolean demo = settings.isDemoMode() || settings.getEndpoint().isEmpty();

            AlertResult base = new AlertResult();
            base.locationOk = locationInfo.ok;
            base.locationText = locationInfo.text;
            base.contatosCadastrados = !settings.getContacts().isEmpty();
            base.demo = demo;

            if (demo) {
                new MockAlertSender().send(null, result -> completar(result, base, callback));
                return;
            }

            TelemetrySender.Sample sample = new TelemetrySender.Sample();
            sample.queda = true;
            sample.quedaG = quedaG;
            sample.demo = false;
            if (locationInfo.ok) {
                sample.latitude = locationInfo.lat;
                sample.longitude = locationInfo.lon;
                sample.velocidadeKmh = locationInfo.speedKmh;
            }

            TelemetrySender.enviar(appContext, settings.getEndpoint(), sample, (ok, msg) -> {
                AlertResult result = new AlertResult();
                result.apiOk = ok;
                result.apiMessage = msg;
                completar(result, base, callback);
            });
        });
    }

    private void completar(AlertResult result, AlertResult base, Callback callback) {
        result.locationOk = base.locationOk;
        result.locationText = base.locationText;
        result.contatosCadastrados = base.contatosCadastrados;
        result.demo = base.demo;
        callback.onResult(result);
    }
}
