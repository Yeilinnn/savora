package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record RegistroNegocioSolicitud(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 60) String tipoNegocio,
        @NotBlank @Size(max = 200) String ubicacion,
        @NotNull LocalTime horarioCierre,
        @NotBlank
        @Size(min = 4, max = 30)
        @Pattern(regexp = "^[a-z0-9_.]+$", message = "Use solo minúsculas, números, punto o guion bajo")
        String username,
        @NotBlank @Size(min = 8, max = 72) String password) {
}