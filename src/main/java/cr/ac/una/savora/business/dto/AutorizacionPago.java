package cr.ac.una.savora.business.dto;

/** Resultado de una autorización aprobada por la pasarela simulada. */
public record AutorizacionPago(String referencia, String marca, String ultimos4) {
}