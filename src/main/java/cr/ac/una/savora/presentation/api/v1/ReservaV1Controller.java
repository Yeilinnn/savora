package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.CheckoutService;
import cr.ac.una.savora.business.ReservaConsultaService;
import cr.ac.una.savora.business.dto.CarritoPagadoRespuesta;
import cr.ac.una.savora.business.dto.CarritoPagoSolicitud;
import cr.ac.una.savora.business.dto.ReservaConPagoSolicitud;
import cr.ac.una.savora.business.dto.ReservaPagadaRespuesta;
import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaV1Controller {

    private final CheckoutService checkoutService;
    private final ReservaConsultaService reservaConsultaService;

    public ReservaV1Controller(CheckoutService checkoutService, ReservaConsultaService reservaConsultaService) {
        this.checkoutService = checkoutService;
        this.reservaConsultaService = reservaConsultaService;
    }

    /** Pago simulado + reserva de un paquete. Sin tarjeta válida no se crea la reserva. */
    @PostMapping
    public ResponseEntity<ReservaPagadaRespuesta> crear(
            @Valid @RequestBody ReservaConPagoSolicitud solicitud,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        ReservaPagadaRespuesta creada = checkoutService.pagarYReservar(solicitud, principal.getClienteId());
        return ResponseEntity
                .created(URI.create("/api/v1/reservas/" + creada.id()))
                .body(creada);
    }

    /** Carrito: un solo pago simulado que reserva varios paquetes. Todo o nada. */
    @PostMapping("/carrito")
    public ResponseEntity<CarritoPagadoRespuesta> pagarCarrito(
            @Valid @RequestBody CarritoPagoSolicitud solicitud,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(checkoutService.pagarCarrito(solicitud, principal.getClienteId()));
    }

    @GetMapping
    public Page<ReservaResumen> listar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return reservaConsultaService.listarParaCliente(principal.getClienteId(), estado, pageable);
    }

    @GetMapping("/{id}")
    public ReservaResumen obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return reservaConsultaService.obtenerPorIdParaCliente(id, principal.getClienteId());
    }
}