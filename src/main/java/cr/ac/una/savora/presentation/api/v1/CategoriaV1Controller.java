package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.CategoriaService;
import cr.ac.una.savora.business.dto.CategoriaResumen;
import cr.ac.una.savora.business.dto.CrearCategoriaSolicitud;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaV1Controller {

    private final CategoriaService categoriaService;

    public CategoriaV1Controller(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public Page<CategoriaResumen> listar(Pageable pageable) {
        return categoriaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public CategoriaResumen obtener(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaResumen> crear(@Valid @RequestBody CrearCategoriaSolicitud solicitud) {
        CategoriaResumen creada = categoriaService.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/v1/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public CategoriaResumen actualizar(@PathVariable Long id, @Valid @RequestBody CrearCategoriaSolicitud solicitud) {
        return categoriaService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
