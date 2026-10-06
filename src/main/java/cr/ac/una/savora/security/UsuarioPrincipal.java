package cr.ac.una.savora.security;

import cr.ac.una.savora.data.RolUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class UsuarioPrincipal implements UserDetails {

    private final Long usuarioId;
    private final String username;
    private final String password;
    private final RolUsuario rol;
    private final Long negocioId;
    private final Long clienteId;

    public UsuarioPrincipal(
            Long usuarioId, String username, String password, RolUsuario rol, Long negocioId, Long clienteId) {
        this.usuarioId = usuarioId;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.negocioId = negocioId;
        this.clienteId = clienteId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Long getNegocioId() {
        return negocioId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}