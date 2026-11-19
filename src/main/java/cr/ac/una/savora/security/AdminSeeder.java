package cr.ac.una.savora.security;

import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el usuario administrador "admin" si no existe. En dev y test usa "admin123"; en prod la
 * clave sale de la variable SAVORA_SEED_ADMIN_PASSWORD (si no está definida, no crea nada).
 * Corre al terminar el arranque para no interferir con los otros seeders.
 */
@Component
public class AdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String USUARIO_ADMIN = "admin";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String claveConfigurada;

    public AdminSeeder(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${SAVORA_SEED_ADMIN_PASSWORD:}") String claveConfigurada) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.claveConfigurada = claveConfigurada == null ? "" : claveConfigurada.trim();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sembrar() {
        if (usuarioRepository.findByUsername(USUARIO_ADMIN).isPresent()) {
            return;
        }
        String clave = claveConfigurada;
        if (clave.isEmpty()) {
            if (environment.acceptsProfiles(Profiles.of("dev", "test"))) {
                clave = "admin123";
            } else {
                log.warn("No se creó el usuario admin: falta la variable SAVORA_SEED_ADMIN_PASSWORD");
                return;
            }
        }
        usuarioRepository.save(new Usuario(USUARIO_ADMIN, passwordEncoder.encode(clave), RolUsuario.ADMIN, null, null));
    }
}