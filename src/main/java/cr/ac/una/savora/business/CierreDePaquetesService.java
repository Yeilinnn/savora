package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CerrarPaqueteRequest;
import cr.ac.una.savora.business.dto.CierreResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Donacion;
import cr.ac.una.savora.data.DonacionRepository;
import cr.ac.una.savora.data.OrganizacionComunitaria;
import cr.ac.una.savora.data.OrganizacionComunitariaRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class CierreDePaquetesService {

    private static final String RESULTADO_DONADO = "DONADO";
    private static final String RESULTADO_PERDIDO = "PERDIDO";
    private static final String ESTADO_DONACION_ACEPTADA = "aceptada";

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

    @Transactional
    public CierreResumen cerrar(@Valid CerrarPaqueteRequest request) {
        PaqueteSorpresa paquete = paqueteSorpresaRepository.findById(request.paqueteSorpresaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("PaqueteSorpresa", request.paqueteSorpresaId()));

        Optional<OrganizacionComunitaria> organizacionQueAcepta = buscarMejorOrganizacion(paquete);

        if (organizacionQueAcepta.isEmpty()) {
            // Patrón State: si el paquete ya no está DISPONIBLE (p. ej. alguien lo reservó
            // justo antes de este cierre automático), la transición falla aquí y se aborta.
            paquete.setEstado(EstadoPaquete.desde(paquete.getEstado()).transitarA(EstadoPaquete.PERDIDO));
            paqueteSorpresaRepository.save(paquete);
            return new CierreResumen(paquete.getId(), RESULTADO_PERDIDO, null, null);
        }

        OrganizacionComunitaria organizacion = organizacionQueAcepta.get();

        paquete.setEstado(EstadoPaquete.desde(paquete.getEstado()).transitarA(EstadoPaquete.DONADO));
        paqueteSorpresaRepository.save(paquete);

        donacionRepository.save(
                new Donacion(paquete, organizacion, OffsetDateTime.now(clock), ESTADO_DONACION_ACEPTADA));

        return new CierreResumen(paquete.getId(), RESULTADO_DONADO, organizacion.getId(), organizacion.getNombre());
    }

    /**
     * Regla: se ofrece el paquete a la organización con la MENOR capacidad de recolección
     * que aún así alcance (capacidadRecoleccion >= cantidad del paquete) — así no se gasta
     * la capacidad de una organización grande en un paquete que una chica ya puede recibir.
     */
    private Optional<OrganizacionComunitaria> buscarMejorOrganizacion(PaqueteSorpresa paquete) {
        List<OrganizacionComunitaria> organizaciones = organizacionComunitariaRepository.findAll();
        return organizaciones.stream()
                .filter(org -> org.getCapacidadRecoleccion() >= paquete.getCantidad())
                .min(Comparator.comparingInt(OrganizacionComunitaria::getCapacidadRecoleccion));
    }
}