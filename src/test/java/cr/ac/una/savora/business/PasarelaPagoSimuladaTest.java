package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.AutorizacionPago;
import cr.ac.una.savora.business.dto.TarjetaSimulada;
import cr.ac.una.savora.business.excepcion.PagoRechazadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasarelaPagoSimuladaTest {

    PasarelaPagoSimulada pasarela;

    @BeforeEach
    void setUp() {
        pasarela = new PasarelaPagoSimulada(Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneOffset.UTC));
    }

    private static TarjetaSimulada tarjeta(String numero, String vencimiento) {
        return new TarjetaSimulada(numero, "María Pérez", vencimiento, "123");
    }

    @Test
    void tarjetaVisaValidaSeAprueba() {
        AutorizacionPago autorizacion = pasarela.autorizar(tarjeta("4242 4242 4242 4242", "12/30"));

        assertThat(autorizacion.marca()).isEqualTo("Visa");
        assertThat(autorizacion.ultimos4()).isEqualTo("4242");
        assertThat(autorizacion.referencia()).startsWith("SIM-");
    }

    @Test
    void tarjetaMastercardValidaSeAprueba() {
        AutorizacionPago autorizacion = pasarela.autorizar(tarjeta("5555555555554444", "12/30"));

        assertThat(autorizacion.marca()).isEqualTo("Mastercard");
        assertThat(autorizacion.ultimos4()).isEqualTo("4444");
    }

    @Test
    void elMesActualToda­víaNoEstaVencido() {
        assertThat(pasarela.autorizar(tarjeta("4242424242424242", "11/26")).ultimos4()).isEqualTo("4242");
    }

    @Test
    void numeroQueNoCumpleLuhnSeRechaza() {
        assertThatThrownBy(() -> pasarela.autorizar(tarjeta("4242 4242 4242 4241", "12/30")))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("no es válido");
    }

    @Test
    void numeroConLetrasSeRechaza() {
        assertThatThrownBy(() -> pasarela.autorizar(tarjeta("4242 abcd 4242 4242", "12/30")))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("no es válido");
    }

    @Test
    void tarjetaVencidaSeRechaza() {
        assertThatThrownBy(() -> pasarela.autorizar(tarjeta("4242424242424242", "10/26")))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("vencida");
    }

    @Test
    void tarjetaDePruebaRechazadaPorElBanco() {
        assertThatThrownBy(() -> pasarela.autorizar(tarjeta("4000 0000 0000 0002", "12/30")))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("rechazada");
    }

    @Test
    void tarjetaDePruebaSinFondos() {
        assertThatThrownBy(() -> pasarela.autorizar(tarjeta("4000000000009995", "12/30")))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("Fondos insuficientes");
    }
}