package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResenaSolicitud(
        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        Integer calificacion,
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String comentario) {
}