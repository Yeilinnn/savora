package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroClienteSolicitud(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank
        @Pattern(regexp = "^[0-9+\\- ]{8,20}$", message = "El teléfono debe tener entre 8 y 20 dígitos")
        String telefono,
        @NotBlank
        @Size(min = 4, max = 30)
        @Pattern(regexp = "^[a-z0-9_.]+$", message = "Use solo minúsculas, números, punto o guion bajo")
        String username,
        @NotBlank @Size(min = 8, max = 72) String password) {
}