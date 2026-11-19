package cr.ac.una.savora.business;

import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilImpactoPosicionamientoTest {

    @Mock
    PerfilImpactoRepository perfilImpactoRepository;

    PerfilImpactoService service;

    @BeforeEach
    void setUp() {
        service = new PerfilImpactoService(
                perfilImpactoRepository, Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneOffset.UTC));
    }

    private PerfilImpacto negocio(String id, int donados, int perdidos) {
        PerfilImpacto perfil = new PerfilImpacto(id, TipoPropietario.NEGOCIO);
        perfil.setPaquetesDonados(donados);
        perfil.setPaquetesPerdidos(perdidos);
        return perfil;
    }

    @Test
    void registrarKilogramosSumaAlAcumulado() {
        PerfilImpacto perfil = new PerfilImpacto("7", TipoPropietario.CLIENTE);
        perfil.setKgRescatados(new BigDecimal("2.0"));
        when(perfilImpactoRepository.findByPropietarioIdAndTipoPropietario("7", TipoPropietario.CLIENTE))
                .thenReturn(Optional.of(perfil));
        when(perfilImpactoRepository.save(any(PerfilImpacto.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarKilogramos("7", TipoPropietario.CLIENTE, 3);

        assertThat(perfil.getKgRescatados()).isEqualByComparingTo("5.0");
    }

    @Test
    void sonDestacadosLosNegociosQueDonanLaMayoriaDeSuExcedente() {
        when(perfilImpactoRepository.findByTipoPropietarioAndPropietarioIdIn(
                        TipoPropietario.NEGOCIO, List.of("1", "2", "3", "4")))
                .thenReturn(List.of(
                        negocio("1", 4, 1),   // 80 %: destacado
                        negocio("2", 1, 1),   // 50 %: no
                        negocio("3", 0, 0),   // sin historial: no
                        negocio("4", 0, 3))); // solo pérdidas: no

        assertThat(service.negociosDestacados(List.of(1L, 2L, 3L, 4L))).containsExactly(1L);
    }

    @Test
    void sinNegociosNoConsultaMongo() {
        assertThat(service.negociosDestacados(List.of())).isEmpty();
        verify(perfilImpactoRepository, never()).findByTipoPropietarioAndPropietarioIdIn(any(), any());
    }
}