package cr.ac.una.savora.data;

import java.util.List;

public interface DonacionRepository extends RepositorioBase<Donacion, Long> {

    List<Donacion> findByOrganizacionComunitariaId(Long organizacionComunitariaId);
}
