package cr.ac.una.savora.business;

import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.TipoPropietario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerfilImpactoServiceTest {

    private final PerfilImpactoService service = new PerfilImpactoService(null);

    @Test
    void sinReservasDaElLimiteBase() {
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);

        assertEquals(3, service.calcularLimiteReservas(perfil));
    }

    @Test
    void clienteConfiableSubeElLimite() {
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(10);
        perfil.setReservasNoRecogidas(0);

        assertEquals(5, service.calcularLimiteReservas(perfil));
    }

    @Test
    void muchasReservasPeroMalRatioSeQuedaEnElLimiteBase() {
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(10);
        perfil.setReservasNoRecogidas(5);

        assertEquals(3, service.calcularLimiteReservas(perfil));
    }

    @Test
    void buenRatioPeroPocasReservasSeQuedaEnElLimiteBase() {
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(2);
        perfil.setReservasNoRecogidas(0);

        assertEquals(3, service.calcularLimiteReservas(perfil));
    }
}