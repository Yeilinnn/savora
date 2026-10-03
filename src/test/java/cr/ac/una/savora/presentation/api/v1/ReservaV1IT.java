package cr.ac.una.savora.presentation.api.v1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReservaV1IT {

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
    static class RelojReservaV1Config {
        @Bean
        @Primary
        Clock relojReservaV1() {
            return Clock.fixed(Instant.parse("2026-08-27T14:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void postReservaDevuelve201ConLocation() throws Exception {
        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"paqueteSorpresaId":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("pendiente"));
    }

    @Test
    void postReservaConCuerpoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":null,"paqueteSorpresaId":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validación de formato"));
    }

    @Test
    void postReservaPaqueteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"paqueteSorpresaId":999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void postReservaFueraDeHoraLimiteDevuelve422() throws Exception {
        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"paqueteSorpresaId":4}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
    }

    @Test
    void getReservaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/reservas/99999"))
                .andExpect(status().isNotFound());
    }
}
