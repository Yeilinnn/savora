package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
        @NotNull Long clienteId,
        @NotNull Long paqueteSorpresaId) {
}