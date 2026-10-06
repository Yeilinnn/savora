package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearPaqueteSorpresaSolicitud;
import cr.ac.una.savora.business.dto.PaqueteSorpresaResumen;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.CategoriaRepository;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaEspecificaciones;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PaqueteSorpresaAppService {

    private static final String ESTADO_DISPONIBLE = "disponible";

    private final PaqueteSorpresaRepository paqueteSorpresaRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;

    public PaqueteSorpresaAppService(
            PaqueteSorpresaRepository paqueteSorpresaRepository,
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository) {
        this.paqueteSorpresaRepository = paqueteSorpresaRepository;
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public Page<PaqueteSorpresaResumen> listar(String estado, Long categoriaId, Long negocioId, Pageable pageable) {
        Specification<PaqueteSorpresa> spec = Specification.allOf(
                PaqueteSorpresaEspecificaciones.conEstado(estado),
                PaqueteSorpresaEspecificaciones.deCategoria(categoriaId),
                PaqueteSorpresaEspecificaciones.deNegocio(negocioId),
                PaqueteSorpresaEspecificaciones.conNegocioYCategoria());
        return paqueteSorpresaRepository.findAll(spec, pageable).map(PaqueteSorpresaAppService::aResumen);
    }

    @Transactional(readOnly = true)
    public PaqueteSorpresaResumen obtenerPorId(Long id) {
        return aResumen(buscar(id));
    }

    @Transactional(readOnly = true)
    public Long obtenerNegocioPropietario(Long id) {
        return buscar(id).getNegocio().getId();
    }

    /**
     * El negocioId NUNCA se toma del cuerpo de la solicitud: viene del claim negocioId del JWT
     * autenticado, para que un negocio no pueda publicar paquetes a nombre de otro
     * (mitigación de asignación masiva / BOLA — OWASP API1).
     */
    @Transactional
    public PaqueteSorpresaResumen crear(@Valid CrearPaqueteSorpresaSolicitud solicitud, Long negocioIdAutenticado) {
        Negocio negocio = negocioRepository.findById(negocioIdAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Negocio", negocioIdAutenticado));
        Categoria categoria = categoriaRepository.findById(solicitud.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria", solicitud.categoriaId()));

        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio,
                categoria,
                solicitud.descripcion(),
                solicitud.cantidad(),
                solicitud.precioOriginal(),
                solicitud.precioConDescuento(),
                solicitud.horaLimiteRecogida(),
                ESTADO_DISPONIBLE);
        return aResumen(paqueteSorpresaRepository.save(paquete));
    }

    private PaqueteSorpresa buscar(Long id) {
        return paqueteSorpresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("PaqueteSorpresa", id));
    }

    private static PaqueteSorpresaResumen aResumen(PaqueteSorpresa paquete) {
        return new PaqueteSorpresaResumen(
                paquete.getId(),
                paquete.getNegocio().getId(),
                paquete.getNegocio().getNombre(),
                paquete.getCategoria().getId(),
                paquete.getCategoria().getNombre(),
                paquete.getDescripcion(),
                paquete.getCantidad(),
                paquete.getPrecioOriginal(),
                paquete.getPrecioConDescuento(),
                paquete.getHoraLimiteRecogida(),
                paquete.getEstado());
    }
}