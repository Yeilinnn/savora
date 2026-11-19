package cr.ac.una.savora.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface ResenaRepository extends MongoRepository<Resena, String> {

    boolean existsByReservaId(Long reservaId);

    Page<Resena> findByNegocioId(Long negocioId, Pageable pageable);

    List<Resena> findByNegocioIdIn(Collection<Long> negocioIds);

    List<Resena> findByClienteId(Long clienteId);
}