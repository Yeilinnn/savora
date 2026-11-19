package cr.ac.una.savora.security;

import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.config.RegistroEventosSeguridad;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.RestablecimientoContrasena;
import cr.ac.una.savora.data.RestablecimientoContrasenaRepository;
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

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionContrasenaServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    ClienteRepository clienteRepository;
    @Mock
    RestablecimientoContrasenaRepository restablecimientoRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    EnviadorCorreo enviadorCorreo;
    @Mock
    RegistroEventosSeguridad registroEventosSeguridad;

    GestionContrasenaService service;
    Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new GestionContrasenaService(
                usuarioRepository, clienteRepository, restablecimientoRepository, passwordEncoder,
                enviadorCorreo, registroEventosSeguridad, RELOJ, "https://spa.test/");
        usuario = new Usuario("maria", "hash-viejo", RolUsuario.CLIENTE, null,
                new Cliente("María", "maria@correo.com", "88888888"));
        ReflectionTestUtils.setField(usuario, "id", 7L);
    }

    @Test
    void solicitarConCorreoRegistradoGuardaHashDelTokenYEnviaEnlace() {
        Cliente cliente = new Cliente("María", "maria@correo.com", "88888888");
        ReflectionTestUtils.setField(cliente, "id", 3L);
        when(clienteRepository.findByCorreo("maria@correo.com")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByClienteId(3L)).thenReturn(Optional.of(usuario));
        when(restablecimientoRepository.findByUsuarioIdAndUsadoEnIsNull(7L)).thenReturn(List.of());

        service.solicitarRestablecimiento("  Maria@Correo.COM ");

        ArgumentCaptor<RestablecimientoContrasena> guardado = ArgumentCaptor.forClass(RestablecimientoContrasena.class);
        verify(restablecimientoRepository).save(guardado.capture());
        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreo).enviar(org.mockito.ArgumentMatchers.eq("maria@correo.com"), anyString(), cuerpo.capture());

        String texto = cuerpo.getValue();
        assertThat(texto).contains("https://spa.test/#/restablecer?token=");
        String token = texto.substring(texto.indexOf("token=") + 6).lines().findFirst().orElseThrow();
        assertThat(guardado.getValue().getTokenHash()).isEqualTo(GestionContrasenaService.hash(token));
        assertThat(guardado.getValue().getTokenHash()).doesNotContain(token);
        assertThat(guardado.getValue().getExpiraEn()).isEqualTo(OffsetDateTime.now(RELOJ).plusMinutes(30));
    }

    @Test
    void solicitarConCorreoDesconocidoNoHaceNadaNiLanzaError() {
        when(clienteRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        service.solicitarRestablecimiento("nadie@correo.com");

        verify(enviadorCorreo, never()).enviar(anyString(), anyString(), anyString());
        verify(restablecimientoRepository, never()).save(any());
    }

    @Test
    void solicitarNoFallaSiElCorreoNoSePuedeEnviar() {
        Cliente cliente = new Cliente("María", "maria@correo.com", "88888888");
        ReflectionTestUtils.setField(cliente, "id", 3L);
        when(clienteRepository.findByCorreo("maria@correo.com")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByClienteId(3L)).thenReturn(Optional.of(usuario));
        when(restablecimientoRepository.findByUsuarioIdAndUsadoEnIsNull(7L)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new IllegalStateException("caído"))
                .when(enviadorCorreo).enviar(anyString(), anyString(), anyString());

        service.solicitarRestablecimiento("maria@correo.com");

        verify(restablecimientoRepository).save(any());
    }

    @Test
    void restablecerConTokenVigenteCambiaLaContrasenaYConsumeElEnlace() {
        RestablecimientoContrasena registro = new RestablecimientoContrasena(
                usuario, GestionContrasenaService.hash("tok"), OffsetDateTime.now(RELOJ).plusMinutes(10));
        when(restablecimientoRepository.findByTokenHash(GestionContrasenaService.hash("tok")))
                .thenReturn(Optional.of(registro));
        when(passwordEncoder.encode("nueva-clave-1")).thenReturn("hash-nuevo");
        when(restablecimientoRepository.findByUsuarioIdAndUsadoEnIsNull(7L)).thenReturn(List.of());

        service.restablecer("tok", "nueva-clave-1");

        assertThat(usuario.getPassword()).isEqualTo("hash-nuevo");
        assertThat(registro.getUsadoEn()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void restablecerConTokenVencidoOUsadoOInexistenteFalla() {
        RestablecimientoContrasena vencido = new RestablecimientoContrasena(
                usuario, GestionContrasenaService.hash("viejo"), OffsetDateTime.now(RELOJ).minusMinutes(1));
        RestablecimientoContrasena usado = new RestablecimientoContrasena(
                usuario, GestionContrasenaService.hash("usado"), OffsetDateTime.now(RELOJ).plusMinutes(10));
        usado.marcarUsado(OffsetDateTime.now(RELOJ).minusMinutes(1));
        when(restablecimientoRepository.findByTokenHash(GestionContrasenaService.hash("viejo")))
                .thenReturn(Optional.of(vencido));
        when(restablecimientoRepository.findByTokenHash(GestionContrasenaService.hash("usado")))
                .thenReturn(Optional.of(usado));
        when(restablecimientoRepository.findByTokenHash(GestionContrasenaService.hash("falso")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restablecer("viejo", "nueva-clave-1"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);
        assertThatThrownBy(() -> service.restablecer("usado", "nueva-clave-1"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);
        assertThatThrownBy(() -> service.restablecer("falso", "nueva-clave-1"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarConClaveActualCorrectaActualizaElHash() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("actual-123", "hash-viejo")).thenReturn(true);
        when(passwordEncoder.matches("nueva-123", "hash-viejo")).thenReturn(false);
        when(passwordEncoder.encode("nueva-123")).thenReturn("hash-nuevo");

        service.cambiar("maria", "actual-123", "nueva-123");

        assertThat(usuario.getPassword()).isEqualTo("hash-nuevo");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarConClaveActualIncorrectaFalla() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash-viejo")).thenReturn(false);

        assertThatThrownBy(() -> service.cambiar("maria", "mala", "nueva-123"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class)
                .hasMessageContaining("actual");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarConLaMismaClaveFalla() {
        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("misma-1234", "hash-viejo")).thenReturn(true);

        assertThatThrownBy(() -> service.cambiar("maria", "misma-1234", "misma-1234"))
                .isInstanceOf(SolicitudCuentaInvalidaException.class)
                .hasMessageContaining("distinta");
    }
}