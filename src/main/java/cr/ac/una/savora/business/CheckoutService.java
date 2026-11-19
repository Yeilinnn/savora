package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.AutorizacionPago;
import cr.ac.una.savora.business.dto.CarritoPagadoRespuesta;
import cr.ac.una.savora.business.dto.CarritoPagoSolicitud;
import cr.ac.una.savora.business.dto.ComprobantePago;
import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.business.dto.ReservaConPagoSolicitud;
import cr.ac.una.savora.business.dto.ReservaPagadaRespuesta;
import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.SolicitudCuentaInvalidaException;
import cr.ac.una.savora.data.Pago;
import cr.ac.una.savora.data.PagoRepository;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Paga (simulado) y reserva. Primero se autoriza la tarjeta: si es rechazada no se toca ningún
 * paquete. Si una reserva falla (límite, hora límite, sin cantidad…) la transacción completa se
 * revierte y no queda ningún pago ni reserva registrados.
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger("pagos");
    private static final String ESTADO_APROBADO = "aprobado";

    private final PasarelaPagoSimulada pasarela;
    private final ReservaService reservaService;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final Clock clock;

    public CheckoutService(
            PasarelaPagoSimulada pasarela,
            ReservaService reservaService,
            ReservaRepository reservaRepository,
            PagoRepository pagoRepository,
            Clock clock) {
        this.pasarela = pasarela;
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservaPagadaRespuesta pagarYReservar(ReservaConPagoSolicitud solicitud, Long clienteIdAutenticado) {
        AutorizacionPago autorizacion = pasarela.autorizar(solicitud.tarjeta());

        ReservaResumen resumen = reservaService.reservar(
                new CrearReservaRequest(solicitud.paqueteSorpresaId()), clienteIdAutenticado);
        registrarPago(resumen, autorizacion, OffsetDateTime.now(clock));

        log.info(
                "evento=pago_simulado_aprobado reserva={} referencia={} monto={}",
                resumen.id(),
                autorizacion.referencia(),
                resumen.precioConDescuento());

        return new ReservaPagadaRespuesta(
                resumen.id(),
                resumen.paqueteSorpresaId(),
                resumen.descripcionPaquete(),
                resumen.precioConDescuento(),
                resumen.descuentoPorcentaje(),
                resumen.fechaHoraReserva(),
                resumen.estado(),
                new ComprobantePago(
                        autorizacion.referencia(),
                        resumen.precioConDescuento(),
                        autorizacion.marca(),
                        autorizacion.ultimos4(),
                        ESTADO_APROBADO));
    }

    /** Un solo pago para varios paquetes: cada reserva guarda su monto con la misma referencia. */
    @Transactional
    public CarritoPagadoRespuesta pagarCarrito(CarritoPagoSolicitud solicitud, Long clienteIdAutenticado) {
        List<Long> ids = solicitud.paqueteSorpresaIds();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new SolicitudCuentaInvalidaException("El carrito tiene paquetes repetidos");
        }

        AutorizacionPago autorizacion = pasarela.autorizar(solicitud.tarjeta());
        OffsetDateTime ahora = OffsetDateTime.now(clock);

        List<ReservaResumen> reservas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Long paqueteId : ids) {
            ReservaResumen resumen = reservaService.reservar(new CrearReservaRequest(paqueteId), clienteIdAutenticado);
            registrarPago(resumen, autorizacion, ahora);
            reservas.add(resumen);
            total = total.add(resumen.precioConDescuento());
        }

        log.info(
                "evento=pago_simulado_carrito_aprobado referencia={} paquetes={} total={}",
                autorizacion.referencia(),
                reservas.size(),
                total);

        return new CarritoPagadoRespuesta(
                autorizacion.referencia(),
                total,
                autorizacion.marca(),
                autorizacion.ultimos4(),
                ESTADO_APROBADO,
                reservas);
    }

    private void registrarPago(ReservaResumen resumen, AutorizacionPago autorizacion, OffsetDateTime fecha) {
        Reserva reserva = reservaRepository.findById(resumen.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", resumen.id()));
        pagoRepository.save(new Pago(
                reserva,
                resumen.precioConDescuento(),
                ESTADO_APROBADO,
                autorizacion.marca(),
                autorizacion.ultimos4(),
                autorizacion.referencia(),
                fecha));
    }
}