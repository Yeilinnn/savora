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
import org.testcontainers.containers.MongoDBContainer;
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

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.mongodb.uri", () -> mongo.getReplicaSetUrl("savora"));
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
    void siLaReservaFallaDespuesDeActualizarElPaqueteElRollbackLoDeshace() {
        // Paquete 5 (seed): disponible, sin reserva previa.
        // ReservaService hace saveAndFlush del paquete antes del INSERT de reserva; forzamos
        // fallo en el INSERT (UNIQUE paquete_sorpresa_id) para comprobar que el UPDATE previo
        // se revierte gracias a @Transactional.
        Cliente clienteQueYaReservo = clienteRepository.findById(2L).orElseThrow();
        PaqueteSorpresa paquete = paqueteSorpresaRepository.findById(5L).orElseThrow();
        reservaRepository.saveAndFlush(
                new Reserva(clienteQueYaReservo, paquete, OffsetDateTime.now(), "pendiente"));

        long reservasAntes = reservaRepository.count();
        int cantidadAntes = paquete.getCantidad();

        assertThatThrownBy(() -> reservaService.reservar(new CrearReservaRequest(3L, 5L)))
                .isInstanceOf(DataIntegrityViolationException.class);

        PaqueteSorpresa paqueteDespues = paqueteSorpresaRepository.findById(5L).orElseThrow();
        assertThat(paqueteDespues.getEstado()).isEqualTo("disponible");
        assertThat(paqueteDespues.getCantidad()).isEqualTo(cantidadAntes);

        assertThat(reservaRepository.count()).isEqualTo(reservasAntes);
    }
}