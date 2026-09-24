package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.excepcion.HoraLimiteSuperadaException;
import cr.ac.una.savora.business.excepcion.LimiteReservasActivasException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.TransicionEstadoInvalidaException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    ReservaRepository reservaRepository;
    @Mock
    PaqueteSorpresaRepository paqueteSorpresaRepository;
    @Mock
    ClienteRepository clienteRepository;
    @Mock
    PerfilImpactoService perfilImpactoService;

    ReservaService service;

    // 2026-08-27T16:00:00Z == 2026-08-27T10:00:00-06:00 (hora de Costa Rica)
    final Clock clock = Clock.fixed(Instant.parse("2026-08-27T16:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new ReservaService(
                reservaRepository, paqueteSorpresaRepository, clienteRepository, perfilImpactoService, clock);
    }

    private PaqueteSorpresa paqueteConHoraLimite(Long id, String horaLimiteIso) {
        Negocio negocio = new Negocio("Panadería La Espiga", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 1L);
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Pan del día", 10,
                new BigDecimal("6000.00"), new BigDecimal("2500.00"),
                OffsetDateTime.parse(horaLimiteIso), "disponible");
        ReflectionTestUtils.setField(paquete, "id", id);
        return paquete;
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente("Ana Rojas", "ana@example.com", "8888-1111");
        ReflectionTestUtils.setField(cliente, "id", id);
        return cliente;
    }

    @Test
    void reservaExitosaCuandoTodoEstaEnRegla() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00");
        Cliente cliente = cliente(1L);
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(perfilImpactoService.obtenerOCrear("1", TipoPropietario.CLIENTE)).thenReturn(perfil);
        when(perfilImpactoService.calcularLimiteReservas(perfil)).thenReturn(3);
        when(reservaRepository.countByClienteIdAndEstado(1L, "pendiente")).thenReturn(0L);
        when(paqueteSorpresaRepository.save(any(PaqueteSorpresa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 100L);
            return r;
        });

        ReservaResumen resumen = service.reservar(new CrearReservaRequest(1L, 5L));

        assertThat(resumen.id()).isEqualTo(100L);
        assertThat(resumen.estado()).isEqualTo("pendiente");
        assertThat(paquete.getEstado()).isEqualTo("reservado");
        verify(paqueteSorpresaRepository).save(paquete);
    }

    @Test
    void calculaElPorcentajeDeDescuentoCorrectamente() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00"); // 6000 -> 2500
        Cliente cliente = cliente(1L);
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(perfilImpactoService.obtenerOCrear("1", TipoPropietario.CLIENTE)).thenReturn(perfil);
        when(perfilImpactoService.calcularLimiteReservas(perfil)).thenReturn(3);
        when(reservaRepository.countByClienteIdAndEstado(1L, "pendiente")).thenReturn(0L);
        when(paqueteSorpresaRepository.save(any(PaqueteSorpresa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResumen resumen = service.reservar(new CrearReservaRequest(1L, 5L));

        assertThat(resumen.descuentoPorcentaje()).isEqualByComparingTo("58.3");
    }

    @Test
    void lanzaExcepcionSiElPaqueteNoExiste() {
        when(paqueteSorpresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(new CrearReservaRequest(1L, 99L)))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void lanzaExcepcionSiElClienteNoExiste() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00");
        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(new CrearReservaRequest(1L, 5L)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void lanzaExcepcionSiYaPasoLaHoraLimite() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T08:00:00-06:00");
        Cliente cliente = cliente(1L);
        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> service.reservar(new CrearReservaRequest(1L, 5L)))
                .isInstanceOf(HoraLimiteSuperadaException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void lanzaExcepcionSiElClienteYaAlcanzoSuLimiteDeReservasActivas() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00");
        Cliente cliente = cliente(1L);
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(perfilImpactoService.obtenerOCrear("1", TipoPropietario.CLIENTE)).thenReturn(perfil);
        when(perfilImpactoService.calcularLimiteReservas(perfil)).thenReturn(3);
        when(reservaRepository.countByClienteIdAndEstado(1L, "pendiente")).thenReturn(3L);

        assertThatThrownBy(() -> service.reservar(new CrearReservaRequest(1L, 5L)))
                .isInstanceOf(LimiteReservasActivasException.class);

        verify(paqueteSorpresaRepository, never()).save(any());
    }

    @Test
    void unClienteConfiablePuedeReservarPorEncimaDelLimiteBase() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00");
        Cliente cliente = cliente(1L);
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(perfilImpactoService.obtenerOCrear("1", TipoPropietario.CLIENTE)).thenReturn(perfil);
        when(perfilImpactoService.calcularLimiteReservas(perfil)).thenReturn(5); // confiable
        when(reservaRepository.countByClienteIdAndEstado(1L, "pendiente")).thenReturn(4L); // > límite base (3)
        when(paqueteSorpresaRepository.save(any(PaqueteSorpresa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResumen resumen = service.reservar(new CrearReservaRequest(1L, 5L));

        assertThat(resumen).isNotNull();
    }

    @Test
    void lanzaExcepcionSiElPaqueteYaNoEstaDisponible() {
        PaqueteSorpresa paquete = paqueteConHoraLimite(5L, "2026-08-27T21:30:00-06:00");
        paquete.setEstado("reservado");
        Cliente cliente = cliente(1L);
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        when(paqueteSorpresaRepository.findById(5L)).thenReturn(Optional.of(paquete));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(perfilImpactoService.obtenerOCrear("1", TipoPropietario.CLIENTE)).thenReturn(perfil);
        when(perfilImpactoService.calcularLimiteReservas(perfil)).thenReturn(3);
        when(reservaRepository.countByClienteIdAndEstado(1L, "pendiente")).thenReturn(0L);

        assertThatThrownBy(() -> service.reservar(new CrearReservaRequest(1L, 5L)))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }
}