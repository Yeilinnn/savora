package cr.ac.una.savora.security;

import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("prod")
public class ProdUsuarioSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String negocioPassword;
    private final String clientePassword;

    public ProdUsuarioSeeder(
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            @Value("${SAVORA_SEED_NEGOCIO_PASSWORD}") String negocioPassword,
            @Value("${SAVORA_SEED_CLIENTE_PASSWORD}") String clientePassword) {
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.negocioPassword = negocioPassword;
        this.clientePassword = clientePassword;
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
                "negocio1", passwordEncoder.encode(negocioPassword), RolUsuario.NEGOCIO, negocio1, null));
        usuarioRepository.save(new Usuario(
                "negocio2", passwordEncoder.encode(negocioPassword), RolUsuario.NEGOCIO, negocio2, null));
        usuarioRepository.save(new Usuario(
                "cliente1", passwordEncoder.encode(clientePassword), RolUsuario.CLIENTE, null, cliente1));
    }
}
