package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaqueteSorpresaResumen(
        Long id,
        Long negocioId,
        String nombreNegocio,
        Long categoriaId,
        String nombreCategoria,
        String descripcion,
        int cantidad,
        BigDecimal precioOriginal,
        BigDecimal precioConDescuento,
        OffsetDateTime horaLimiteRecogida,
        String estado) {
}