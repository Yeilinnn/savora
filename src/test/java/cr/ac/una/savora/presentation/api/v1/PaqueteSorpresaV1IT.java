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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaqueteSorpresaV1IT {

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
    void crearPaqueteSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/paquetes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":1,"descripcion":"Prueba","cantidad":5,
                                 "precioOriginal":5000,"precioConDescuento":2000,
                                 "horaLimiteRecogida":"2026-12-01T20:00:00-06:00"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearPaqueteConRolClienteDevuelve403() throws Exception {
        String token = tokenDe("cliente1", "cliente123");
        mockMvc.perform(post("/api/v1/paquetes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":1,"descripcion":"Prueba","cantidad":5,
                                 "precioOriginal":5000,"precioConDescuento":2000,
                                 "horaLimiteRecogida":"2026-12-01T20:00:00-06:00"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearPaqueteConRolNegocioDevuelve201YUsaNegocioDelToken() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        mockMvc.perform(post("/api/v1/paquetes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":1,"descripcion":"Pan recién horneado","cantidad":5,
                                 "precioOriginal":5000,"precioConDescuento":2000,
                                 "horaLimiteRecogida":"2026-12-01T20:00:00-06:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.negocioId").value(1))
                .andExpect(jsonPath("$.estado").value("disponible"));
    }

    @Test
    void crearPaqueteConCuerpoInvalidoDevuelve400() throws Exception {
        String token = tokenDe("negocio1", "negocio123");
        mockMvc.perform(post("/api/v1/paquetes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":null,"descripcion":"","cantidad":5,
                                 "precioOriginal":5000,"precioConDescuento":2000,
                                 "horaLimiteRecogida":"2026-12-01T20:00:00-06:00"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPaqueteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/paquetes/99999"))
                .andExpect(status().isNotFound());
    }
}