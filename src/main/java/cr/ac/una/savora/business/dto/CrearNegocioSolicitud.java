package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CrearNegocioSolicitud(
        @NotBlank String nombre,
        @NotBlank String tipoNegocio,
        @NotBlank String ubicacion,
        @NotNull LocalTime horarioCierre) {
}
