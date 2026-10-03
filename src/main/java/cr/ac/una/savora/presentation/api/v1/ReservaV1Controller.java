package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.ReservaConsultaService;
import cr.ac.una.savora.business.ReservaService;
import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.business.dto.ReservaResumen;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    private final ReservaService reservaService;
    private final ReservaConsultaService reservaConsultaService;

    public ReservaV1Controller(ReservaService reservaService, ReservaConsultaService reservaConsultaService) {
        this.reservaService = reservaService;
        this.reservaConsultaService = reservaConsultaService;
    }

    @PostMapping
    public ResponseEntity<ReservaResumen> crear(@Valid @RequestBody CrearReservaRequest request) {
        ReservaResumen creada = reservaService.reservar(request);
        return ResponseEntity
                .created(URI.create("/api/v1/reservas/" + creada.id()))
                .body(creada);
    }

    @GetMapping
    public Page<ReservaResumen> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return reservaConsultaService.listar(clienteId, estado, pageable);
    }

    @GetMapping("/{id}")
    public ReservaResumen obtener(@PathVariable Long id) {
        return reservaConsultaService.obtenerPorId(id);
    }
}
