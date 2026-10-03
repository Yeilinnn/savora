package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearCategoriaSolicitud(@NotBlank String nombre) {
}
