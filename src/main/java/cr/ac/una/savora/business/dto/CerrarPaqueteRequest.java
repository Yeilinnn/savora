package cr.ac.una.savora.business.dto;

import jakarta.validation.constraints.NotNull;

public record CerrarPaqueteRequest(
        @NotNull Long paqueteSorpresaId) {
}