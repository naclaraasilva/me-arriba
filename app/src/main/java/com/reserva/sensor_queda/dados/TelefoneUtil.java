package com.reserva.sensor_queda.dados;

/**
 * Normaliza e valida telefones para o padrao E.164.
 *
 * <p>O app e focado no Brasil (+55). Aceita as formas mais comuns digitadas
 * pelo usuario (com parenteses, espacos, hifen, zero de tronco e codigo de
 * operadora) e converte para {@code +55DDDNUMERO}. Numeros internacionais
 * informados explicitamente com {@code +} sao preservados.</p>
 */
public final class TelefoneUtil {

    public static final int OK = 0;
    public static final int VAZIO = 1;
    public static final int TAMANHO = 2;
    public static final int DDD = 3;

    public static class Resultado {
        public final int motivo;
        /** Telefone normalizado; preenchido apenas quando {@link #isValido()}. */
        public final String e164;

        private Resultado(int motivo, String e164) {
            this.motivo = motivo;
            this.e164 = e164;
        }

        public boolean isValido() {
            return motivo == OK;
        }
    }

    private TelefoneUtil() {
    }

    public static Resultado normalizar(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            return new Resultado(VAZIO, null);
        }

        String texto = entrada.trim();
        boolean internacional = texto.startsWith("+");
        String digitos = somenteDigitos(texto);
        if (digitos.isEmpty()) {
            return new Resultado(TAMANHO, null);
        }

        // Remove zero de tronco e prefixos de discagem (ex.: 011, 021, 0055).
        while (digitos.startsWith("0")) {
            digitos = digitos.substring(1);
        }
        if (digitos.isEmpty()) {
            return new Resultado(TAMANHO, null);
        }

        boolean pareceBrasil = digitos.startsWith("55")
                && (digitos.length() == 12 || digitos.length() == 13);
        if (pareceBrasil) {
            digitos = digitos.substring(2);
        } else if (internacional) {
            // Numero internacional explicito que nao e brasileiro: mantem E.164.
            if (digitos.length() >= 8 && digitos.length() <= 15) {
                return new Resultado(OK, "+" + digitos);
            }
            return new Resultado(TAMANHO, null);
        }

        // Formato com codigo de operadora: 0 + operadora(2) + DDD + numero.
        if (digitos.length() == 12 || digitos.length() == 13) {
            digitos = digitos.substring(2);
        }

        if (digitos.length() != 10 && digitos.length() != 11) {
            return new Resultado(TAMANHO, null);
        }

        int ddd = Integer.parseInt(digitos.substring(0, 2));
        if (ddd < 11 || ddd > 99) {
            return new Resultado(DDD, null);
        }

        return new Resultado(OK, "+55" + digitos);
    }

    private static String somenteDigitos(String texto) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
