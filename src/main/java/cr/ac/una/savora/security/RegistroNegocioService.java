package cr.ac.una.savora.security;

import cr.ac.una.savora.business.dto.MensajeRespuesta;
import cr.ac.una.savora.business.dto.RegistroNegocioSolicitud;
import cr.ac.una.savora.business.excepcion.UsuarioDuplicadoException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de negocios: crea el Negocio en estado "pendiente" y su Usuario (rol NEGOCIO). No
 * devuelve token: el negocio no puede iniciar sesión hasta que un administrador lo apruebe.
 */
@Service
public class RegistroNegocioService {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistroEventosSeguridad registroEventosSeguridad;

    public RegistroNegocioService(
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository,
            PasswordEncoder passwordEncoder,
            RegistroEventosSeguridad registroEventosSeguridad) {
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.passwordEncoder = passwordEncoder;
        this.registroEventosSeguridad = registroEventosSeguridad;
    }

    @Transactional
    public MensajeRespuesta registrar(RegistroNegocioSolicitud solicitud) {
        String username = solicitud.username().trim();
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new UsuarioDuplicadoException("El nombre de usuario ya está en uso");
        }

        Negocio negocio = new Negocio(
                solicitud.nombre().trim(),
                solicitud.tipoNegocio().trim(),
                solicitud.ubicacion().trim(),
                solicitud.horarioCierre());
        negocio.setEstado(Negocio.PENDIENTE);
        negocio = negocioRepository.save(negocio);

        usuarioRepository.save(new Usuario(
                username, passwordEncoder.encode(solicitud.password()), RolUsuario.NEGOCIO, negocio, null));
        registroEventosSeguridad.negocioRegistrado(username);

        return new MensajeRespuesta(
                "Solicitud recibida. Un administrador revisará su negocio y, cuando lo apruebe, podrá iniciar sesión.");
    }
}