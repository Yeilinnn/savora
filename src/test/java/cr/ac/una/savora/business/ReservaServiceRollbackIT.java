package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.CrearReservaRequest;
import cr.ac.una.savora.data.Cliente;
import cr.ac.una.savora.data.ClienteRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import cr.ac.una.savora.data.Reserva;
import cr.ac.una.savora.data.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class ReservaServiceRollbackIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propiedadesDePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @TestConfiguration
    static class RelojDePruebaConfig {
        @Bean
        @Primary
        Clock relojDePrueba() {
            // 2026-08-27 08:00 hora de Costa Rica: antes de la hora límite del paquete 5 (21:30).
            return Clock.fixed(Instant.parse("2026-08-27T14:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    ReservaService reservaService;
    @Autowired
    ReservaRepository reservaRepository;
    @Autowired
    PaqueteSorpresaRepository paqueteSorpresaRepository;
    @Autowired
    ClienteRepository clienteRepository;

    @Test
    void siLaReservaFallaAMitadDeCaminoNadaQuedaEscrito() {
        // Paquete 5 (seed): 'Caja de vegetales frescos', estado 'disponible', sin reserva previa.
        // Simulamos una condición de carrera: otro cliente ya reservó este mismo paquete
        // justo antes (viola la restricción UNIQUE de reserva.paquete_sorpresa_id).
        Cliente clienteQueYaReservo = clienteRepository.findById(2L).orElseThrow();
        PaqueteSorpresa paquete = paqueteSorpresaRepository.findById(5L).orElseThrow();
        reservaRepository.saveAndFlush(
                new Reserva(clienteQueYaReservo, paquete, OffsetDateTime.now(), "pendiente"));

        long reservasAntes = reservaRepository.count();

        assertThatThrownBy(() -> reservaService.reservar(new CrearReservaRequest(3L, 5L)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // El paquete debe seguir "disponible": el cambio de estado que sí se alcanzó a
        // escribir en la transacción se deshizo con el rollback.
        PaqueteSorpresa paqueteDespues = paqueteSorpresaRepository.findById(5L).orElseThrow();
        assertThat(paqueteDespues.getEstado()).isEqualTo("disponible");

        // No se agregó ninguna reserva nueva (solo sigue la que insertamos manualmente antes).
        assertThat(reservaRepository.count()).isEqualTo(reservasAntes);
    }
}