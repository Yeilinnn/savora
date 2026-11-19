package cr.ac.una.savora.security;

import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioPrincipal loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        String motivo = motivoDeshabilitado(usuario);
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getRol(),
                usuario.getNegocio() != null ? usuario.getNegocio().getId() : null,
                usuario.getCliente() != null ? usuario.getCliente().getId() : null,
                motivo == null,
                motivo);
    }

    private static String motivoDeshabilitado(Usuario usuario) {
        if (!usuario.isActivo()) {
            return "Su cuenta está desactivada. Comuníquese con un administrador.";
        }
        if (usuario.getRol() == RolUsuario.NEGOCIO && usuario.getNegocio() != null) {
            String estado = usuario.getNegocio().getEstado();
            if (Negocio.PENDIENTE.equals(estado)) {
                return "Su negocio está pendiente de aprobación por un administrador.";
            }
            if (Negocio.SUSPENDIDO.equals(estado)) {
                return "Su negocio fue suspendido. Comuníquese con un administrador.";
            }
        }
        return null;
    }
}