package com.reserva.sensor_queda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.reserva.sensor_queda.dados.TelefoneUtil;

import org.junit.Test;

/** Testes da normalizacao de telefone para E.164. */
public class TelefoneUtilTest {

    private void deveNormalizar(String entrada, String esperado) {
        TelefoneUtil.Resultado resultado = TelefoneUtil.normalizar(entrada);
        assertTrue("Deveria ser valido: " + entrada, resultado.isValido());
        assertEquals(esperado, resultado.e164);
    }

    @Test
    public void formataCelularComMascara() {
        deveNormalizar("(11) 99999-8888", "+5511999998888");
        deveNormalizar("11 9 9999-8888", "+5511999998888");
    }

    @Test
    public void adicionaCodigoDoPais() {
        deveNormalizar("11999998888", "+5511999998888");
        deveNormalizar("5511999998888", "+5511999998888");
        deveNormalizar("+55 11 99999-8888", "+5511999998888");
    }

    @Test
    public void removeZeroDeTroncoEOperadora() {
        deveNormalizar("011 99999-8888", "+5511999998888");
        deveNormalizar("021 11 99999-8888", "+5511999998888");
    }

    @Test
    public void aceitaFixo() {
        deveNormalizar("(11) 3333-4444", "+551133334444");
        deveNormalizar("+55 11 3333-4444", "+551133334444");
    }

    @Test
    public void preservaNumeroInternacional() {
        deveNormalizar("+351 912 345 678", "+351912345678");
        deveNormalizar("+1 415 555 2671", "+14155552671");
    }

    @Test
    public void rejeitaEntradasInvalidas() {
        assertFalse(TelefoneUtil.normalizar("").isValido());
        assertEquals(TelefoneUtil.VAZIO, TelefoneUtil.normalizar(null).motivo);
        assertEquals(TelefoneUtil.TAMANHO, TelefoneUtil.normalizar("9999-8888").motivo);
        assertEquals(TelefoneUtil.DDD, TelefoneUtil.normalizar("10 99999-8888").motivo);
        assertEquals(TelefoneUtil.TAMANHO, TelefoneUtil.normalizar("abc").motivo);
    }
}
