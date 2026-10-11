package com.reserva.sensor_queda.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detector heuristico de quedas combinando acelerometro e giroscopio.
 *
 * <p>Estrategia: uma queda tipica apresenta um periodo de quase ausencia de
 * gravidade (queda livre) seguido por um pico brusco de aceleracao (impacto)
 * e/ou rotacao abrupta. Proximidade e barometro sao opcionais e nao fazem
 * parte desta versao.</p>
 *
 * <p>O detector NAO e infalivel: e um algoritmo de demonstracao que precisa
 * de calibracao e testes em campo.</p>
 */
public class FallDetector implements SensorEventListener {

    public interface Listener {
        void onPotentialFall(boolean simulated);
    }

    private static final float LIMITE_QUEDA_LIVRE = 3.5f;   // m/s^2
    private static final float LIMITE_IMPACTO = 25.0f;      // m/s^2
    private static final float LIMITE_GIRO = 3.0f;          // rad/s
    private static final long JANELA_QUEDA_MS = 1200L;
    private static final long INTERVALO_MINIMO_MS = 8000L;

    private final SensorManager sensorManager;
    private final Sensor acelerometro;
    private final Sensor giroscopio;
    private final Sensor sensorTemperatura;
    private final Sensor sensorPressao;
    private final Listener listener;

    private long ultimaQuedaLivre = 0L;
    private long ultimaRotacao = 0L;
    private long ultimoDisparo = 0L;
    private boolean registrado = false;

    private float aceleracaoAtualG = 1f;
    private float ultimoImpactoG = 0f;
    private float ultimaTemperatura = Float.NaN;
    private float ultimaPressao = Float.NaN;

    public FallDetector(Context context, Listener listener) {
        this.listener = listener;
        this.sensorManager = (SensorManager) context.getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        this.acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorTemperatura = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        this.sensorPressao = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    public boolean isAvailable() {
        return acelerometro != null;
    }

    public boolean hasGyroscope() {
        return giroscopio != null;
    }

    public boolean hasTemperature() {
        return sensorTemperatura != null;
    }

    public Float getUltimaTemperatura() {
        return Float.isNaN(ultimaTemperatura) ? null : ultimaTemperatura;
    }

    public boolean hasPressure() {
        return sensorPressao != null;
    }

    public Float getUltimaPressao() {
        return Float.isNaN(ultimaPressao) ? null : ultimaPressao;
    }

    public float getAceleracaoAtualG() {
        return aceleracaoAtualG;
    }

    public float getUltimoImpactoG() {
        return ultimoImpactoG;
    }

    public void start() {
        if (registrado || acelerometro == null) {
            return;
        }
        sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_GAME);
        if (giroscopio != null) {
            sensorManager.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_GAME);
        }
        if (sensorTemperatura != null) {
            sensorManager.registerListener(this, sensorTemperatura, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorPressao != null) {
            sensorManager.registerListener(this, sensorPressao, SensorManager.SENSOR_DELAY_NORMAL);
        }
        registrado = true;
    }

    public void stop() {
        if (!registrado) {
            return;
        }
        sensorManager.unregisterListener(this);
        registrado = false;
    }

    /** Dispara um evento de queda simulado, claramente marcado como teste. */
    public void simulate() {
        long agora = System.currentTimeMillis();
        if (agora - ultimoDisparo < INTERVALO_MINIMO_MS) {
            return;
        }
        ultimoDisparo = agora;
        ultimoImpactoG = 3.0f;
        if (listener != null) {
            listener.onPotentialFall(true);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long agora = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float magnitude = magnitude(event.values);
            aceleracaoAtualG = magnitude / 9.80665f;
            if (magnitude < LIMITE_QUEDA_LIVRE) {
                ultimaQuedaLivre = agora;
            } else if (magnitude > LIMITE_IMPACTO) {
                boolean quaseZeroG = (agora - ultimaQuedaLivre) < JANELA_QUEDA_MS;
                boolean rotacaoBrusca = (agora - ultimaRotacao) < JANELA_QUEDA_MS;
                if ((quaseZeroG || rotacaoBrusca) && (agora - ultimoDisparo) > INTERVALO_MINIMO_MS) {
                    ultimoDisparo = agora;
                    ultimoImpactoG = aceleracaoAtualG;
                    if (listener != null) {
                        listener.onPotentialFall(false);
                    }
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (magnitude(event.values) > LIMITE_GIRO) {
                ultimaRotacao = agora;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            ultimaTemperatura = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            ultimaPressao = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private static float magnitude(float[] values) {
        return (float) Math.sqrt(
                values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
    }
}
