package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginSolicitud(
        @NotBlank String username,
        @NotBlank String password) {
}