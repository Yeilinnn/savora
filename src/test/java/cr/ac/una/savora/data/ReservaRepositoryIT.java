package cr.ac.una.savora.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ReservaRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propiedadesDePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ReservaRepository reservaRepository;

    @Test
    void listaReservasPendientesConPaqueteYNegocio() {
        var reservas = reservaRepository.findPendientesConPaquete(1L);

        assertThat(reservas).hasSize(1);
        assertThat(reservas.get(0).getPaqueteSorpresa().getNegocio().getNombre())
                .isEqualTo("Restaurante El Mirador");
    }

    @Test
    void filtraReservasDelClientePorEstado() {
        var spec = ReservaEspecificaciones.delCliente(1L)
                .and(ReservaEspecificaciones.conEstado("recogido"));

        var reservas = reservaRepository.findAll(spec);

        assertThat(reservas).hasSize(1);
        assertThat(reservas.get(0).getEstado()).isEqualTo("recogido");
    }
}
