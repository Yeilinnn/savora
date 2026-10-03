package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ReservaMapeador {

    private ReservaMapeador() {
    }

    public static ReservaResumen aResumen(Reserva reserva) {
        PaqueteSorpresa paquete = reserva.getPaqueteSorpresa();
        return new ReservaResumen(
                reserva.getId(),
                paquete.getId(),
                paquete.getDescripcion(),
                paquete.getPrecioConDescuento(),
                calcularDescuentoPorcentaje(paquete.getPrecioOriginal(), paquete.getPrecioConDescuento()),
                reserva.getFechaHoraReserva(),
                reserva.getEstado());
    }

    private static BigDecimal calcularDescuentoPorcentaje(BigDecimal precioOriginal, BigDecimal precioConDescuento) {
        BigDecimal ahorro = precioOriginal.subtract(precioConDescuento);
        return ahorro
                .divide(precioOriginal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
