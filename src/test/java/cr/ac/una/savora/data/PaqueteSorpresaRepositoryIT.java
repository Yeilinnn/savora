package cr.ac.una.savora.data;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PaqueteSorpresaRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propiedadesDePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    NegocioRepository negocioRepository;
    @Autowired
    CategoriaRepository categoriaRepository;
    @Autowired
    PaqueteSorpresaRepository paqueteSorpresaRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @PersistenceContext
    EntityManager entityManager;

    @Test
    void guardaYRecuperaUnPaquete() {
        var negocio = negocioRepository.save(
                new Negocio("Panadería IT", "Panadería", "San José", LocalTime.of(19, 0)));
        var categoria = categoriaRepository.save(new Categoria("Categoría IT " + System.nanoTime()));

        var paquete = paqueteSorpresaRepository.save(new PaqueteSorpresa(
                negocio, categoria, "Pan del día", 5,
                new BigDecimal("6000.00"), new BigDecimal("2500.00"),
                Instant.now().plusSeconds(3600), "disponible"));

        assertThat(paqueteSorpresaRepository.findById(paquete.getId())).isPresent();
    }

    @Test
    void rechazaPrecioConDescuentoMayorOIgualAlOriginal() {
        var negocio = negocioRepository.save(
                new Negocio("Soda IT", "Soda", "Heredia", LocalTime.of(20, 0)));
        var categoria = categoriaRepository.save(new Categoria("Categoría IT " + System.nanoTime()));

        assertThatThrownBy(() -> paqueteSorpresaRepository.saveAndFlush(new PaqueteSorpresa(
                negocio, categoria, "Precio inválido", 2,
                new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                Instant.now().plusSeconds(3600), "disponible")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Evidencia del N+1 (antes): 3 paquetes con negocio y categoría distintos
    // cada uno. entityManager.clear() es la parte clave: sin ella, los objetos
    // que acabamos de construir en memoria siguen gestionados en la misma
    // sesión, y Hibernate los reutiliza por identidad al navegar
    // paquete.getNegocio() sin ir a la base — nunca hubo un proxy perezoso
    // real de por medio. Limpiar la caché obliga a que findAllById cargue
    // paquetes "frescos", con negocio y categoria como proxies perezosos
    // genuinos, igual que en una petición HTTP nueva de la app real.
    @Test
    @Transactional
    void sinJoinFetchCadaPaqueteDisparaDosConsultasAdicionales() {
        List<Long> idsDePaquetes = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            var negocio = negocioRepository.save(
                    new Negocio("Negocio N+1 #" + i, "Restaurante", "Test", LocalTime.of(21, 0)));
            var categoria = categoriaRepository.save(new Categoria("Categoría N+1 #" + i + " " + System.nanoTime()));
            var paquete = paqueteSorpresaRepository.save(new PaqueteSorpresa(
                    negocio, categoria, "Paquete N+1 #" + i, 1,
                    new BigDecimal("1000.00"), new BigDecimal("500.00"),
                    Instant.now().plusSeconds(3600), "disponible"));
            idsDePaquetes.add(paquete.getId());
        }
        entityManager.flush();
        entityManager.clear();

        Statistics estadisticas = estadisticasDeHibernate();
        estadisticas.clear();

        List<PaqueteSorpresa> paquetes = paqueteSorpresaRepository.findAllById(idsDePaquetes);
        for (PaqueteSorpresa p : paquetes) {
            p.getNegocio().getNombre();
            p.getCategoria().getNombre();
        }

        // 1 (findAllById) + 3 (negocio) + 3 (categoria) = 7 = 1 + 2N
        assertThat(estadisticas.getPrepareStatementCount()).isEqualTo(7);
    }

    // Evidencia del N+1 (después): mismo escenario, con JOIN FETCH. Siempre 1
    // consulta, sin importar cuántos paquetes existan.
    @Test
    @Transactional
    void conJoinFetchElCatalogoQuedaEnUnaSolaConsulta() {
        var negocio = negocioRepository.save(
                new Negocio("Negocio JOIN FETCH", "Restaurante", "Test", LocalTime.of(21, 0)));
        var categoria = categoriaRepository.save(new Categoria("Categoría JOIN FETCH " + System.nanoTime()));
        paqueteSorpresaRepository.save(new PaqueteSorpresa(
                negocio, categoria, "Paquete JOIN FETCH", 1,
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                Instant.now().plusSeconds(3600), "disponible"));
        entityManager.flush();
        entityManager.clear();

        Statistics estadisticas = estadisticasDeHibernate();
        estadisticas.clear();

        List<PaqueteSorpresa> catalogo = paqueteSorpresaRepository.catalogoPorEstado("disponible");
        for (PaqueteSorpresa p : catalogo) {
            p.getNegocio().getNombre();
            p.getCategoria().getNombre();
        }

        assertThat(estadisticas.getPrepareStatementCount()).isEqualTo(1);
    }

    private Statistics estadisticasDeHibernate() {
        Statistics estadisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estadisticas.setStatisticsEnabled(true);
        return estadisticas;
    }
}