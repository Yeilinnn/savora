package cr.ac.una.savora.data;

import java.util.Optional;

public interface CategoriaRepository extends RepositorioBase<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);
}