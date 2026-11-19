package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.ReservaNegocioResumen;
import cr.ac.una.savora.business.excepcion.CierreAntesDeHoraLimiteException;
import cr.ac.una.savora.business.excepcion.PropiedadRecursoException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.ReservaNoPendienteException;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaEspecificaciones;
import cr.ac.una.savora.data.ReservaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Casos de uso del negocio sobre las reservas de sus paquetes: consultarlas y marcarlas como
 * recogidas o no recogidas. Al cerrar el ciclo de una reserva se actualiza el PerfilImpacto
 * del cliente (Proceso 3 de la propuesta de dominio).
 */
@Service
public class GestionReservasNegocioService {

    private static final String ESTADO_PENDIENTE = "pendiente";
    private static final String ESTADO_RECOGIDO = "recogido";
    private static final String ESTADO_NO_RECOGIDO = "no_recogido";

    private final ReservaRepository reservaRepository;
    private final PerfilImpactoService perfilImpactoService;
    private final Clock clock;

    public GestionReservasNegocioService(
            ReservaRepository reservaRepository, PerfilImpactoService perfilImpactoService, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.perfilImpactoService = perfilImpactoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<ReservaNegocioResumen> listar(Long negocioId, String estado, Pageable pageable) {
        Specification<Reserva> spec = Specification.allOf(
                ReservaEspecificaciones.delNegocio(negocioId),
                ReservaEspecificaciones.conEstado(estado),
                ReservaEspecificaciones.conPaqueteYCliente());
        return reservaRepository.findAll(spec, pageable).map(GestionReservasNegocioService::aResumen);
    }

    @Transactional
    public ReservaNegocioResumen marcarRecogida(Long reservaId, Long negocioIdAutenticado) {
        Reserva reserva = buscarDelNegocio(reservaId, negocioIdAutenticado);
        exigirPendiente(reserva);

        PaqueteSorpresa paquete = reserva.getPaqueteSorpresa();
        reserva.setEstado(ESTADO_RECOGIDO);
        // Un paquete agotado (última unidad reservada) no tiene transición a "recogido" en la
        // máquina de estados; en ese caso solo cambia la reserva.
        if (EstadoPaquete.RESERVADO.getValor().equals(paquete.getEstado())) {
            paquete.setEstado(EstadoPaquete.RESERVADO.transitarA(EstadoPaquete.RECOGIDO));
        }
        reservaRepository.save(reserva);

        perfilImpactoService.registrarRecogida(String.valueOf(reserva.getCliente().getId()));
        return aResumen(reserva);
    }

    @Transactional
    public ReservaNegocioResumen marcarNoRecogida(Long reservaId, Long negocioIdAutenticado) {
        Reserva reserva = buscarDelNegocio(reservaId, negocioIdAutenticado);
        exigirPendiente(reserva);

        PaqueteSorpresa paquete = reserva.getPaqueteSorpresa();
        if (paquete.getHoraLimiteRecogida().isAfter(OffsetDateTime.now(clock))) {
            throw new CierreAntesDeHoraLimiteException(paquete.getId());
        }

        reserva.setEstado(ESTADO_NO_RECOGIDO);
        reservaRepository.save(reserva);

        perfilImpactoService.registrarNoRecogida(String.valueOf(reserva.getCliente().getId()));
        return aResumen(reserva);
    }

    private Reserva buscarDelNegocio(Long reservaId, Long negocioIdAutenticado) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", reservaId));
        if (negocioIdAutenticado == null
                || !reserva.getPaqueteSorpresa().getNegocio().getId().equals(negocioIdAutenticado)) {
            throw new PropiedadRecursoException("Reserva", reservaId);
        }
        return reserva;
    }

    private void exigirPendiente(Reserva reserva) {
        if (!ESTADO_PENDIENTE.equals(reserva.getEstado())) {
            throw new ReservaNoPendienteException(reserva.getId(), reserva.getEstado());
        }
    }

    private static ReservaNegocioResumen aResumen(Reserva reserva) {
        PaqueteSorpresa paquete = reserva.getPaqueteSorpresa();
        return new ReservaNegocioResumen(
                reserva.getId(),
                paquete.getId(),
                paquete.getDescripcion(),
                reserva.getCliente().getNombre(),
                paquete.getPrecioConDescuento(),
                reserva.getFechaHoraReserva(),
                paquete.getHoraLimiteRecogida(),
                reserva.getEstado(),
                paquete.getEstado());
    }
}