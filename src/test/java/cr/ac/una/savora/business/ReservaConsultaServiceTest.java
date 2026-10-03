package cr.ac.una.savora.business;

import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaConsultaServiceTest {

    @Mock
    ReservaRepository reservaRepository;

    @InjectMocks
    ReservaConsultaService service;

    @Test
    void listarMapeaReservas() {
        Cliente cliente = new Cliente("Ana", "a@t.com", "1");
        ReflectionTestUtils.setField(cliente, "id", 1L);
        var negocio = new cr.ac.una.savora.data.Negocio("N", "T", "U", LocalTime.NOON);
        var categoria = new cr.ac.una.savora.data.Categoria("C");
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Desc", 1,
                BigDecimal.TEN, BigDecimal.ONE,
                OffsetDateTime.now(), "reservado");
        ReflectionTestUtils.setField(paquete, "id", 2L);
        Reserva reserva = new Reserva(cliente, paquete, OffsetDateTime.now(), "pendiente");
        ReflectionTestUtils.setField(reserva, "id", 5L);

        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reserva)));

        assertThat(service.listar(1L, "pendiente", Pageable.unpaged()).getTotalElements()).isEqualTo(1);
    }
}
