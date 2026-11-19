package cr.ac.una.savora.data;

import java.util.List;
import java.util.Optional;

public interface RestablecimientoContrasenaRepository extends RepositorioBase<RestablecimientoContrasena, Long> {

    Optional<RestablecimientoContrasena> findByTokenHash(String tokenHash);

    List<RestablecimientoContrasena> findByUsuarioIdAndUsadoEnIsNull(Long usuarioId);
}