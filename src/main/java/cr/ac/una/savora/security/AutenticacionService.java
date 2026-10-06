package cr.ac.una.savora.security;

import cr.ac.una.savora.business.dto.LoginRespuesta;
import cr.ac.una.savora.business.dto.LoginSolicitud;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService {

    private final UsuarioDetailsService usuarioDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AutenticacionService(
            UsuarioDetailsService usuarioDetailsService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginRespuesta login(LoginSolicitud solicitud) {
        UsuarioPrincipal principal;
        try {
            principal = usuarioDetailsService.loadUserByUsername(solicitud.username());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("Usuario o contraseña inválidos");
        }
        if (!passwordEncoder.matches(solicitud.password(), principal.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña inválidos");
        }
        String token = jwtService.generar(principal);
        return new LoginRespuesta(token, principal.getRol().name(), jwtService.vigenciaSegundos());
    }
}