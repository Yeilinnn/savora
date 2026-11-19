package cr.ac.una.savora.business.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ReservaConPagoSolicitud(@NotNull Long paqueteSorpresaId, @NotNull @Valid TarjetaSimulada tarjeta) {
}