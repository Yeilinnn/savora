package cr.ac.una.savora.presentation.api.v1;

import com.jayway.jsonpath.JsonPath;
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

    private static String cuerpo(Object paqueteId, String numeroTarjeta) {
        return """
                {"paqueteSorpresaId":%s,
                 "tarjeta":{"numero":"%s","titular":"Cliente de Prueba","vencimiento":"12/30","cvv":"123"}}
                """.formatted(paqueteId, numeroTarjeta);
    }

    private String tokenCliente() throws Exception {
        String respuesta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente1\",\"password\":\"cliente123\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    @Test
    void postReservaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(5, "4242424242424242")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postReservaConPagoAprobadoDevuelve201ConComprobante() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(5, "4242 4242 4242 4242")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andExpect(jsonPath("$.pago.estado").value("aprobado"))
                .andExpect(jsonPath("$.pago.marca").value("Visa"))
                .andExpect(jsonPath("$.pago.ultimos4").value("4242"))
                .andExpect(jsonPath("$.pago.referencia").isNotEmpty());
    }

    @Test
    void postReservaConTarjetaRechazadaDevuelve422YNoReserva() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(5, "4000 0000 0000 0002")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Pago rechazado"));
    }

    @Test
    void postReservaSinTarjetaDevuelve400() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paqueteSorpresaId":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validación de formato"));
    }

    @Test
    void postReservaConCuerpoInvalidoDevuelve400() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("null", "4242424242424242")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validación de formato"))
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void postReservaPaqueteInexistenteDevuelve404() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(999, "4242424242424242")))
                .andExpect(status().isNotFound());
    }

    @Test
    void postReservaFueraDeHoraLimiteDevuelve422() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(post("/api/v1/reservas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(4, "4242424242424242")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
    }

    @Test
    void getReservaInexistenteDevuelve404() throws Exception {
        String token = tokenCliente();
        mockMvc.perform(get("/api/v1/reservas/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}