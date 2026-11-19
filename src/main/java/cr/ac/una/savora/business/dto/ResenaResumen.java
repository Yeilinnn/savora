package cr.ac.una.savora.business.dto;

import java.time.Instant;

public record ResenaResumen(
        String id,
        Long reservaId,
        Long negocioId,
        Long paqueteSorpresaId,
        String nombreCliente,
        int calificacion,
        String comentario,
        Instant fecha) {
}