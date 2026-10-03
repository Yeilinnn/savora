package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CerrarPaqueteRequest;
import cr.ac.una.savora.business.dto.CierreResumen;
import cr.ac.una.savora.business.excepcion.CierreAntesDeHoraLimiteException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Donacion;
import cr.ac.una.savora.data.DonacionRepository;
import cr.ac.una.savora.data.OrganizacionComunitaria;
import cr.ac.una.savora.data.OrganizacionComunitariaRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaEspecificaciones;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import jakarta.validation.Valid;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Validated
public class CierreDePaquetesService {

    private static final String RESULTADO_DONADO = "DONADO";
    private static final String RESULTADO_PERDIDO = "PERDIDO";
    private static final String ESTADO_DONACION_ACEPTADA = "aceptada";
    private static final String ESTADO_DISPONIBLE = "disponible";
    private static final String ESTADO_DONADO = "donado";

    private final PaqueteSorpresaRepository paqueteSorpresaRepository;
    private final OrganizacionComunitariaRepository organizacionComunitariaRepository;
    private final DonacionRepository donacionRepository;
    private final Clock clock;

    public CierreDePaquetesService(
            PaqueteSorpresaRepository paqueteSorpresaRepository,
            OrganizacionComunitariaRepository organizacionComunitariaRepository,
            DonacionRepository donacionRepository,
            Clock clock) {
        this.paqueteSorpresaRepository = paqueteSorpresaRepository;
        this.organizacionComunitariaRepository = organizacionComunitariaRepository;
        this.donacionRepository = donacionRepository;
        this.clock = clock;
    }

    /**
     * Patrón Specification (Lab 3): compone filtros de negocio para localizar paquetes
     * disponibles cuya hora límite ya venció, antes de ejecutar el cierre uno a uno.
     */
    @Transactional
    public List<CierreResumen> cerrarPaquetesVencidosDisponibles() {
        OffsetDateTime ahora = OffsetDateTime.now(clock);
        Specification<PaqueteSorpresa> spec = Specification.allOf(
                PaqueteSorpresaEspecificaciones.conEstado(ESTADO_DISPONIBLE),
                PaqueteSorpresaEspecificaciones.horaLimiteVencida(ahora));
        return paqueteSorpresaRepository.findAll(spec).stream()
                .map(paquete -> cerrar(new CerrarPaqueteRequest(paquete.getId())))
                .toList();
    }

    @Transactional
    public CierreResumen cerrar(@Valid CerrarPaqueteRequest request) {
        PaqueteSorpresa paquete = paqueteSorpresaRepository.findById(request.paqueteSorpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("PaqueteSorpresa", request.paqueteSorpresaId()));

        OffsetDateTime ahora = OffsetDateTime.now(clock);
        if (paquete.getHoraLimiteRecogida().isAfter(ahora)) {
            throw new CierreAntesDeHoraLimiteException(paquete.getId());
        }

        AsignacionDonacion asignacion = evaluarOrganizaciones(paquete);

        if (asignacion.organizacionAceptada() == null) {
            paquete.setEstado(EstadoPaquete.desde(paquete.getEstado()).transitarA(EstadoPaquete.PERDIDO));
            paqueteSorpresaRepository.save(paquete);
            return new CierreResumen(
                    paquete.getId(), RESULTADO_PERDIDO, null, null, 0, 0, List.of());
        }

        OrganizacionComunitaria organizacion = asignacion.organizacionAceptada();
        int kilogramos = paquete.getCantidad();

        paquete.setEstado(EstadoPaquete.desde(paquete.getEstado()).transitarA(EstadoPaquete.DONADO));
        paqueteSorpresaRepository.save(paquete);

        donacionRepository.save(
                new Donacion(paquete, organizacion, ahora, ESTADO_DONACION_ACEPTADA));

        int acumuladoNegocio = calcularKilogramosDonadosNegocio(paquete.getNegocio().getId());

        return new CierreResumen(
                paquete.getId(),
                RESULTADO_DONADO,
                organizacion.getId(),
                organizacion.getNombre(),
                kilogramos,
                acumuladoNegocio,
                asignacion.organizacionesRechazadas());
    }

    /**
     * Recorre organizaciones elegibles (capacidad suficiente), ordenadas de menor a mayor capacidad.
     * La primera acepta; el resto queda registrado como rechazadas en el resumen (sin fila en BD:
     * solo una donación por paquete).
     */
    private AsignacionDonacion evaluarOrganizaciones(PaqueteSorpresa paquete) {
        List<OrganizacionComunitaria> elegibles = organizacionComunitariaRepository.findAll().stream()
                .filter(org -> org.getCapacidadRecoleccion() >= paquete.getCantidad())
                .sorted(Comparator.comparingInt(OrganizacionComunitaria::getCapacidadRecoleccion))
                .toList();
        if (elegibles.isEmpty()) {
            return new AsignacionDonacion(null, List.of());
        }
        OrganizacionComunitaria aceptada = elegibles.getFirst();
        List<Long> rechazadas = new ArrayList<>();
        for (int i = 1; i < elegibles.size(); i++) {
            rechazadas.add(elegibles.get(i).getId());
        }
        return new AsignacionDonacion(aceptada, List.copyOf(rechazadas));
    }

    private int calcularKilogramosDonadosNegocio(Long negocioId) {
        Specification<PaqueteSorpresa> spec = Specification.allOf(
                PaqueteSorpresaEspecificaciones.conEstado(ESTADO_DONADO),
                PaqueteSorpresaEspecificaciones.deNegocio(negocioId));
        return paqueteSorpresaRepository.findAll(spec).stream()
                .mapToInt(PaqueteSorpresa::getCantidad)
                .sum();
    }

    private record AsignacionDonacion(
            OrganizacionComunitaria organizacionAceptada,
            List<Long> organizacionesRechazadas) {
    }
}
