package cr.ac.una.savora.security;

import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Siembra usuarios de prueba (uno por rol, enlazados a los negocios/clientes del seed V4) la
 * primera vez que arranca la aplicación. Va en código -no en el SQL de Flyway- porque la
 * contraseña debe pasar por el mismo PasswordEncoder (BCrypt) que usará el login: así se evita
 * incrustar un hash fijo en un script y queda garantizado que siembra y login usan el mismo
 * algoritmo y el mismo costo.
 *
 * Usuarios: negocio1/negocio123 (negocio 1), negocio2/negocio123 (negocio 2), cliente1/cliente123
 * (cliente 1).
 */
@Component
public class UsuarioSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSeeder(
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Negocio negocio1 = negocioRepository.findById(1L).orElse(null);
        Negocio negocio2 = negocioRepository.findById(2L).orElse(null);
        Cliente cliente1 = clienteRepository.findById(1L).orElse(null);
        if (negocio1 == null || negocio2 == null || cliente1 == null) {
            return;
        }
        usuarioRepository.save(new Usuario(
                "negocio1", passwordEncoder.encode("negocio123"), RolUsuario.NEGOCIO, negocio1, null));
        usuarioRepository.save(new Usuario(
                "negocio2", passwordEncoder.encode("negocio123"), RolUsuario.NEGOCIO, negocio2, null));
        usuarioRepository.save(new Usuario(
                "cliente1", passwordEncoder.encode("cliente123"), RolUsuario.CLIENTE, null, cliente1));
    }
}