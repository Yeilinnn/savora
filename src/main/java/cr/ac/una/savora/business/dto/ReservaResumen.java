package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservaResumen(
        Long id,
        Long paqueteSorpresaId,
        String descripcionPaquete,
        BigDecimal precioConDescuento,
        BigDecimal descuentoPorcentaje,
        OffsetDateTime fechaHoraReserva,
        String estado) {
}