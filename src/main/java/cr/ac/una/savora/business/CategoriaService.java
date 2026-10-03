package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CategoriaResumen;
import cr.ac.una.savora.business.dto.CrearCategoriaSolicitud;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.data.Categoria;
import cr.ac.una.savora.data.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<String> obtenerCategorias() {
        return categoriaRepository.findAll().stream()
                .map(Categoria::getNombre)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResumen> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(c -> new CategoriaResumen(c.getId(), c.getNombre()));
    }

    @Transactional(readOnly = true)
    public CategoriaResumen obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(c -> new CategoriaResumen(c.getId(), c.getNombre()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria", id));
    }

    @Transactional
    public CategoriaResumen crear(CrearCategoriaSolicitud solicitud) {
        Categoria categoria = categoriaRepository.save(new Categoria(solicitud.nombre()));
        return new CategoriaResumen(categoria.getId(), categoria.getNombre());
    }
}