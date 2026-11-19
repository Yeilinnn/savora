package cr.ac.una.savora.security;

import cr.ac.una.savora.business.dto.LoginRespuesta;
import cr.ac.una.savora.business.dto.RegistroClienteSolicitud;
import cr.ac.una.savora.business.excepcion.UsuarioDuplicadoException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.RolUsuario;
import cr.ac.una.savora.data.Usuario;
import cr.ac.una.savora.data.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroClienteServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    ClienteRepository clienteRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    RegistroEventosSeguridad registroEventosSeguridad;

    RegistroClienteService service;

    @BeforeEach
    void setUp() {
        service = new RegistroClienteService(
                usuarioRepository, clienteRepository, passwordEncoder, jwtService, registroEventosSeguridad);
    }

    private RegistroClienteSolicitud solicitud() {
        return new RegistroClienteSolicitud("  María Pérez ", "Maria@Correo.COM", "8888-8888", "maria", "secreta123");
    }

    @Test
    void registrarCreaClienteYUsuarioConContrasenaCifradaYDevuelveToken() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.empty());
        when(clienteRepository.findByCorreo("maria@correo.com")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente cliente = invocacion.getArgument(0);
            ReflectionTestUtils.setField(cliente, "id", 11L);
            return cliente;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", 21L);
            return usuario;
        });
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");
        when(jwtService.generar(any(UsuarioPrincipal.class))).thenReturn("token-de-prueba");
        when(jwtService.vigenciaSegundos()).thenReturn(7200L);

        LoginRespuesta respuesta = service.registrar(solicitud());

        assertThat(respuesta.token()).isEqualTo("token-de-prueba");
        assertThat(respuesta.rol()).isEqualTo("CLIENTE");
        assertThat(respuesta.expiraEnSegundos()).isEqualTo(7200L);

        ArgumentCaptor<Cliente> clienteGuardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteGuardado.capture());
        assertThat(clienteGuardado.getValue().getNombre()).isEqualTo("María Pérez");
        assertThat(clienteGuardado.getValue().getCorreo()).isEqualTo("maria@correo.com");

        ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioGuardado.capture());
        assertThat(usuarioGuardado.getValue().getRol()).isEqualTo(RolUsuario.CLIENTE);
        assertThat(usuarioGuardado.getValue().getPassword()).isEqualTo("hash-bcrypt");
        assertThat(usuarioGuardado.getValue().getCliente()).isSameAs(clienteGuardado.getValue());
        assertThat(usuarioGuardado.getValue().getNegocio()).isNull();

        verify(registroEventosSeguridad).registroCliente("maria");
    }

    @Test
    void registrarConUsuarioExistenteLanzaUsuarioDuplicado() {
        Usuario existente = new Usuario("maria", "hash", RolUsuario.CLIENTE, null, null);
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.registrar(solicitud()))
                .isInstanceOf(UsuarioDuplicadoException.class)
                .hasMessageContaining("usuario");

        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarConCorreoExistenteLanzaUsuarioDuplicado() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.empty());
        when(clienteRepository.findByCorreo("maria@correo.com"))
                .thenReturn(Optional.of(new Cliente("Otra", "maria@correo.com", "1")));

        assertThatThrownBy(() -> service.registrar(solicitud()))
                .isInstanceOf(UsuarioDuplicadoException.class)
                .hasMessageContaining("correo");

        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}