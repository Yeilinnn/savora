package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.business.dto.ReservaResumen;
import cr.ac.una.savora.business.excepcion.HoraLimiteSuperadaException;
import cr.ac.una.savora.business.excepcion.LimiteReservasActivasException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import cr.ac.una.savora.data.TipoPropietario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@Validated
public class ReservaService {

    private static final String ESTADO_PENDIENTE = "pendiente";

    private final ReservaRepository reservaRepository;
    private final PaqueteSorpresaRepository paqueteSorpresaRepository;
    private final ClienteRepository clienteRepository;
    private final PerfilImpactoService perfilImpactoService;
    private final Clock clock;

    public ReservaService(
            ReservaRepository reservaRepository,
            PaqueteSorpresaRepository paqueteSorpresaRepository,
            ClienteRepository clienteRepository,
            PerfilImpactoService perfilImpactoService,
            Clock clock) {
        this.reservaRepository = reservaRepository;
        this.paqueteSorpresaRepository = paqueteSorpresaRepository;
        this.clienteRepository = clienteRepository;
        this.perfilImpactoService = perfilImpactoService;
        this.clock = clock;
    }

    @Transactional
    public ReservaResumen reservar(@Valid CrearReservaRequest request) {
        PaqueteSorpresa paquete = paqueteSorpresaRepository.findById(request.paqueteSorpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("PaqueteSorpresa", request.paqueteSorpresaId()));

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.clienteId()));

        OffsetDateTime ahora = OffsetDateTime.now(clock);
        if (!paquete.getHoraLimiteRecogida().isAfter(ahora)) {
            throw new HoraLimiteSuperadaException(paquete.getId());
        }

        validarLimiteDeReservasActivas(cliente);

        // Patrón State: la transición valida por sí misma que el paquete esté DISPONIBLE.
        paquete.setEstado(EstadoPaquete.desde(paquete.getEstado()).transitarA(EstadoPaquete.RESERVADO));
        paqueteSorpresaRepository.save(paquete);

        Reserva reserva = reservaRepository.save(new Reserva(cliente, paquete, ahora, ESTADO_PENDIENTE));

        return new ReservaResumen(
                reserva.getId(),
                paquete.getId(),
                paquete.getDescripcion(),
                paquete.getPrecioConDescuento(),
                calcularDescuentoPorcentaje(paquete.getPrecioOriginal(), paquete.getPrecioConDescuento()),
                reserva.getFechaHoraReserva(),
                reserva.getEstado());
    }

    private void validarLimiteDeReservasActivas(Cliente cliente) {
        PerfilImpacto perfil = perfilImpactoService.obtenerOCrear(
                String.valueOf(cliente.getId()), TipoPropietario.CLIENTE);
        int limite = perfilImpactoService.calcularLimiteReservas(perfil);
        long activas = reservaRepository.countByClienteIdAndEstado(cliente.getId(), ESTADO_PENDIENTE);
        if (activas >= limite) {
            throw new LimiteReservasActivasException(cliente.getId(), limite);
        }
    }

    private BigDecimal calcularDescuentoPorcentaje(BigDecimal precioOriginal, BigDecimal precioConDescuento) {
        BigDecimal ahorro = precioOriginal.subtract(precioConDescuento);
        return ahorro
                .divide(precioOriginal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}