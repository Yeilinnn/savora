package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.GestionReservasNegocioService;
import cr.ac.una.savora.business.dto.ReservaNegocioResumen;
import cr.ac.una.savora.security.UsuarioPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reservas recibidas por el negocio autenticado. El negocio sale siempre del JWT, nunca de la
 * URL ni del cuerpo (misma regla que al publicar paquetes).
 */
@RestController
@RequestMapping("/api/v1/negocio/reservas")
public class NegocioReservasV1Controller {

    private final GestionReservasNegocioService gestionReservasNegocioService;

    public NegocioReservasV1Controller(GestionReservasNegocioService gestionReservasNegocioService) {
        this.gestionReservasNegocioService = gestionReservasNegocioService;
    }

    @GetMapping
    public Page<ReservaNegocioResumen> listar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return gestionReservasNegocioService.listar(principal.getNegocioId(), estado, pageable);
    }

    @PostMapping("/{id}/recogida")
    public ReservaNegocioResumen marcarRecogida(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return gestionReservasNegocioService.marcarRecogida(id, principal.getNegocioId());
    }

    @PostMapping("/{id}/no-recogida")
    public ReservaNegocioResumen marcarNoRecogida(
            @PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return gestionReservasNegocioService.marcarNoRecogida(id, principal.getNegocioId());
    }
}