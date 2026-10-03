package cr.ac.una.savora.business;

import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.Reserva;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaMapeadorTest {

    @Test
    void mapeaReservaAResumen() {
        Cliente cliente = new Cliente("Ana", "a@t.com", "1");
        var negocio = new cr.ac.una.savora.data.Negocio("N", "T", "U", LocalTime.NOON);
        var categoria = new cr.ac.una.savora.data.Categoria("C");
        PaqueteSorpresa paquete = new PaqueteSorpresa(
                negocio, categoria, "Pan", 2,
                new BigDecimal("100.00"), new BigDecimal("50.00"),
                OffsetDateTime.now(), "reservado");
        ReflectionTestUtils.setField(paquete, "id", 3L);
        Reserva reserva = new Reserva(cliente, paquete, OffsetDateTime.now(), "pendiente");
        ReflectionTestUtils.setField(reserva, "id", 7L);

        assertThat(ReservaMapeador.aResumen(reserva).id()).isEqualTo(7L);
        assertThat(ReservaMapeador.aResumen(reserva).paqueteSorpresaId()).isEqualTo(3L);
    }
}
