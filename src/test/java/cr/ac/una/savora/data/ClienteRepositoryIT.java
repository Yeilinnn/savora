package cr.ac.una.savora.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class ClienteRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propiedadesDePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ClienteRepository clienteRepository;

    @Test
    void guardaYRecuperaUnCliente() {
        var cliente = clienteRepository.save(
                new Cliente("Cliente IT", "cliente.it." + System.nanoTime() + "@mail.com", "8888-0000"));

        assertThat(clienteRepository.findById(cliente.getId())).isPresent();
    }

    @Test
    void rechazaCorreoDuplicado() {
        String correo = "duplicado." + System.nanoTime() + "@mail.com";
        clienteRepository.save(new Cliente("Primero", correo, "8888-1111"));

        assertThatThrownBy(() -> clienteRepository.saveAndFlush(
                new Cliente("Segundo", correo, "8888-2222")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}