package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearNegocioSolicitud;
import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NegocioAppServiceTest {

    @Mock
    NegocioRepository negocioRepository;

    @InjectMocks
    NegocioAppService service;

    @Test
    void obtenerPorIdDevuelveResumen() {
        Negocio negocio = new Negocio("Pan", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        when(negocioRepository.findById(1L)).thenReturn(Optional.of(negocio));

        assertThat(service.obtenerPorId(1L).nombre()).isEqualTo("Pan");
    }

    @Test
    void actualizarModificaNegocio() {
        Negocio negocio = new Negocio("Pan", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        when(negocioRepository.findById(1L)).thenReturn(Optional.of(negocio));
        when(negocioRepository.save(negocio)).thenReturn(negocio);

        assertThat(service.actualizar(
                        1L,
                        new CrearNegocioSolicitud("Pan Nuevo", "Panadería", "Nicoya", LocalTime.of(20, 0)))
                .nombre())
                .isEqualTo("Pan Nuevo");
    }

    @Test
    void eliminarBorraNegocio() {
        Negocio negocio = new Negocio("Pan", "Panadería", "Liberia", LocalTime.of(19, 0));
        ReflectionTestUtils.setField(negocio, "id", 1L);
        when(negocioRepository.findById(1L)).thenReturn(Optional.of(negocio));

        service.eliminar(1L);

        verify(negocioRepository).delete(negocio);
    }
}
