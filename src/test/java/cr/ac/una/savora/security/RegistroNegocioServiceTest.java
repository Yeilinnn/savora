package cr.ac.una.savora.security;

import cr.ac.una.savora.business.dto.MensajeRespuesta;
import cr.ac.una.savora.business.dto.RegistroNegocioSolicitud;
import cr.ac.una.savora.business.excepcion.UsuarioDuplicadoException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
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

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroNegocioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    NegocioRepository negocioRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RegistroEventosSeguridad registroEventosSeguridad;

    RegistroNegocioService service;

    @BeforeEach
    void setUp() {
        service = new RegistroNegocioService(
                usuarioRepository, negocioRepository, passwordEncoder, registroEventosSeguridad);
    }

    private RegistroNegocioSolicitud solicitud() {
        return new RegistroNegocioSolicitud(
                "  Panadería La Espiga ", "Panadería", " Santa Cruz ", LocalTime.of(19, 0), "espiga", "secreta123");
    }

    @Test
    void registrarCreaNegocioPendienteYUsuarioConContrasenaCifrada() {
        when(usuarioRepository.findByUsername("espiga")).thenReturn(Optional.empty());
        when(negocioRepository.save(any(Negocio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(passwordEncoder.encode("secreta123")).thenReturn("hash-bcrypt");

        MensajeRespuesta respuesta = service.registrar(solicitud());

        assertThat(respuesta.mensaje()).contains("administrador");

        ArgumentCaptor<Negocio> negocio = ArgumentCaptor.forClass(Negocio.class);
        verify(negocioRepository).save(negocio.capture());
        assertThat(negocio.getValue().getNombre()).isEqualTo("Panadería La Espiga");
        assertThat(negocio.getValue().getUbicacion()).isEqualTo("Santa Cruz");
        assertThat(negocio.getValue().getEstado()).isEqualTo(Negocio.PENDIENTE);
        assertThat(negocio.getValue().estaAprobado()).isFalse();

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuario.capture());
        assertThat(usuario.getValue().getRol()).isEqualTo(RolUsuario.NEGOCIO);
        assertThat(usuario.getValue().getPassword()).isEqualTo("hash-bcrypt");
        assertThat(usuario.getValue().getNegocio()).isSameAs(negocio.getValue());
        assertThat(usuario.getValue().getCliente()).isNull();

        verify(registroEventosSeguridad).negocioRegistrado("espiga");
    }

    @Test
    void registrarConUsuarioExistenteLanzaUsuarioDuplicado() {
        when(usuarioRepository.findByUsername("espiga"))
                .thenReturn(Optional.of(new Usuario("espiga", "hash", RolUsuario.NEGOCIO, null, null)));

        assertThatThrownBy(() -> service.registrar(solicitud())).isInstanceOf(UsuarioDuplicadoException.class);

        verify(negocioRepository, never()).save(any(Negocio.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}