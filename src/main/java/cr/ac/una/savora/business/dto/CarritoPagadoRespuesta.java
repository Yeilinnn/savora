package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;
import java.util.List;

/** Comprobante de un carrito pagado: un solo pago (misma referencia) que cubre varias reservas. */
public record CarritoPagadoRespuesta(
        String referencia,
        BigDecimal total,
        String marca,
        String ultimos4,
        String estado,
        List<ReservaResumen> reservas) {
}