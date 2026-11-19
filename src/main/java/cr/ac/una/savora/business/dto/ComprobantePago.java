package cr.ac.una.savora.business.dto;

import java.math.BigDecimal;

public record ComprobantePago(String referencia, BigDecimal monto, String marca, String ultimos4, String estado) {
}