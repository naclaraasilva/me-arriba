package com.reserva.sensor_queda.componente;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** Anel circular reutilizado no status de monitoramento e no contador de queda. */
public class VisaoAnelProgresso extends View {

    private final Paint anelFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint anelProgresso = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private float progresso = 1f;
    private float larguraTraco;

    public VisaoAnelProgresso(Context context) {
        super(context);
        iniciar();
    }

    public VisaoAnelProgresso(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        iniciar();
    }

    public VisaoAnelProgresso(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        iniciar();
    }

    private void iniciar() {
        larguraTraco = dp(14f);
        anelFundo.setStyle(Paint.Style.STROKE);
        anelFundo.setStrokeWidth(larguraTraco);
        anelFundo.setColor(0xFF2A3B47);

        anelProgresso.setStyle(Paint.Style.STROKE);
        anelProgresso.setStrokeWidth(larguraTraco);
        anelProgresso.setStrokeCap(Paint.Cap.ROUND);
        anelProgresso.setColor(0xFF20C978);
    }

    public void setProgresso(float progresso) {
        this.progresso = Math.max(0f, Math.min(1f, progresso));
        invalidate();
    }

    public void setCor(int cor) {
        anelProgresso.setColor(cor);
        invalidate();
    }

    public void setLarguraTracoDp(float dp) {
        this.larguraTraco = dp(dp);
        anelFundo.setStrokeWidth(larguraTraco);
        anelProgresso.setStrokeWidth(larguraTraco);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float raio = Math.min(cx, cy) - larguraTraco / 2f;
        if (raio <= 0) {
            return;
        }
        canvas.drawCircle(cx, cy, raio, anelFundo);
        oval.set(cx - raio, cy - raio, cx + raio, cy + raio);
        canvas.drawArc(oval, -90f, 360f * progresso, false, anelProgresso);
    }

    private float dp(float valor) {
        return valor * getResources().getDisplayMetrics().density;
    }
}
