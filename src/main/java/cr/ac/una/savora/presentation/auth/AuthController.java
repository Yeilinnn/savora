package cr.ac.una.savora.presentation.auth;

import cr.ac.una.savora.business.dto.LoginRespuesta;
import cr.ac.una.savora.business.dto.LoginSolicitud;
import cr.ac.una.savora.business.dto.MensajeRespuesta;
import cr.ac.una.savora.business.dto.OlvideContrasenaSolicitud;
import cr.ac.una.savora.business.dto.RegistroClienteSolicitud;
import cr.ac.una.savora.business.dto.RegistroNegocioSolicitud;
import cr.ac.una.savora.business.dto.RestablecerContrasenaSolicitud;
import cr.ac.una.savora.security.AutenticacionService;
import cr.ac.una.savora.security.GestionContrasenaService;
import cr.ac.una.savora.security.RegistroClienteService;
import cr.ac.una.savora.security.RegistroNegocioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacionService autenticacionService;
    private final RegistroClienteService registroClienteService;
    private final RegistroNegocioService registroNegocioService;
    private final GestionContrasenaService gestionContrasenaService;

    public AuthController(
            AutenticacionService autenticacionService,
            RegistroClienteService registroClienteService,
            RegistroNegocioService registroNegocioService,
            GestionContrasenaService gestionContrasenaService) {
        this.autenticacionService = autenticacionService;
        this.registroClienteService = registroClienteService;
        this.registroNegocioService = registroNegocioService;
        this.gestionContrasenaService = gestionContrasenaService;
    }

    @PostMapping("/login")
    public LoginRespuesta login(@Valid @RequestBody LoginSolicitud solicitud) {
        return autenticacionService.login(solicitud);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginRespuesta registro(@Valid @RequestBody RegistroClienteSolicitud solicitud) {
        return registroClienteService.registrar(solicitud);
    }

    @PostMapping("/registro-negocio")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeRespuesta registroNegocio(@Valid @RequestBody RegistroNegocioSolicitud solicitud) {
        return registroNegocioService.registrar(solicitud);
    }

    @PostMapping("/olvide-contrasena")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MensajeRespuesta olvideContrasena(@Valid @RequestBody OlvideContrasenaSolicitud solicitud) {
        gestionContrasenaService.solicitarRestablecimiento(solicitud.correo());
        // Misma respuesta exista o no el correo.
        return new MensajeRespuesta(
                "Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.");
    }

    @PostMapping("/restablecer-contrasena")
    public MensajeRespuesta restablecerContrasena(@Valid @RequestBody RestablecerContrasenaSolicitud solicitud) {
        gestionContrasenaService.restablecer(solicitud.token(), solicitud.password());
        return new MensajeRespuesta("Contraseña actualizada. Ya puedes iniciar sesión.");
    }
}