package cr.ac.una.savora.security;

import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import org.springframework.security.core.Authentication;

/** Regla de acceso: un cliente solo puede consultar sus propios datos; el administrador, los de cualquiera. */
public final class AccesoPropio {

    private AccesoPropio() {
    }

    public static void exigirClienteOAdmin(Long clienteId, Authentication autenticacion) {
        boolean esAdmin = autenticacion != null
                && autenticacion.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (esAdmin) {
            return;
        }
        if (autenticacion != null
                && autenticacion.getPrincipal() instanceof UsuarioPrincipal principal
                && clienteId != null
                && clienteId.equals(principal.getClienteId())) {
            return;
        }
        throw new PropiedadRecursoException("Cliente", clienteId);
    }
}