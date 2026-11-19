package cr.ac.una.savora.business.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CarritoPagoSolicitud(
        @NotEmpty @Size(max = 5, message = "El carrito admite hasta 5 paquetes") List<@NotNull Long> paqueteSorpresaIds,
        @NotNull @Valid TarjetaSimulada tarjeta) {
}