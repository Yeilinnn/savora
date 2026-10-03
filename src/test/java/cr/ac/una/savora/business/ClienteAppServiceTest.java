package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearClienteSolicitud;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteAppServiceTest {

    @Mock
    ClienteRepository clienteRepository;

    @InjectMocks
    ClienteAppService service;

    @Test
    void obtenerPorIdDevuelveResumen() {
        Cliente cliente = new Cliente("Ana", "ana@test.com", "8888");
        ReflectionTestUtils.setField(cliente, "id", 1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThat(service.obtenerPorId(1L).nombre()).isEqualTo("Ana");
    }

    @Test
    void crearPersisteCliente() {
        when(clienteRepository.save(org.mockito.ArgumentMatchers.any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 10L);
            return c;
        });

        assertThat(service.crear(new CrearClienteSolicitud("Nuevo", "n@t.com", "1111")).id()).isEqualTo(10L);
    }
}
