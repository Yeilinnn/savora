package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.NegocioAdminResumen;
import cr.ac.una.savora.business.dto.UsuarioAdminResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministracionServiceTest {

    @Mock
    NegocioRepository negocioRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    RegistroEventosSeguridad registroEventosSeguridad;

    AdministracionService service;

    @BeforeEach
    void setUp() {
        service = new AdministracionService(negocioRepository, usuarioRepository, registroEventosSeguridad);
    }

    private Negocio negocioPendiente() {
        Negocio negocio = new Negocio("Soda Doña Lela", "Soda", "Liberia", LocalTime.of(18, 0));
        negocio.setEstado(Negocio.PENDIENTE);
        return negocio;
    }

    @Test
    void aprobarNegocioCambiaEstadoYRegistraEvento() {
        Negocio negocio = negocioPendiente();
        when(negocioRepository.findById(5L)).thenReturn(Optional.of(negocio));
        when(usuarioRepository.findByNegocioId(any())).thenReturn(Optional.empty());

        NegocioAdminResumen resumen = service.cambiarEstadoNegocio(5L, "aprobado", "admin");

        assertThat(negocio.estaAprobado()).isTrue();
        assertThat(resumen.estado()).isEqualTo("aprobado");
        assertThat(resumen.usuario()).isEqualTo("(sin usuario)");
        verify(negocioRepository).save(negocio);
        verify(registroEventosSeguridad).adminAccion("admin", "negocio_aprobado", "negocioId=5");
    }

    @Test
    void estadoInvalidoSeRechaza() {
        assertThatThrownBy(() -> service.cambiarEstadoNegocio(5L, "pendiente", "admin"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);
        assertThatThrownBy(() -> service.cambiarEstadoNegocio(5L, null, "admin"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);
        verify(negocioRepository, never()).save(any(Negocio.class));
    }

    @Test
    void negocioInexistenteLanzaNoEncontrado() {
        when(negocioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstadoNegocio(99L, "aprobado", "admin"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarNegociosIncluyeElNombreDeUsuario() {
        Negocio negocio = negocioPendiente();
        Pageable pagina = PageRequest.of(0, 10);
        when(negocioRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(negocio), pagina, 1));
        when(usuarioRepository.findByNegocioId(any()))
                .thenReturn(Optional.of(new Usuario("lela", "hash", RolUsuario.NEGOCIO, negocio, null)));

        Page<NegocioAdminResumen> resultado = service.listarNegocios(pagina);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).usuario()).isEqualTo("lela");
        assertThat(resultado.getContent().get(0).estado()).isEqualTo("pendiente");
    }

    @Test
    void contarPendientesDelegaEnElRepositorio() {
        when(negocioRepository.countByEstado(Negocio.PENDIENTE)).thenReturn(3L);

        assertThat(service.contarNegociosPendientes()).isEqualTo(3L);
    }

    @Test
    void desactivarClienteActualizaElUsuario() {
        Usuario usuario = new Usuario("maria", "hash", RolUsuario.CLIENTE, null, new Cliente("María", "m@c.com", "1"));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        UsuarioAdminResumen resumen = service.cambiarActivo(7L, false, "admin");

        assertThat(usuario.isActivo()).isFalse();
        assertThat(resumen.activo()).isFalse();
        assertThat(resumen.asociado()).isEqualTo("María");
        verify(usuarioRepository).save(usuario);
        verify(registroEventosSeguridad).adminAccion("admin", "usuario_desactivado", "usuarioId=7");
    }

    @Test
    void noSePuedeDesactivarUnAdministrador() {
        Usuario admin = new Usuario("admin", "hash", RolUsuario.ADMIN, null, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.cambiarActivo(1L, false, "admin"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);

        assertThat(admin.isActivo()).isTrue();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void usuarioInexistenteLanzaNoEncontrado() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarActivo(404L, true, "admin"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}