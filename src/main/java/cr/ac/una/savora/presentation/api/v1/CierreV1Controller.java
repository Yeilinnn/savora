package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.CierreDePaquetesService;
import cr.ac.una.savora.business.dto.CerrarPaqueteRequest;
import cr.ac.una.savora.business.dto.CierreResumen;
import cr.ac.una.savora.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/paquetes")
public class CierreV1Controller {

    private final CierreDePaquetesService cierreDePaquetesService;

    public CierreV1Controller(CierreDePaquetesService cierreDePaquetesService) {
        this.cierreDePaquetesService = cierreDePaquetesService;
    }

    @PostMapping("/{id}/cierre")
    public CierreResumen cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        return cierreDePaquetesService.cerrar(new CerrarPaqueteRequest(id), principal.getNegocioId());
    }
}
