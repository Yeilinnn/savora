package cr.ac.una.savora.business.dto;

import java.time.LocalTime;

public record NegocioResumen(
        Long id,
        String nombre,
        String tipoNegocio,
        String ubicacion,
        LocalTime horarioCierre) {
}
