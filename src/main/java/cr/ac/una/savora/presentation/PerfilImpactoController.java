package cr.ac.una.savora.presentation;

import cr.ac.una.savora.business.PerfilImpactoService;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.TipoPropietario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PerfilImpactoController {

    private final PerfilImpactoService perfilImpactoService;

    public PerfilImpactoController(PerfilImpactoService perfilImpactoService) {
        this.perfilImpactoService = perfilImpactoService;
    }

    @GetMapping("/api/clientes/{id}/perfil-impacto")
    public Map<String, Object> obtenerPerfilCliente(@PathVariable String id) {
        PerfilImpacto perfil = perfilImpactoService.obtenerOCrear(id, TipoPropietario.CLIENTE);
        return Map.of(
                "perfil", perfil,
                "limiteReservasActivas", perfilImpactoService.calcularLimiteReservas(perfil)
        );
    }

    @GetMapping("/api/negocios/{id}/perfil-impacto")
    public PerfilImpacto obtenerPerfilNegocio(@PathVariable String id) {
        return perfilImpactoService.obtenerOCrear(id, TipoPropietario.NEGOCIO);
    }
}