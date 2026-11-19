package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearPaqueteSorpresaSolicitud;
import cr.ac.una.savora.business.dto.PaqueteSorpresaResumen;
import cr.ac.una.savora.business.dto.ResenaPromedio;
import cr.ac.una.savora.business.excepcion.CuentaNoHabilitadaException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class PaqueteSorpresaAppService {

    private static final String ESTADO_DISPONIBLE = "disponible";

    private final PaqueteSorpresaRepository paqueteSorpresaRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PerfilImpactoService perfilImpactoService;
    private final ResenaService resenaService;

    public PaqueteSorpresaAppService(
            PaqueteSorpresaRepository paqueteSorpresaRepository,
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            PerfilImpactoService perfilImpactoService,
            ResenaService resenaService) {
        this.paqueteSorpresaRepository = paqueteSorpresaRepository;
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.perfilImpactoService = perfilImpactoService;
        this.resenaService = resenaService;
    }

    @Transactional(readOnly = true)
    public Page<PaqueteSorpresaResumen> listar(String estado, Long categoriaId, Long negocioId, Pageable pageable) {
        return listar(estado, categoriaId, negocioId, false, pageable);
    }

    /**
     * Con destacadosPrimero, el catálogo se ordena así: primero los paquetes de negocios con buen
     * historial de donación (PerfilImpacto en MongoDB), luego por mejor promedio de reseñas, y
     * al final por el orden pedido. El orden se calcula sobre todos los resultados y después se
     * pagina, para que el posicionamiento valga en todas las páginas y no solo dentro de una.
     */
    @Transactional(readOnly = true)
    public Page<PaqueteSorpresaResumen> listar(
            String estado, Long categoriaId, Long negocioId, boolean destacadosPrimero, Pageable pageable) {
        Specification<PaqueteSorpresa> spec = Specification.allOf(
                PaqueteSorpresaEspecificaciones.conEstado(estado),
                PaqueteSorpresaEspecificaciones.deCategoria(categoriaId),
                PaqueteSorpresaEspecificaciones.deNegocio(negocioId),
                PaqueteSorpresaEspecificaciones.conNegocioYCategoria());

        if (!destacadosPrimero) {
            Page<PaqueteSorpresa> pagina = paqueteSorpresaRepository.findAll(spec, pageable);
            return new PageImpl<>(enriquecer(pagina.getContent()), pageable, pagina.getTotalElements());
        }

        Sort orden = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("horaLimiteRecogida");
        List<PaqueteSorpresaResumen> todos = new ArrayList<>(enriquecer(paqueteSorpresaRepository.findAll(spec, orden)));
        // List.sort es estable: lo que empata conserva el orden pedido (p. ej. por hora límite).
        todos.sort(Comparator
                .comparing(PaqueteSorpresaResumen::negocioDestacado, Comparator.reverseOrder())
                .thenComparingDouble(r -> -(r.promedioResenas() == null ? 0.0 : r.promedioResenas())));
        int desde = (int) Math.min(pageable.getOffset(), todos.size());
        int hasta = Math.min(desde + pageable.getPageSize(), todos.size());
        return new PageImpl<>(todos.subList(desde, hasta), pageable, todos.size());
    }

    @Transactional(readOnly = true)
    public PaqueteSorpresaResumen obtenerPorId(Long id) {
        return enriquecer(List.of(buscar(id))).getFirst();
    }

    private List<PaqueteSorpresaResumen> enriquecer(List<PaqueteSorpresa> paquetes) {
        List<Long> negocioIds = paquetes.stream().map(p -> p.getNegocio().getId()).distinct().toList();
        Set<Long> destacados = perfilImpactoService.negociosDestacados(negocioIds);
        Map<Long, ResenaPromedio> promedios = resenaService.promediosPorNegocio(negocioIds);
        return paquetes.stream()
                .map(p -> aResumen(
                        p, destacados.contains(p.getNegocio().getId()), promedios.get(p.getNegocio().getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Long obtenerNegocioPropietario(Long id) {
        return buscar(id).getNegocio().getId();
    }

    /**
     * El negocioId NUNCA se toma del cuerpo de la solicitud: viene del claim negocioId del JWT
     * autenticado, para que un negocio no pueda publicar paquetes a nombre de otro
     * (mitigación de asignación masiva / BOLA — OWASP API1). Además, el negocio debe estar
     * aprobado por un administrador.
     */
    @Transactional
    public PaqueteSorpresaResumen crear(@Valid CrearPaqueteSorpresaSolicitud solicitud, Long negocioIdAutenticado) {
        Negocio negocio = negocioRepository.findById(negocioIdAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Negocio", negocioIdAutenticado));
        if (!negocio.estaAprobado()) {
            throw new CuentaNoHabilitadaException("Su negocio no está habilitado para publicar paquetes");
        }
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
        return aResumen(paqueteSorpresaRepository.save(paquete), false, null);
    }

    private PaqueteSorpresa buscar(Long id) {
        return paqueteSorpresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("PaqueteSorpresa", id));
    }

    private static PaqueteSorpresaResumen aResumen(
            PaqueteSorpresa paquete, boolean negocioDestacado, ResenaPromedio promedio) {
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
                paquete.getEstado(),
                negocioDestacado,
                promedio == null ? null : promedio.promedio(),
                promedio == null ? 0 : promedio.total());
    }
}