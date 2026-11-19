package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.AutorizacionPago;
import cr.ac.una.savora.business.dto.CarritoPagadoRespuesta;
import cr.ac.una.savora.business.dto.CarritoPagoSolicitud;
import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.business.dto.ReservaConPagoSolicitud;
import cr.ac.una.savora.business.dto.ReservaPagadaRespuesta;
import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.dto.TarjetaSimulada;
import cr.ac.una.savora.business.excepcion.PagoRechazadoException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.data.Pago;
import cr.ac.una.savora.data.PagoRepository;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    PasarelaPagoSimulada pasarela;
    @Mock
    ReservaService reservaService;
    @Mock
    ReservaRepository reservaRepository;
    @Mock
    PagoRepository pagoRepository;
    @Mock
    Reserva reserva;
    @Mock
    Reserva otraReserva;

    CheckoutService service;

    private final TarjetaSimulada tarjeta = new TarjetaSimulada("4242 4242 4242 4242", "María Pérez", "12/30", "123");

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneOffset.UTC);
        service = new CheckoutService(pasarela, reservaService, reservaRepository, pagoRepository, reloj);
    }

    private static ReservaResumen resumen(long id, long paqueteId, String descripcion, String precio) {
        return new ReservaResumen(
                id,
                paqueteId,
                descripcion,
                new BigDecimal(precio),
                new BigDecimal("50.0"),
                OffsetDateTime.parse("2026-11-18T12:00:00Z"),
                "pendiente");
    }

    @Test
    void pagoAprobadoCreaLaReservaYGuardaElComprobante() {
        when(pasarela.autorizar(tarjeta)).thenReturn(new AutorizacionPago("SIM-ABC12345", "Visa", "4242"));
        when(reservaService.reservar(any(CrearReservaRequest.class), eq(7L)))
                .thenReturn(resumen(10L, 5L, "Pan del día", "2500.00"));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        ReservaPagadaRespuesta respuesta = service.pagarYReservar(new ReservaConPagoSolicitud(5L, tarjeta), 7L);

        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.estado()).isEqualTo("pendiente");
        assertThat(respuesta.pago().referencia()).isEqualTo("SIM-ABC12345");
        assertThat(respuesta.pago().ultimos4()).isEqualTo("4242");
        assertThat(respuesta.pago().monto()).isEqualByComparingTo("2500.00");

        ArgumentCaptor<Pago> pago = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(pago.capture());
        assertThat(pago.getValue().getReserva()).isSameAs(reserva);
        assertThat(pago.getValue().getMonto()).isEqualByComparingTo("2500.00");
        assertThat(pago.getValue().getEstado()).isEqualTo("aprobado");
        assertThat(pago.getValue().getMarca()).isEqualTo("Visa");
        assertThat(pago.getValue().getUltimos4()).isEqualTo("4242");
        assertThat(pago.getValue().getReferencia()).isEqualTo("SIM-ABC12345");
    }

    @Test
    void pagoRechazadoNoTocaElPaqueteNiGuardaNada() {
        when(pasarela.autorizar(tarjeta)).thenThrow(new PagoRechazadoException("rechazada"));

        assertThatThrownBy(() -> service.pagarYReservar(new ReservaConPagoSolicitud(5L, tarjeta), 7L))
                .isInstanceOf(PagoRechazadoException.class);

        verifyNoInteractions(reservaService, reservaRepository, pagoRepository);
    }

    @Test
    void siLaReservaFallaNoQuedaPagoRegistrado() {
        when(pasarela.autorizar(tarjeta)).thenReturn(new AutorizacionPago("SIM-ABC12345", "Visa", "4242"));
        when(reservaService.reservar(any(CrearReservaRequest.class), eq(7L)))
                .thenThrow(new RecursoNoEncontradoException("PaqueteSorpresa", 5L));

        assertThatThrownBy(() -> service.pagarYReservar(new ReservaConPagoSolicitud(5L, tarjeta), 7L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verifyNoInteractions(pagoRepository);
    }

    @Test
    void carritoCobraUnaVezYReservaTodosLosPaquetes() {
        when(pasarela.autorizar(tarjeta)).thenReturn(new AutorizacionPago("SIM-CARRITO1", "Visa", "4242"));
        when(reservaService.reservar(any(CrearReservaRequest.class), eq(7L)))
                .thenReturn(resumen(10L, 5L, "Pan del día", "2500.00"), resumen(11L, 6L, "Casado", "1800.00"));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.findById(11L)).thenReturn(Optional.of(otraReserva));

        CarritoPagadoRespuesta respuesta =
                service.pagarCarrito(new CarritoPagoSolicitud(List.of(5L, 6L), tarjeta), 7L);

        assertThat(respuesta.total()).isEqualByComparingTo("4300.00");
        assertThat(respuesta.referencia()).isEqualTo("SIM-CARRITO1");
        assertThat(respuesta.estado()).isEqualTo("aprobado");
        assertThat(respuesta.reservas()).hasSize(2);

        verify(pasarela, times(1)).autorizar(tarjeta);
        ArgumentCaptor<Pago> pagos = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository, times(2)).save(pagos.capture());
        assertThat(pagos.getAllValues()).extracting(Pago::getReferencia).containsOnly("SIM-CARRITO1");
        assertThat(pagos.getAllValues()).extracting(Pago::getReserva).containsExactly(reserva, otraReserva);
    }

    @Test
    void carritoConPaquetesRepetidosSeRechazaSinCobrar() {
        assertThatThrownBy(() -> service.pagarCarrito(new CarritoPagoSolicitud(List.of(5L, 5L), tarjeta), 7L))
                .isInstanceOf(SolicitudCuentaInvalidaException.class);

        verifyNoInteractions(pasarela, reservaService, reservaRepository, pagoRepository);
    }

    @Test
    void carritoConTarjetaRechazadaNoReservaNada() {
        when(pasarela.autorizar(tarjeta)).thenThrow(new PagoRechazadoException("rechazada"));

        assertThatThrownBy(() -> service.pagarCarrito(new CarritoPagoSolicitud(List.of(5L, 6L), tarjeta), 7L))
                .isInstanceOf(PagoRechazadoException.class);

        verifyNoInteractions(reservaService, reservaRepository, pagoRepository);
    }
}