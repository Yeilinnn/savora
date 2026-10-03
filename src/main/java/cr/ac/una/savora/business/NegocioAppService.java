package cr.ac.una.savora.business;

import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.business.dto.CrearNegocioSolicitud;
import cr.ac.una.savora.business.dto.NegocioResumen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NegocioAppService {

    private final NegocioRepository negocioRepository;

    public NegocioAppService(NegocioRepository negocioRepository) {
        this.negocioRepository = negocioRepository;
    }

    @Transactional(readOnly = true)
    public Page<NegocioResumen> listar(Pageable pageable) {
        return negocioRepository.findAll(pageable).map(NegocioAppService::aResponse);
    }

    @Transactional(readOnly = true)
    public NegocioResumen obtenerPorId(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public NegocioResumen crear(CrearNegocioSolicitud request) {
        Negocio negocio = negocioRepository.save(new Negocio(
                request.nombre(), request.tipoNegocio(), request.ubicacion(), request.horarioCierre()));
        return aResponse(negocio);
    }

    private Negocio buscar(Long id) {
        return negocioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Negocio", id));
    }

    private static NegocioResumen aResponse(Negocio negocio) {
        return new NegocioResumen(
                negocio.getId(),
                negocio.getNombre(),
                negocio.getTipoNegocio(),
                negocio.getUbicacion(),
                negocio.getHorarioCierre());
    }
}
