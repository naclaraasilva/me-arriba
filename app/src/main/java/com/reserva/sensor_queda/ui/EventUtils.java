package com.reserva.sensor_queda.ui;

import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.data.EventType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Mapeia tipos de evento para recursos visuais e formata data/hora. */
public final class EventUtils {

    private EventUtils() {
    }

    public static int tituloRes(String type) {
        switch (type) {
            case EventType.QUEDA_DETECTADA:
                return R.string.evento_queda_detectada;
            case EventType.FALSO_POSITIVO:
                return R.string.evento_falso_positivo;
            case EventType.ALERTA_SOLICITADO:
                return R.string.evento_alerta_solicitado;
            case EventType.ALERTA_ENVIADO:
                return R.string.evento_alerta_enviado;
            case EventType.ALERTA_FALHOU:
                return R.string.evento_alerta_falhou;
            case EventType.CHECKIN:
                return R.string.evento_checkin;
            default:
                return R.string.app_name;
        }
    }

    public static int iconeRes(String type) {
        switch (type) {
            case EventType.QUEDA_DETECTADA:
                return R.drawable.ic_alert;
            case EventType.FALSO_POSITIVO:
                return R.drawable.ic_check_circle;
            case EventType.ALERTA_SOLICITADO:
                return R.drawable.ic_shield;
            case EventType.ALERTA_ENVIADO:
                return R.drawable.ic_check_circle;
            case EventType.ALERTA_FALHOU:
                return R.drawable.ic_error;
            case EventType.CHECKIN:
                return R.drawable.ic_clock;
            default:
                return R.drawable.ic_history;
        }
    }

    public static int corRes(String type) {
        switch (type) {
            case EventType.QUEDA_DETECTADA:
            case EventType.ALERTA_FALHOU:
                return R.color.emergency;
            case EventType.FALSO_POSITIVO:
            case EventType.ALERTA_ENVIADO:
                return R.color.safe;
            case EventType.CHECKIN:
                return R.color.secondary_action;
            default:
                return R.color.text_secondary;
        }
    }

    public static boolean isQueda(String type) {
        return EventType.QUEDA_DETECTADA.equals(type) || EventType.FALSO_POSITIVO.equals(type);
    }

    public static boolean isAlerta(String type) {
        return EventType.ALERTA_SOLICITADO.equals(type)
                || EventType.ALERTA_ENVIADO.equals(type)
                || EventType.ALERTA_FALHOU.equals(type);
    }

    public static String formatarDataHora(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR"));
        return sdf.format(new Date(timestamp));
    }
}
