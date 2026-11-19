package cr.ac.una.savora.data;

import java.util.Optional;

public interface UsuarioRepository extends RepositorioBase<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByClienteId(Long clienteId);

    Optional<Usuario> findByNegocioId(Long negocioId);
}