package com.reserva.sensor_queda.ui;

import com.reserva.sensor_queda.R;
import com.reserva.sensor_queda.dados.TipoEvento;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Mapeia tipos de evento para recursos visuais e formata data/hora. */
public final class UtilitariosEvento {

    private UtilitariosEvento() {
    }

    public static int tituloRes(String tipo) {
        switch (tipo) {
            case TipoEvento.QUEDA_DETECTADA:
                return R.string.evento_queda_detectada;
            case TipoEvento.FALSO_POSITIVO:
                return R.string.evento_falso_positivo;
            case TipoEvento.ALERTA_SOLICITADO:
                return R.string.evento_alerta_solicitado;
            case TipoEvento.ALERTA_ENVIADO:
                return R.string.evento_alerta_enviado;
            case TipoEvento.ALERTA_FALHOU:
                return R.string.evento_alerta_falhou;
            case TipoEvento.CHECKIN:
                return R.string.evento_checkin;
            default:
                return R.string.app_name;
        }
    }

    public static int iconeRes(String tipo) {
        switch (tipo) {
            case TipoEvento.QUEDA_DETECTADA:
                return R.drawable.ic_alert;
            case TipoEvento.FALSO_POSITIVO:
                return R.drawable.ic_check_circle;
            case TipoEvento.ALERTA_SOLICITADO:
                return R.drawable.ic_shield;
            case TipoEvento.ALERTA_ENVIADO:
                return R.drawable.ic_check_circle;
            case TipoEvento.ALERTA_FALHOU:
                return R.drawable.ic_error;
            case TipoEvento.CHECKIN:
                return R.drawable.ic_clock;
            default:
                return R.drawable.ic_history;
        }
    }

    public static int corRes(String tipo) {
        switch (tipo) {
            case TipoEvento.QUEDA_DETECTADA:
            case TipoEvento.ALERTA_FALHOU:
                return R.color.emergency;
            case TipoEvento.FALSO_POSITIVO:
            case TipoEvento.ALERTA_ENVIADO:
                return R.color.safe;
            case TipoEvento.CHECKIN:
                return R.color.secondary_action;
            default:
                return R.color.text_secondary;
        }
    }

    public static boolean ehQueda(String tipo) {
        return TipoEvento.QUEDA_DETECTADA.equals(tipo) || TipoEvento.FALSO_POSITIVO.equals(tipo);
    }

    public static boolean ehAlerta(String tipo) {
        return TipoEvento.ALERTA_SOLICITADO.equals(tipo)
                || TipoEvento.ALERTA_ENVIADO.equals(tipo)
                || TipoEvento.ALERTA_FALHOU.equals(tipo);
    }

    public static String formatarDataHora(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", new Locale("pt", "BR"));
        return sdf.format(new Date(timestamp));
    }
}
