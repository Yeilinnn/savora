package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearClienteSolicitud(
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank String telefono) {
}
