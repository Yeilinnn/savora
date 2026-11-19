package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Misma forma que ReservaResumen, más el comprobante del pago. */
public record ReservaPagadaRespuesta(
        Long id,
        Long paqueteSorpresaId,
        String descripcionPaquete,
        BigDecimal precioConDescuento,
        BigDecimal descuentoPorcentaje,
        OffsetDateTime fechaHoraReserva,
        String estado,
        ComprobantePago pago) {
}