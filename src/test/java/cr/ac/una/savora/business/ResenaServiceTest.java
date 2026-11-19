package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ResenaPromedio;
import cr.ac.una.savora.business.dto.ResenaResumen;
import cr.ac.una.savora.business.dto.ResenaSolicitud;
import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.ResenaDuplicadaException;
import cr.ac.una.savora.business.excepcion.ResenaNoPermitidaException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import cr.ac.una.savora.data.Resena;
import cr.ac.una.savora.data.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    ResenaRepository resenaRepository;
    @Mock
    ReservaRepository reservaRepository;

    ResenaService service;

    final Clock clock = Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new ResenaService(resenaRepository, reservaRepository, clock);
    }

    private Reserva reserva(String estado) {
        Negocio negocio = new Negocio("Panadería La Espiga", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        Categoria categoria = new Categoria("Panadería");
        ReflectionTestUtils.setField(categoria, "id", 1L);
        Cliente cliente = new Cliente("Ana María Mora", "ana@test.com", "88888888");
        ReflectionTestUtils.setField(cliente, "id", 7L);
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Canasta de pan", 1,
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2500),
                OffsetDateTime.parse("2026-11-19T20:00:00Z"), "reservado");
        ReflectionTestUtils.setField(paquete, "id", 2L);
        Reserva reserva = new Reserva(cliente, paquete, OffsetDateTime.parse("2026-11-19T10:00:00Z"), estado);
        ReflectionTestUtils.setField(reserva, "id", 5L);
        return reserva;
    }

    @Test
    void elClienteDuenoPuedeCalificarUnaReservaRecogida() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("recogido")));
        when(resenaRepository.existsByReservaId(5L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        ResenaResumen resumen = service.crear(5L, 7L, new ResenaSolicitud(5, "  Pan fresco  "));

        assertThat(resumen.calificacion()).isEqualTo(5);
        assertThat(resumen.comentario()).isEqualTo("Pan fresco");
        assertThat(resumen.negocioId()).isEqualTo(1L);
        assertThat(resumen.paqueteSorpresaId()).isEqualTo(2L);
        assertThat(resumen.nombreCliente()).isEqualTo("Ana");
        assertThat(resumen.fecha()).isEqualTo(Instant.parse("2026-11-19T18:00:00Z"));
    }

    @Test
    void unComentarioEnBlancoSeGuardaComoNulo() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("recogido")));
        when(resenaRepository.existsByReservaId(5L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(service.crear(5L, 7L, new ResenaSolicitud(4, "   ")).comentario()).isNull();
    }

    @Test
    void unaReservaInexistenteDa404() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(99L, 7L, new ResenaSolicitud(5, null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void unClienteNoPuedeCalificarLaReservaDeOtro() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("recogido")));

        assertThatThrownBy(() -> service.crear(5L, 8L, new ResenaSolicitud(5, null)))
                .isInstanceOf(PropiedadRecursoException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void soloSeCalificaLoQueFueRecogido() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("pendiente")));

        assertThatThrownBy(() -> service.crear(5L, 7L, new ResenaSolicitud(5, null)))
                .isInstanceOf(ResenaNoPermitidaException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void noSePermiteUnaSegundaResenaDeLaMismaReserva() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("recogido")));
        when(resenaRepository.existsByReservaId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.crear(5L, 7L, new ResenaSolicitud(5, null)))
                .isInstanceOf(ResenaDuplicadaException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void siElIndiceUnicoRechazaLaCarreraSeTraduceAConflicto() {
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva("recogido")));
        when(resenaRepository.existsByReservaId(5L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenThrow(new DuplicateKeyException("dup"));

        assertThatThrownBy(() -> service.crear(5L, 7L, new ResenaSolicitud(5, null)))
                .isInstanceOf(ResenaDuplicadaException.class);
    }

    private Resena resena(Long negocioId, int calificacion) {
        return new Resena(1L, negocioId, 2L, 7L, "Ana", calificacion, null, Instant.now(clock));
    }

    @Test
    void elPromedioSeRedondeaAUnDecimalYSeCuentanLasResenas() {
        when(resenaRepository.findByNegocioIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(resena(1L, 5), resena(1L, 4), resena(1L, 4), resena(2L, 3)));

        Map<Long, ResenaPromedio> promedios = service.promediosPorNegocio(List.of(1L, 2L));

        assertThat(promedios.get(1L).promedio()).isEqualTo(4.3);
        assertThat(promedios.get(1L).total()).isEqualTo(3);
        assertThat(promedios.get(2L).promedio()).isEqualTo(3.0);
    }

    @Test
    void unNegocioSinResenasNoApareceEnElMapaYSuResumenVieneVacio() {
        when(resenaRepository.findByNegocioIdIn(List.of(9L))).thenReturn(List.of());

        assertThat(service.promediosPorNegocio(List.of(9L))).isEmpty();
        assertThat(service.resumenDeNegocio(9L)).isEqualTo(new ResenaPromedio(9L, null, 0));
    }

    @Test
    void sinNegociosNoConsultaMongo() {
        assertThat(service.promediosPorNegocio(List.of())).isEmpty();
        verify(resenaRepository, never()).findByNegocioIdIn(any());
    }

    @Test
    void lasReservasCalificadasSonLasDelCliente() {
        when(resenaRepository.findByClienteId(7L)).thenReturn(List.of(resena(1L, 5)));

        assertThat(service.reservasCalificadasPor(7L)).containsExactly(1L);
    }
}