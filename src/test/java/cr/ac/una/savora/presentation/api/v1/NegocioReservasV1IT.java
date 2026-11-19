package cr.ac.una.savora.presentation.api.v1;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NegocioReservasV1IT {

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
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/negocio/reservas")).andExpect(status().isUnauthorized());
    }

    @Test
    void listarComoClienteDevuelve403() throws Exception {
        String token = tokenDe("cliente1", "cliente123");
        mockMvc.perform(get("/api/v1/negocio/reservas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarComoNegocioDevuelve200() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        mockMvc.perform(get("/api/v1/negocio/reservas")
                        .param("estado", "pendiente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void marcarRecogidaDeReservaInexistenteDevuelve404() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        mockMvc.perform(post("/api/v1/negocio/reservas/999999/recogida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcarRecogidaComoClienteDevuelve403() throws Exception {
        String token = tokenDe("cliente1", "cliente123");
        mockMvc.perform(post("/api/v1/negocio/reservas/1/recogida")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}