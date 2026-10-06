package cr.ac.una.savora.presentation.api.v1;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CierreV1IT {

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
    static class RelojCierreV1Config {
        @Bean
        @Primary
        Clock relojCierreV1() {
            return Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc mockMvc;

    private String tokenDe(String username, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void cerrarPaqueteDeOtroNegocioDevuelve403() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        // paquete 3 pertenece al negocio 2 (seed V4)
        mockMvc.perform(post("/api/v1/paquetes/3/cierre")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void cerrarPaqueteYaRecogidoDevuelve422() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        // paquete 1 es del negocio 1, pero ya está "recogido": esa transición no es válida
        mockMvc.perform(post("/api/v1/paquetes/1/cierre")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
    }

    @Test
    void cerrarPaquetePropioVencidoDevuelve200ConDonacion() throws Exception {
        String token = tokenDe("negocio1", "negocio123");

        String respuestaCreacion = mockMvc.perform(post("/api/v1/paquetes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":1,"descripcion":"Pan vencido para cierre","cantidad":10,
                                 "precioOriginal":5000,"precioConDescuento":2000,
                                 "horaLimiteRecogida":"2020-01-01T10:00:00-06:00"}
                                """))
                .andReturn().getResponse().getContentAsString();
        Number nuevoId = JsonPath.read(respuestaCreacion, "$.id");

        mockMvc.perform(post("/api/v1/paquetes/" + nuevoId.longValue() + "/cierre")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("DONADO"))
                .andExpect(jsonPath("$.organizacionComunitariaId").value(1))
                .andExpect(jsonPath("$.kilogramosDonados").value(10));
    }
}