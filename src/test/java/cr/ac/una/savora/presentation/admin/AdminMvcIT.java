package cr.ac.una.savora.presentation.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AdminMvcIT {

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

    @Test
    void loginMuestraFormulario() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    void adminRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/admin/categorias")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin/negocios")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin/usuarios")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminAccedeAlListadoDeCategorias() throws Exception {
        mockMvc.perform(get("/admin/categorias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categorias/lista"));
    }

    @Test
    void adminAccedeAlListadoDeNegociosYUsuarios() throws Exception {
        mockMvc.perform(get("/admin/negocios").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/negocios/lista"));
        mockMvc.perform(get("/admin/usuarios").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/usuarios/lista"));
    }

    @Test
    void negocioYClienteNoPuedenEntrarAlPanel() throws Exception {
        mockMvc.perform(get("/admin/negocios").with(user("negocio1").roles("NEGOCIO")))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(200)
                        .isGreaterThanOrEqualTo(300)
                        .isLessThan(500));
        mockMvc.perform(get("/admin/usuarios").with(user("cliente1").roles("CLIENTE")))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(200)
                        .isGreaterThanOrEqualTo(300)
                        .isLessThan(500));
    }

    @Test
    void crearCategoriaInvalidaMuestraErrores() throws Exception {
        mockMvc.perform(post("/admin/categorias")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nombre", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categorias/formulario"));
    }
}