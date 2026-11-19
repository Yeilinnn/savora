package cr.ac.una.savora.data;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PerfilImpactoRepository extends MongoRepository<PerfilImpacto, String> {

    Optional<PerfilImpacto> findByPropietarioIdAndTipoPropietario(
            String propietarioId, TipoPropietario tipoPropietario);

    List<PerfilImpacto> findByTipoPropietarioAndPropietarioIdIn(
            TipoPropietario tipoPropietario, Collection<String> propietarioIds);
}