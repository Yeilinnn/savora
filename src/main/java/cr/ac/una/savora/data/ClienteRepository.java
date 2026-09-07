package cr.ac.una.savora.data;

import java.util.Optional;

public interface ClienteRepository extends RepositorioBase<Cliente, Long> {

    Optional<Cliente> findByCorreo(String correo);
}