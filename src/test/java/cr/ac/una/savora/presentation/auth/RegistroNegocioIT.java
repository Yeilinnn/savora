package cr.ac.una.savora.presentation.auth;

import cr.ac.una.savora.data.Negocio;
import cr.ac.una.savora.data.NegocioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RegistroNegocioIT {

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
    @Autowired
    NegocioRepository negocioRepository;

    private static String cuerpo(String nombre, String username, String password) {
        return """
                {"nombre":"%s","tipoNegocio":"Panadería","ubicacion":"Santa Cruz","horarioCierre":"19:00",
                 "username":"%s","password":"%s"}
                """.formatted(nombre, username, password);
    }

    private static String login(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }

    @Test
    void negocioNuevoQuedaPendienteYSoloEntraTrasLaAprobacionDelAdmin() throws Exception {
        mockMvc.perform(post("/auth/registro-negocio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Panadería Prueba Uno", "prueba.uno", "clave-segura-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        // Pendiente: credenciales correctas pero sin acceso.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("prueba.uno", "clave-segura-1")))
                .andExpect(status().isForbidden());

        // Contraseña incorrecta: sigue siendo 401 (no se revela el estado).
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("prueba.uno", "otra-clave-xx")))
                .andExpect(status().isUnauthorized());

        Long negocioId = negocioRepository.findAll().stream()
                .filter(n -> "Panadería Prueba Uno".equals(n.getNombre()))
                .map(Negocio::getId)
                .findFirst()
                .orElseThrow();

        // El administrador lo aprueba desde el panel.
        mockMvc.perform(post("/admin/negocios/{id}/estado", negocioId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("estado", "aprobado"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("prueba.uno", "clave-segura-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("NEGOCIO"));

        // Si lo suspende, vuelve a quedar sin acceso.
        mockMvc.perform(post("/admin/negocios/{id}/estado", negocioId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("estado", "suspendido"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("prueba.uno", "clave-segura-1")))
                .andExpect(status().isForbidden());

        assertThat(negocioRepository.findById(negocioId).orElseThrow().getEstado()).isEqualTo("suspendido");
    }

    @Test
    void registroConUsuarioRepetidoDevuelve409() throws Exception {
        mockMvc.perform(post("/auth/registro-negocio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Negocio Repetido A", "repetido.neg", "clave-segura-2")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/registro-negocio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Negocio Repetido B", "repetido.neg", "clave-segura-2")))
                .andExpect(status().isConflict());
    }

    @Test
    void registroConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/registro-negocio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("", "Mayusculas", "corta")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elAdminSembradoPuedeIniciarSesionYElNegocioNo() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }
}