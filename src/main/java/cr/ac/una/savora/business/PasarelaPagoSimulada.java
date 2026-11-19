package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.AutorizacionPago;
import cr.ac.una.savora.business.dto.TarjetaSimulada;
import cr.ac.una.savora.business.excepcion.PagoRechazadoException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Pasarela de pago SIMULADA. No se comunica con ningún banco ni cobra nada.
 * Reglas: número con algoritmo de Luhn, tarjeta no vencida y dos tarjetas de prueba que fallan.
 */
@Component
public class PasarelaPagoSimulada {

    static final String TARJETA_RECHAZADA = "4000000000000002";
    static final String TARJETA_SIN_FONDOS = "4000000000009995";

    private final Clock clock;

    public PasarelaPagoSimulada(Clock clock) {
        this.clock = clock;
    }

    public AutorizacionPago autorizar(TarjetaSimulada tarjeta) {
        String numero = tarjeta.numero().replace(" ", "");
        if (!numero.matches("\\d{13,19}") || !cumpleLuhn(numero)) {
            throw new PagoRechazadoException("El número de tarjeta no es válido");
        }
        if (estaVencida(tarjeta.vencimiento())) {
            throw new PagoRechazadoException("La tarjeta está vencida");
        }
        if (TARJETA_RECHAZADA.equals(numero)) {
            throw new PagoRechazadoException("La tarjeta fue rechazada por el banco emisor (simulación)");
        }
        if (TARJETA_SIN_FONDOS.equals(numero)) {
            throw new PagoRechazadoException("Fondos insuficientes (simulación)");
        }
        String referencia = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new AutorizacionPago(referencia, marcaDe(numero), numero.substring(numero.length() - 4));
    }

    private boolean estaVencida(String vencimiento) {
        try {
            int mes = Integer.parseInt(vencimiento.substring(0, 2));
            int anio = 2000 + Integer.parseInt(vencimiento.substring(3, 5));
            return YearMonth.of(anio, mes).isBefore(YearMonth.now(clock));
        } catch (DateTimeException | NumberFormatException | StringIndexOutOfBoundsException ex) {
            throw new PagoRechazadoException("El vencimiento no es válido");
        }
    }

    private static boolean cumpleLuhn(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    private static String marcaDe(String numero) {
        if (numero.startsWith("4")) {
            return "Visa";
        }
        if (numero.startsWith("5")) {
            return "Mastercard";
        }
        if (numero.startsWith("34") || numero.startsWith("37")) {
            return "Amex";
        }
        return "Tarjeta";
    }
}