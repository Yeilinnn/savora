package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.OrganizacionAppService;
import cr.ac.una.savora.business.dto.CrearOrganizacionSolicitud;
import cr.ac.una.savora.business.dto.OrganizacionResumen;
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
@RequestMapping("/api/v1/organizaciones-comunitarias")
public class OrganizacionV1Controller {

    private final OrganizacionAppService organizacionAppService;

    public OrganizacionV1Controller(OrganizacionAppService organizacionAppService) {
        this.organizacionAppService = organizacionAppService;
    }

    @GetMapping
    public Page<OrganizacionResumen> listar(Pageable pageable) {
        return organizacionAppService.listar(pageable);
    }

    @GetMapping("/{id}")
    public OrganizacionResumen obtener(@PathVariable Long id) {
        return organizacionAppService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<OrganizacionResumen> crear(@Valid @RequestBody CrearOrganizacionSolicitud solicitud) {
        OrganizacionResumen creada = organizacionAppService.crear(solicitud);
        return ResponseEntity
                .created(URI.create("/api/v1/organizaciones-comunitarias/" + creada.id()))
                .body(creada);
    }
}
