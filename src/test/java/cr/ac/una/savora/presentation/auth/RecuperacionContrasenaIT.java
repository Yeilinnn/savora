package cr.ac.una.savora.presentation.auth;

import com.jayway.jsonpath.JsonPath;
import cr.ac.una.savora.security.EnviadorCorreo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RecuperacionContrasenaIT {

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

    @MockitoBean
    EnviadorCorreo enviadorCorreo;

    private void registrar(String correo, String username, String password) throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Persona Recupera","correo":"%s","telefono":"88887777","username":"%s","password":"%s"}
                                """.formatted(correo, username, password)))
                .andExpect(status().isCreated());
    }

    private int login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void flujoCompletoOlvideRestablecerYIniciarSesionConLaClaveNueva() throws Exception {
        registrar("recupera1@test.com", "recupera.uno", "clave-vieja-1");

        mockMvc.perform(post("/auth/olvide-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"recupera1@test.com\"}"))
                .andExpect(status().isAccepted());

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreo).enviar(eq("recupera1@test.com"), anyString(), cuerpo.capture());
        String texto = cuerpo.getValue();
        String token = texto.substring(texto.indexOf("token=") + 6).lines().findFirst().orElseThrow();

        mockMvc.perform(post("/auth/restablecer-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"password\":\"clave-nueva-1\"}".formatted(token)))
                .andExpect(status().isOk());

        assertThat(login("recupera.uno", "clave-nueva-1")).isEqualTo(200);
        assertThat(login("recupera.uno", "clave-vieja-1")).isEqualTo(401);

        // El enlace es de un solo uso.
        mockMvc.perform(post("/auth/restablecer-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"password\":\"otra-clave-22\"}".formatted(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void olvideConCorreoDesconocidoResponde202SinEnviarNada() throws Exception {
        mockMvc.perform(post("/auth/olvide-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"no-existe@test.com\"}"))
                .andExpect(status().isAccepted());
        verify(enviadorCorreo, never()).enviar(eq("no-existe@test.com"), anyString(), anyString());
    }

    @Test
    void restablecerConTokenFalsoDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/restablecer-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"falso\",\"password\":\"clave-nueva-1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarContrasenaExigeSesionYClaveActualCorrecta() throws Exception {
        mockMvc.perform(post("/api/v1/cuenta/cambiar-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"x\",\"nueva\":\"clave-nueva-1\"}"))
                .andExpect(status().isUnauthorized());

        registrar("recupera2@test.com", "recupera.dos", "clave-vieja-2");
        String respuesta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"recupera.dos\",\"password\":\"clave-vieja-2\"}"))
                .andReturn().getResponse().getContentAsString();
        String jwt = "Bearer " + JsonPath.read(respuesta, "$.token");

        mockMvc.perform(post("/api/v1/cuenta/cambiar-contrasena")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"incorrecta\",\"nueva\":\"clave-nueva-2\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/cuenta/cambiar-contrasena")
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actual\":\"clave-vieja-2\",\"nueva\":\"clave-nueva-2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada"));

        assertThat(login("recupera.dos", "clave-nueva-2")).isEqualTo(200);
    }
}