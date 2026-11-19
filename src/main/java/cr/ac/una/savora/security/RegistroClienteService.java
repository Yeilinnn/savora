package cr.ac.una.savora.security;

import cr.ac.una.savora.business.dto.LoginRespuesta;
import cr.ac.una.savora.business.dto.RegistroClienteSolicitud;
import cr.ac.una.savora.business.excepcion.UsuarioDuplicadoException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Registro de clientes: crea el Cliente y su Usuario (rol CLIENTE) en una sola transacción y
 * devuelve un token para que la persona quede con la sesión iniciada. El rol nunca viene del
 * cuerpo de la solicitud: siempre es CLIENTE (los negocios los da de alta el administrador).
 */
@Service
public class RegistroClienteService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RegistroEventosSeguridad registroEventosSeguridad;

    public RegistroClienteService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RegistroEventosSeguridad registroEventosSeguridad) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registroEventosSeguridad = registroEventosSeguridad;
    }

    @Transactional
    public LoginRespuesta registrar(RegistroClienteSolicitud solicitud) {
        String username = solicitud.username().trim();
        String correo = solicitud.correo().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new UsuarioDuplicadoException("El nombre de usuario ya está en uso");
        }
        if (clienteRepository.findByCorreo(correo).isPresent()) {
            throw new UsuarioDuplicadoException("Ya existe una cuenta con ese correo");
        }

        Cliente cliente = clienteRepository.save(
                new Cliente(solicitud.nombre().trim(), correo, solicitud.telefono().trim()));
        String hash = passwordEncoder.encode(solicitud.password());
        Usuario usuario = usuarioRepository.save(new Usuario(username, hash, RolUsuario.CLIENTE, null, cliente));

        registroEventosSeguridad.registroCliente(username);

        UsuarioPrincipal principal = new UsuarioPrincipal(
                usuario.getId(), username, hash, RolUsuario.CLIENTE, null, cliente.getId());
        return new LoginRespuesta(jwtService.generar(principal), RolUsuario.CLIENTE.name(), jwtService.vigenciaSegundos());
    }
}