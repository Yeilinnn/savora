package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearOrganizacionSolicitud;
import cr.ac.una.savora.data.OrganizacionComunitaria;
import cr.ac.una.savora.data.OrganizacionComunitariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizacionAppServiceTest {

    @Mock
    OrganizacionComunitariaRepository organizacionRepository;

    @InjectMocks
    OrganizacionAppService service;

    @Test
    void obtenerPorIdDevuelveResumen() {
        OrganizacionComunitaria org = new OrganizacionComunitaria("Comedor", "ONG", 20, "2666");
        ReflectionTestUtils.setField(org, "id", 1L);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(org));

        assertThat(service.obtenerPorId(1L).capacidadRecoleccion()).isEqualTo(20);
    }

    @Test
    void actualizarModificaOrganizacion() {
        OrganizacionComunitaria org = new OrganizacionComunitaria("Comedor", "ONG", 20, "2666");
        ReflectionTestUtils.setField(org, "id", 1L);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organizacionRepository.save(org)).thenReturn(org);

        assertThat(service.actualizar(
                        1L, new CrearOrganizacionSolicitud("Comedor Esperanza", "ONG", 25, "2666-0001"))
                .capacidadRecoleccion())
                .isEqualTo(25);
    }

    @Test
    void eliminarBorraOrganizacion() {
        OrganizacionComunitaria org = new OrganizacionComunitaria("Comedor", "ONG", 20, "2666");
        ReflectionTestUtils.setField(org, "id", 1L);
        when(organizacionRepository.findById(1L)).thenReturn(Optional.of(org));

        service.eliminar(1L);

        verify(organizacionRepository).delete(org);
    }
}
