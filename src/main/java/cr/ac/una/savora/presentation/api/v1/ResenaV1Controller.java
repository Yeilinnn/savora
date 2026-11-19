package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.ResenaService;
import cr.ac.una.savora.business.dto.ResenaPromedio;
import cr.ac.una.savora.business.dto.ResenaResumen;
import cr.ac.una.savora.business.dto.ResenaSolicitud;
import cr.ac.una.savora.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reseñas. Las rutas bajo /api/v1/reservas/** son solo de CLIENTE y las de /api/v1/negocio/**
 * solo de NEGOCIO (SecurityConfig); la lectura pública por negocio exige sesión.
 */
@RestController
public class ResenaV1Controller {

    private final ResenaService resenaService;

    public ResenaV1Controller(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /** El cliente califica una reserva suya ya recogida. */
    @PostMapping("/api/v1/reservas/{reservaId}/resena")
    public ResponseEntity<ResenaResumen> calificar(
            @PathVariable Long reservaId,
            @Valid @RequestBody ResenaSolicitud solicitud,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resenaService.crear(reservaId, principal.getClienteId(), solicitud));
    }

    /** Ids de las reservas que el cliente ya calificó. */
    @GetMapping("/api/v1/reservas/calificadas")
    public List<Long> reservasCalificadas(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return resenaService.reservasCalificadasPor(principal.getClienteId());
    }

    @GetMapping("/api/v1/negocios/{negocioId}/resenas")
    public Page<ResenaResumen> resenasDeNegocio(
            @PathVariable Long negocioId,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return resenaService.listarPorNegocio(negocioId, pageable);
    }

    @GetMapping("/api/v1/negocios/{negocioId}/resenas/resumen")
    public ResenaPromedio resumenDeNegocio(@PathVariable Long negocioId) {
        return resenaService.resumenDeNegocio(negocioId);
    }

    /** El negocio autenticado ve las reseñas de sus propios paquetes. */
    @GetMapping("/api/v1/negocio/resenas")
    public Page<ResenaResumen> misResenas(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return resenaService.listarPorNegocio(principal.getNegocioId(), pageable);
    }

    @GetMapping("/api/v1/negocio/resenas/resumen")
    public ResenaPromedio miResumen(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return resenaService.resumenDeNegocio(principal.getNegocioId());
    }
}