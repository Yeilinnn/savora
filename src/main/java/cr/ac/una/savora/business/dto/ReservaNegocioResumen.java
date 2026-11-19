package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservaNegocioResumen(
        Long id,
        Long paqueteSorpresaId,
        String descripcionPaquete,
        String nombreCliente,
        BigDecimal precioConDescuento,
        OffsetDateTime fechaHoraReserva,
        OffsetDateTime horaLimiteRecogida,
        String estado,
        String estadoPaquete) {
}