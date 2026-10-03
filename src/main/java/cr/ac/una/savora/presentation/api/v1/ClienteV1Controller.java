package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.ClienteAppService;
import cr.ac.una.savora.business.dto.ClienteResumen;
import cr.ac.una.savora.business.dto.CrearClienteSolicitud;
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
@RequestMapping("/api/v1/clientes")
public class ClienteV1Controller {

    private final ClienteAppService clienteAppService;

    public ClienteV1Controller(ClienteAppService clienteAppService) {
        this.clienteAppService = clienteAppService;
    }

    @GetMapping
    public Page<ClienteResumen> listar(Pageable pageable) {
        return clienteAppService.listar(pageable);
    }

    @GetMapping("/{id}")
    public ClienteResumen obtener(@PathVariable Long id) {
        return clienteAppService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ClienteResumen> crear(@Valid @RequestBody CrearClienteSolicitud solicitud) {
        ClienteResumen creado = clienteAppService.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + creado.id())).body(creado);
    }
}
