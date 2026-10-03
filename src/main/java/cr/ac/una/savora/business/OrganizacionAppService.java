package cr.ac.una.savora.business;

import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.OrganizacionComunitaria;
import cr.ac.una.savora.data.OrganizacionComunitariaRepository;
import cr.ac.una.savora.business.dto.CrearOrganizacionSolicitud;
import cr.ac.una.savora.business.dto.OrganizacionResumen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizacionAppService {

    private final OrganizacionComunitariaRepository organizacionRepository;

    public OrganizacionAppService(OrganizacionComunitariaRepository organizacionRepository) {
        this.organizacionRepository = organizacionRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrganizacionResumen> listar(Pageable pageable) {
        return organizacionRepository.findAll(pageable).map(OrganizacionAppService::aResponse);
    }

    @Transactional(readOnly = true)
    public OrganizacionResumen obtenerPorId(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public OrganizacionResumen crear(CrearOrganizacionSolicitud request) {
        OrganizacionComunitaria org = organizacionRepository.save(new OrganizacionComunitaria(
                request.nombre(), request.tipo(), request.capacidadRecoleccion(), request.contacto()));
        return aResponse(org);
    }

    private OrganizacionComunitaria buscar(Long id) {
        return organizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("OrganizacionComunitaria", id));
    }

    private static OrganizacionResumen aResponse(OrganizacionComunitaria org) {
        return new OrganizacionResumen(
                org.getId(), org.getNombre(), org.getTipo(), org.getCapacidadRecoleccion(), org.getContacto());
    }
}
