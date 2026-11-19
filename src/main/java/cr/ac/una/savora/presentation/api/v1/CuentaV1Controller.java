package cr.ac.una.savora.presentation.api.v1;

import cr.ac.una.savora.business.dto.CambiarContrasenaSolicitud;
import cr.ac.una.savora.business.dto.MensajeRespuesta;
import cr.ac.una.savora.security.GestionContrasenaService;
import cr.ac.una.savora.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operaciones de la cuenta de la persona autenticada (cliente o negocio). */
@RestController
@RequestMapping("/api/v1/cuenta")
public class CuentaV1Controller {

    private final GestionContrasenaService gestionContrasenaService;

    public CuentaV1Controller(GestionContrasenaService gestionContrasenaService) {
        this.gestionContrasenaService = gestionContrasenaService;
    }

    @PostMapping("/cambiar-contrasena")
    public MensajeRespuesta cambiarContrasena(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody CambiarContrasenaSolicitud solicitud) {
        gestionContrasenaService.cambiar(principal.getUsername(), solicitud.actual(), solicitud.nueva());
        return new MensajeRespuesta("Contraseña actualizada");
    }
}