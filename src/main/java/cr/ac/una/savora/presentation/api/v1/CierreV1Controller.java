package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.CierreDePaquetesService;
import cr.ac.una.savora.business.PaqueteSorpresaAppService;
import cr.ac.una.savora.business.dto.CerrarPaqueteRequest;
import cr.ac.una.savora.business.dto.CierreResumen;
import cr.ac.una.savora.security.UsuarioPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/paquetes")
public class CierreV1Controller {

    private final CierreDePaquetesService cierreDePaquetesService;
    private final PaqueteSorpresaAppService paqueteSorpresaAppService;

    public CierreV1Controller(
            CierreDePaquetesService cierreDePaquetesService, PaqueteSorpresaAppService paqueteSorpresaAppService) {
        this.cierreDePaquetesService = cierreDePaquetesService;
        this.paqueteSorpresaAppService = paqueteSorpresaAppService;
    }

    /**
     * Verificación de propiedad del recurso (OWASP API1 - Broken Object Level Authorization):
     * un negocio solo puede cerrar sus propios paquetes, aunque conozca el id de otro.
     */
    @PostMapping("/{id}/cierre")
    public CierreResumen cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal principal) {
        Long negocioPropietario = paqueteSorpresaAppService.obtenerNegocioPropietario(id);
        if (!negocioPropietario.equals(principal.getNegocioId())) {
            throw new AccessDeniedException("El paquete " + id + " no pertenece al negocio autenticado");
        }
        return cierreDePaquetesService.cerrar(new CerrarPaqueteRequest(id));
    }
}