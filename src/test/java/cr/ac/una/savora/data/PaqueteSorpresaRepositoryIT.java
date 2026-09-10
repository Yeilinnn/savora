package cr.ac.una.savora.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PaqueteSorpresaRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propiedadesDePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    PaqueteSorpresaRepository paqueteSorpresaRepository;

    @Test
    void listaPaquetesDisponiblesAntesDeLaHoraLimite() {
        OffsetDateTime ahora = OffsetDateTime.parse("2026-08-27T20:00:00-06:00");

        var paquetes = paqueteSorpresaRepository.findDisponibles(ahora);

        assertThat(paquetes).hasSize(1);
        assertThat(paquetes.get(0).getEstado()).isEqualTo("disponible");
    }

    @Test
    void traeCategoriaConJoinFetch() {
        var paquetes = paqueteSorpresaRepository.findByNegocioIdConCategoria(1L);

        assertThat(paquetes).isNotEmpty();
        assertThat(paquetes.get(0).getCategoria().getNombre()).isEqualTo("Panadería");
    }

    @Test
    void filtraPorEstadoYCategoriaConSpecification() {
        var spec = PaqueteSorpresaEspecificaciones.conEstado("recogido")
                .and(PaqueteSorpresaEspecificaciones.deCategoria(2L));

        var paquetes = paqueteSorpresaRepository.findAll(spec);

        assertThat(paquetes).hasSize(1);
        assertThat(paquetes.get(0).getDescripcion()).isEqualTo("Casado del día");
    }
}
