package cr.ac.una.savora.security;

import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import cr.ac.una.savora.data.RolUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccesoPropioTest {

    private Authentication sesion(RolUsuario rol, Long negocioId, Long clienteId) {
        UsuarioPrincipal principal = new UsuarioPrincipal(1L, "u", "x", rol, negocioId, clienteId);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void unClientePuedeVerSusPropiosDatos() {
        assertThatCode(() -> AccesoPropio.exigirClienteOAdmin(7L, sesion(RolUsuario.CLIENTE, null, 7L)))
                .doesNotThrowAnyException();
    }

    @Test
    void unClienteNoPuedeVerLosDatosDeOtro() {
        assertThatThrownBy(() -> AccesoPropio.exigirClienteOAdmin(8L, sesion(RolUsuario.CLIENTE, null, 7L)))
                .isInstanceOf(PropiedadRecursoException.class);
    }

    @Test
    void unNegocioNoPuedeVerDatosDeClientes() {
        assertThatThrownBy(() -> AccesoPropio.exigirClienteOAdmin(7L, sesion(RolUsuario.NEGOCIO, 1L, null)))
                .isInstanceOf(PropiedadRecursoException.class);
    }

    @Test
    void elAdministradorPuedeVerLosDatosDeCualquiera() {
        assertThatCode(() -> AccesoPropio.exigirClienteOAdmin(8L, sesion(RolUsuario.ADMIN, null, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void sinSesionSeRechaza() {
        assertThatThrownBy(() -> AccesoPropio.exigirClienteOAdmin(7L, null))
                .isInstanceOf(PropiedadRecursoException.class);
    }
}