package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaSolicitud(
        @NotBlank @Size(max = 72) String actual,
        @NotBlank @Size(min = 8, max = 72) String nueva) {
}