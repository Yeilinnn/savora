package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.NegocioAdminResumen;
import cr.ac.una.savora.business.dto.UsuarioAdminResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Casos de uso del administrador: supervisar negocios y usuarios registrados. */
@Service
public class AdministracionService {

    private static final Set<String> ESTADOS_PERMITIDOS = Set.of(Negocio.APROBADO, Negocio.SUSPENDIDO);

    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroEventosSeguridad registroEventosSeguridad;

    public AdministracionService(
            NegocioRepository negocioRepository,
            UsuarioRepository usuarioRepository,
            RegistroEventosSeguridad registroEventosSeguridad) {
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroEventosSeguridad = registroEventosSeguridad;
    }

    @Transactional(readOnly = true)
    public Page<NegocioAdminResumen> listarNegocios(Pageable pageable) {
        return negocioRepository.findAll(pageable).map(this::aResumen);
    }

    @Transactional(readOnly = true)
    public long contarNegociosPendientes() {
        return negocioRepository.countByEstado(Negocio.PENDIENTE);
    }

    @Transactional
    public NegocioAdminResumen cambiarEstadoNegocio(Long negocioId, String estado, String administrador) {
        if (estado == null || !ESTADOS_PERMITIDOS.contains(estado)) {
            throw new SolicitudCuentaInvalidaException("Estado de negocio no válido");
        }
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Negocio", negocioId));
        negocio.setEstado(estado);
        negocioRepository.save(negocio);
        registroEventosSeguridad.adminAccion(administrador, "negocio_" + estado, "negocioId=" + negocioId);
        return aResumen(negocio);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioAdminResumen> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(AdministracionService::aResumen);
    }

    @Transactional
    public UsuarioAdminResumen cambiarActivo(Long usuarioId, boolean activo, String administrador) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));
        if (usuario.getRol() == RolUsuario.ADMIN) {
            throw new SolicitudCuentaInvalidaException("No se puede desactivar una cuenta de administrador");
        }
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        registroEventosSeguridad.adminAccion(
                administrador, activo ? "usuario_activado" : "usuario_desactivado", "usuarioId=" + usuarioId);
        return aResumen(usuario);
    }

    private NegocioAdminResumen aResumen(Negocio negocio) {
        String usuario = usuarioRepository.findByNegocioId(negocio.getId())
                .map(Usuario::getUsername)
                .orElse("(sin usuario)");
        return new NegocioAdminResumen(
                negocio.getId(),
                negocio.getNombre(),
                negocio.getTipoNegocio(),
                negocio.getUbicacion(),
                negocio.getEstado(),
                usuario);
    }

    private static UsuarioAdminResumen aResumen(Usuario usuario) {
        String asociado = switch (usuario.getRol()) {
            case NEGOCIO -> usuario.getNegocio() != null ? usuario.getNegocio().getNombre() : "—";
            case CLIENTE -> usuario.getCliente() != null ? usuario.getCliente().getNombre() : "—";
            case ADMIN -> "Administrador";
        };
        return new UsuarioAdminResumen(
                usuario.getId(), usuario.getUsername(), usuario.getRol().name(), usuario.isActivo(), asociado);
    }
}