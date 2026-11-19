package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerContrasenaSolicitud(
        @NotBlank @Size(max = 200) String token,
        @NotBlank @Size(min = 8, max = 72) String password) {
}