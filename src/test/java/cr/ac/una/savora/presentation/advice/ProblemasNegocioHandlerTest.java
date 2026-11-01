package cr.ac.una.savora.presentation.advice;

import cr.ac.una.savora.config.RegistroEventosSeguridad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import cr.ac.una.savora.data.PaqueteSorpresa;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProblemasNegocioHandlerTest {

    @Mock
    RegistroEventosSeguridad registroEventosSeguridad;

    @InjectMocks
    ProblemasNegocioHandler handler;

    @Test
    void conflictoConcurrenciaDevuelve409ConTituloEsperado() {
        ProblemDetail problema = handler.conflictoConcurrencia(
                new ObjectOptimisticLockingFailureException(PaqueteSorpresa.class, 5L));

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problema.getTitle()).isEqualTo("Conflicto de concurrencia");
    }
}
