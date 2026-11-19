package cr.ac.una.savora.security;

import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.RestablecimientoContrasena;
import cr.ac.una.savora.data.RestablecimientoContrasenaRepository;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Olvidé mi contraseña (enlace por correo de un solo uso, vigente 30 minutos) y cambio de
 * contraseña con la sesión abierta. La respuesta de "olvidé mi contraseña" es siempre la misma
 * exista o no el correo, para no revelar qué correos están registrados.
 */
@Service
public class GestionContrasenaService {

    private static final Logger log = LoggerFactory.getLogger(GestionContrasenaService.class);
    private static final Duration VIGENCIA_ENLACE = Duration.ofMinutes(30);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RestablecimientoContrasenaRepository restablecimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnviadorCorreo enviadorCorreo;
    private final RegistroEventosSeguridad registroEventosSeguridad;
    private final Clock clock;
    private final String urlSpa;
    private final SecureRandom aleatorio = new SecureRandom();

    public GestionContrasenaService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            RestablecimientoContrasenaRepository restablecimientoRepository,
            PasswordEncoder passwordEncoder,
            EnviadorCorreo enviadorCorreo,
            RegistroEventosSeguridad registroEventosSeguridad,
            Clock clock,
            @Value("${savora.spa.url:http://localhost:5173}") String urlSpa) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.restablecimientoRepository = restablecimientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.enviadorCorreo = enviadorCorreo;
        this.registroEventosSeguridad = registroEventosSeguridad;
        this.clock = clock;
        this.urlSpa = urlSpa == null ? "" : urlSpa.trim().replaceAll("/+$", "");
    }

    @Transactional
    public void solicitarRestablecimiento(String correoIngresado) {
        String correo = correoIngresado.trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> encontrado = clienteRepository.findByCorreo(correo)
                .flatMap(cliente -> usuarioRepository.findByClienteId(cliente.getId()));
        if (encontrado.isEmpty()) {
            return;
        }
        Usuario usuario = encontrado.get();
        OffsetDateTime ahora = OffsetDateTime.now(clock);

        // Un enlace nuevo deja sin efecto los anteriores.
        restablecimientoRepository.findByUsuarioIdAndUsadoEnIsNull(usuario.getId())
                .forEach(anterior -> anterior.marcarUsado(ahora));

        String token = generarToken();
        restablecimientoRepository.save(new RestablecimientoContrasena(usuario, hash(token), ahora.plus(VIGENCIA_ENLACE)));
        registroEventosSeguridad.restablecimientoSolicitado(usuario.getUsername());

        String enlace = urlSpa + "/#/restablecer?token=" + token;
        String cuerpo = "Hola,\n\n"
                + "Recibimos una solicitud para restablecer la contraseña de tu cuenta de Savora. "
                + "Abre este enlace (vale 30 minutos y solo se puede usar una vez):\n\n"
                + enlace + "\n\n"
                + "Si no fuiste tú, ignora este correo: tu contraseña no cambia.\n\n"
                + "Equipo Savora";
        try {
            enviadorCorreo.enviar(correo, "Restablece tu contraseña de Savora", cuerpo);
        } catch (RuntimeException ex) {
            // No se informa al usuario: la respuesta debe ser igual exista o no el correo.
            log.error("No se pudo enviar el correo de restablecimiento", ex);
        }
    }

    @Transactional
    public void restablecer(String token, String nuevaClave) {
        OffsetDateTime ahora = OffsetDateTime.now(clock);
        RestablecimientoContrasena registro = restablecimientoRepository.findByTokenHash(hash(token))
                .filter(r -> r.estaVigente(ahora))
                .orElseThrow(() -> new SolicitudCuentaInvalidaException(
                        "El enlace no es válido o ya venció. Solicita uno nuevo."));

        Usuario usuario = registro.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);

        registro.marcarUsado(ahora);
        restablecimientoRepository.findByUsuarioIdAndUsadoEnIsNull(usuario.getId())
                .forEach(pendiente -> pendiente.marcarUsado(ahora));
        registroEventosSeguridad.contrasenaCambiada(usuario.getUsername(), "restablecimiento");
    }

    @Transactional
    public void cambiar(String username, String claveActual, String claveNueva) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new SolicitudCuentaInvalidaException("No se encontró la cuenta"));
        if (!passwordEncoder.matches(claveActual, usuario.getPassword())) {
            throw new SolicitudCuentaInvalidaException("La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(claveNueva, usuario.getPassword())) {
            throw new SolicitudCuentaInvalidaException("La nueva contraseña debe ser distinta a la actual");
        }
        usuario.setPassword(passwordEncoder.encode(claveNueva));
        usuarioRepository.save(usuario);
        registroEventosSeguridad.contrasenaCambiada(usuario.getUsername(), "cambio");
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}