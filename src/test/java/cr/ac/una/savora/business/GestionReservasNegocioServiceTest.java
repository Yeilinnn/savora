package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ReservaNegocioResumen;
import cr.ac.una.savora.business.excepcion.CierreAntesDeHoraLimiteException;
import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.ReservaNoPendienteException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionReservasNegocioServiceTest {

    private static final String HORA_PASADA = "2026-10-06T11:00:00Z";
    private static final String HORA_FUTURA = "2026-10-06T13:00:00Z";

    @Mock
    ReservaRepository reservaRepository;
    @Mock
    PerfilImpactoService perfilImpactoService;

    GestionReservasNegocioService service;

    final Clock clock = Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new GestionReservasNegocioService(reservaRepository, perfilImpactoService, clock);
    }

    private Reserva reserva(String estadoReserva, String estadoPaquete, String horaLimiteIso) {
        Negocio negocio = new Negocio("Panadería La Espiga", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 1L);
        Cliente cliente = new Cliente("Ana Mora", "ana@test.com", "88888888");
        ReflectionTestUtils.setField(cliente, "id", 7L);
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Canasta de pan", 1,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2500),
                OffsetDateTime.parse(horaLimiteIso), estadoPaquete);
        ReflectionTestUtils.setField(paquete, "id", 2L);
        Reserva reserva = new Reserva(cliente, paquete, OffsetDateTime.parse("2026-10-06T10:00:00Z"), estadoReserva);
        ReflectionTestUtils.setField(reserva, "id", 5L);
        return reserva;
    }

    @Test
    void marcarRecogidaActualizaReservaPaqueteEImpactoDelCliente() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        ReservaNegocioResumen resumen = service.marcarRecogida(5L, 1L);

        assertThat(resumen.estado()).isEqualTo("recogido");
        assertThat(resumen.estadoPaquete()).isEqualTo("recogido");
        assertThat(resumen.nombreCliente()).isEqualTo("Ana Mora");
        verify(reservaRepository).save(reserva);
        verify(perfilImpactoService).registrarRecogida("7");
    }

    @Test
    void marcarRecogidaConPaqueteAgotadoSoloCambiaLaReserva() {
        Reserva reserva = reserva("pendiente", "agotado", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        ReservaNegocioResumen resumen = service.marcarRecogida(5L, 1L);

        assertThat(resumen.estado()).isEqualTo("recogido");
        assertThat(resumen.estadoPaquete()).isEqualTo("agotado");
        verify(perfilImpactoService).registrarRecogida("7");
    }

    @Test
    void marcarRecogidaDeReservaDeOtroNegocioLanzaPropiedadRecurso() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.marcarRecogida(5L, 2L)).isInstanceOf(PropiedadRecursoException.class);

        verify(reservaRepository, never()).save(any(Reserva.class));
        verifyNoInteractions(perfilImpactoService);
    }

    @Test
    void marcarRecogidaSinNegocioAutenticadoLanzaPropiedadRecurso() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.marcarRecogida(5L, null)).isInstanceOf(PropiedadRecursoException.class);
    }

    @Test
    void marcarRecogidaDeReservaYaProcesadaLanzaReservaNoPendiente() {
        Reserva reserva = reserva("recogido", "recogido", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.marcarRecogida(5L, 1L)).isInstanceOf(ReservaNoPendienteException.class);

        verifyNoInteractions(perfilImpactoService);
    }

    @Test
    void marcarRecogidaDeReservaInexistenteLanzaRecursoNoEncontrado() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarRecogida(99L, 1L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void marcarNoRecogidaDespuesDeLaHoraLimiteActualizaElImpacto() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_PASADA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        ReservaNegocioResumen resumen = service.marcarNoRecogida(5L, 1L);

        assertThat(resumen.estado()).isEqualTo("no_recogido");
        verify(reservaRepository).save(reserva);
        verify(perfilImpactoService).registrarNoRecogida("7");
    }

    @Test
    void marcarNoRecogidaAntesDeLaHoraLimiteLanzaCierreAntesDeHoraLimite() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_FUTURA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> service.marcarNoRecogida(5L, 1L))
                .isInstanceOf(CierreAntesDeHoraLimiteException.class);

        verify(reservaRepository, never()).save(any(Reserva.class));
        verifyNoInteractions(perfilImpactoService);
    }

    @Test
    void listarMapeaLasReservasDelNegocio() {
        Reserva reserva = reserva("pendiente", "reservado", HORA_FUTURA);
        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reserva)));

        var pagina = service.listar(1L, "pendiente", Pageable.unpaged());

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent().get(0).descripcionPaquete()).isEqualTo("Canasta de pan");
    }
}