package cr.ac.una.savora.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PaqueteSorpresaOptimisticLockIT {

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
    PaqueteSorpresaRepository paqueteSorpresaRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void dosTransaccionesConLaMismaVersionGeneranConflicto() {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        AtomicReference<PaqueteSorpresa> copiaObsoleta = new AtomicReference<>();

        transaccion.executeWithoutResult(estado -> copiaObsoleta.set(
                paqueteSorpresaRepository.findById(5L).orElseThrow()));

        transaccion.executeWithoutResult(estado -> {
            PaqueteSorpresa actual = paqueteSorpresaRepository.findById(5L).orElseThrow();
            actual.setCantidad(actual.getCantidad() - 1);
            paqueteSorpresaRepository.saveAndFlush(actual);
        });

        PaqueteSorpresa obsoleto = copiaObsoleta.get();
        obsoleto.setCantidad(obsoleto.getCantidad() - 1);

        assertThatThrownBy(() -> transaccion.execute(estado -> paqueteSorpresaRepository.saveAndFlush(obsoleto)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
