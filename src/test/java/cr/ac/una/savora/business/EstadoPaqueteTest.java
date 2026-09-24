package cr.ac.una.savora.business;

import cr.ac.una.savora.business.excepcion.TransicionEstadoInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoPaqueteTest {

    @Test
    void disponiblePuedeTransitarAReservado() {
        assertThat(EstadoPaquete.DISPONIBLE.transitarA(EstadoPaquete.RESERVADO)).isEqualTo("reservado");
    }

    @Test
    void reservadoNoPuedeVolverADisponible() {
        assertThatThrownBy(() -> EstadoPaquete.RESERVADO.transitarA(EstadoPaquete.DISPONIBLE))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    void desdeConvierteElTextoDeLaBaseDeDatosAlEnum() {
        assertThat(EstadoPaquete.desde("disponible")).isEqualTo(EstadoPaquete.DISPONIBLE);
    }

    @Test
    void desdeRechazaUnEstadoDesconocido() {
        assertThatThrownBy(() -> EstadoPaquete.desde("inexistente"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}