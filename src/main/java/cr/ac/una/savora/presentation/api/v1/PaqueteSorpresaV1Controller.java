package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.PaqueteSorpresaAppService;
import cr.ac.una.savora.business.dto.CrearPaqueteSorpresaSolicitud;
import cr.ac.una.savora.business.dto.PaqueteSorpresaResumen;
import cr.ac.una.savora.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/paquetes")
public class PaqueteSorpresaV1Controller {

    private final PaqueteSorpresaAppService paqueteSorpresaAppService;

    public PaqueteSorpresaV1Controller(PaqueteSorpresaAppService paqueteSorpresaAppService) {
        this.paqueteSorpresaAppService = paqueteSorpresaAppService;
    }

    @GetMapping
    public Page<PaqueteSorpresaResumen> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long negocioId,
            Pageable pageable) {
        return paqueteSorpresaAppService.listar(estado, categoriaId, negocioId, pageable);
    }

    @GetMapping("/{id}")
    public PaqueteSorpresaResumen obtener(@PathVariable Long id) {
        return paqueteSorpresaAppService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PaqueteSorpresaResumen> crear(
            @Valid @RequestBody CrearPaqueteSorpresaSolicitud solicitud,
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        PaqueteSorpresaResumen creado = paqueteSorpresaAppService.crear(solicitud, principal.getNegocioId());
        return ResponseEntity.created(URI.create("/api/v1/paquetes/" + creado.id())).body(creado);
    }
}