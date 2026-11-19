package cr.ac.una.savora.presentation.auth;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RegistroIT {

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

    private static String cuerpo(String correo, String username, String password) {
        return """
                {"nombre":"Persona de Prueba","correo":"%s","telefono":"88887777","username":"%s","password":"%s"}
                """.formatted(correo, username, password);
    }

    @Test
    void registroValidoDevuelve201TokenDeClienteYPermiteIniciarSesion() throws Exception {
        String respuesta = mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("nuevo1@test.com", "nuevo.uno", "clave-segura-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(respuesta, "$.token");

        // El token del registro ya sirve para consumir la API como cliente.
        mockMvc.perform(get("/api/v1/reservas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Y las credenciales funcionan en el login normal.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo.uno\",\"password\":\"clave-segura-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void registroConUsuarioOCorreoRepetidoDevuelve409() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("repetido@test.com", "repetido", "clave-segura-2")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("otro@test.com", "repetido", "clave-segura-2")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("repetido@test.com", "otro.usuario", "clave-segura-2")))
                .andExpect(status().isConflict());
    }

    @Test
    void registroConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("no-es-correo", "Mayusculas", "corta")))
                .andExpect(status().isBadRequest());
    }
}