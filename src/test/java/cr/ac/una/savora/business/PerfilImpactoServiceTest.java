package cr.ac.una.savora.business;

import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilImpactoServiceTest {

    @Mock
    PerfilImpactoRepository perfilImpactoRepository;

    @InjectMocks
    PerfilImpactoService service;

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

    @Test
    void consultarResumenIncluyeLimiteCalculado() {
        PerfilImpacto perfil = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        when(perfilImpactoRepository.findByPropietarioIdAndTipoPropietario("1", TipoPropietario.CLIENTE))
                .thenReturn(Optional.of(perfil));

        assertThat(service.consultarResumen("1", TipoPropietario.CLIENTE).limiteReservasActivas()).isEqualTo(3);
    }
}