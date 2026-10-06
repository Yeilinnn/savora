package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CrearPaqueteSorpresaSolicitud(
        @NotNull Long categoriaId,
        @NotBlank String descripcion,
        @Positive int cantidad,
        @NotNull @Positive BigDecimal precioOriginal,
        @NotNull @PositiveOrZero BigDecimal precioConDescuento,
        @NotNull OffsetDateTime horaLimiteRecogida) {
}