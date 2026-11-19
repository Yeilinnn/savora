package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OlvideContrasenaSolicitud(@NotBlank @Email @Size(max = 120) String correo) {
}