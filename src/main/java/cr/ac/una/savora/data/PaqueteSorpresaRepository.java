package cr.ac.una.savora.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface PaqueteSorpresaRepository extends RepositorioBase<PaqueteSorpresa, Long> {

    List<PaqueteSorpresa> findByNegocioId(Long negocioId);

    @Query("""
            SELECT p FROM PaqueteSorpresa p
            WHERE p.estado = 'disponible'
              AND p.horaLimiteRecogida > :ahora
            """)
    List<PaqueteSorpresa> findDisponibles(@Param("ahora") OffsetDateTime ahora);

    @Query("""
            SELECT p FROM PaqueteSorpresa p
            JOIN FETCH p.categoria
            WHERE p.negocio.id = :negocioId
            """)
    List<PaqueteSorpresa> findByNegocioIdConCategoria(@Param("negocioId") Long negocioId);
}
