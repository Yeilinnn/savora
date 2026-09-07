package cr.ac.una.savora.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaqueteSorpresaRepository extends RepositorioBase<PaqueteSorpresa, Long> {

    // ANTES: negocio y categoria quedan LAZY -> N+1 al navegarlos en un ciclo.
    List<PaqueteSorpresa> findByEstado(String estado);

    // DESPUÉS: JOIN FETCH a las dos relaciones. De 1+2N consultas a 1.
    @Query("""
            SELECT p FROM PaqueteSorpresa p
            JOIN FETCH p.negocio
            JOIN FETCH p.categoria
            WHERE p.estado = :estado
            """)
    List<PaqueteSorpresa> catalogoPorEstado(@Param("estado") String estado);
}