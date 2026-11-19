package cr.ac.una.savora.business.dto;

/** Promedio de calificaciones de un negocio (null si todavía no tiene reseñas). */
public record ResenaPromedio(Long negocioId, Double promedio, int total) {
}