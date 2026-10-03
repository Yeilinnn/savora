package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CrearOrganizacionSolicitud(
        @NotBlank String nombre,
        @NotBlank String tipo,
        @Min(1) int capacidadRecoleccion,
        @NotBlank String contacto) {
}
