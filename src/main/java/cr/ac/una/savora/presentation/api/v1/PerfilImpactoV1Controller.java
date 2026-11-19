package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.PerfilImpactoService;
import cr.ac.una.savora.business.dto.PerfilImpactoResumen;
import cr.ac.una.savora.data.TipoPropietario;
import cr.ac.una.savora.security.AccesoPropio;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PerfilImpactoV1Controller {

    private final PerfilImpactoService perfilImpactoService;

    public PerfilImpactoV1Controller(PerfilImpactoService perfilImpactoService) {
        this.perfilImpactoService = perfilImpactoService;
    }

    /** Un cliente solo ve su propio perfil de impacto; el administrador, el de cualquiera. */
    @GetMapping("/clientes/{id}/perfil-impacto")
    public PerfilImpactoResumen perfilCliente(@PathVariable Long id, Authentication autenticacion) {
        AccesoPropio.exigirClienteOAdmin(id, autenticacion);
        return perfilImpactoService.consultarResumen(String.valueOf(id), TipoPropietario.CLIENTE);
    }

    @GetMapping("/negocios/{id}/perfil-impacto")
    public PerfilImpactoResumen perfilNegocio(@PathVariable String id) {
        return perfilImpactoService.consultarResumen(id, TipoPropietario.NEGOCIO);
    }
}