package cr.ac.una.savora.presentation.auth;

import cr.ac.una.savora.business.dto.LoginRespuesta;
import cr.ac.una.savora.business.dto.LoginSolicitud;
import cr.ac.una.savora.security.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacionService autenticacionService;

    public AuthController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/login")
    public LoginRespuesta login(@Valid @RequestBody LoginSolicitud solicitud) {
        return autenticacionService.login(solicitud);
    }
}