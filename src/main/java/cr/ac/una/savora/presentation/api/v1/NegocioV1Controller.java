package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.NegocioAppService;
import cr.ac.una.savora.business.dto.CrearNegocioSolicitud;
import cr.ac.una.savora.business.dto.NegocioResumen;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/negocios")
public class NegocioV1Controller {

    private final NegocioAppService negocioAppService;

    public NegocioV1Controller(NegocioAppService negocioAppService) {
        this.negocioAppService = negocioAppService;
    }

    @GetMapping
    public Page<NegocioResumen> listar(Pageable pageable) {
        return negocioAppService.listar(pageable);
    }

    @GetMapping("/{id}")
    public NegocioResumen obtener(@PathVariable Long id) {
        return negocioAppService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<NegocioResumen> crear(@Valid @RequestBody CrearNegocioSolicitud solicitud) {
        NegocioResumen creado = negocioAppService.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/v1/negocios/" + creado.id())).body(creado);
    }
}
