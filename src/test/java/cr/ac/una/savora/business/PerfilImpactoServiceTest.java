package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.PerfilImpactoResumen;
import cr.ac.una.savora.data.CodigoInsignia;
import cr.ac.una.savora.data.Insignia;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilImpactoServiceTest {

    // Miércoles 18/11/2026 12:00 hora de Costa Rica. Su semana empieza el lunes 2026-11-16.
    private static final String SEMANA_ACTUAL = "2026-11-16";
    private static final String SEMANA_ANTERIOR = "2026-11-09";
    private static final String HACE_TRES_SEMANAS = "2026-10-26";

    @Mock
    PerfilImpactoRepository perfilImpactoRepository;

    PerfilImpactoService service;

    final Clock clock = Clock.fixed(Instant.parse("2026-11-18T18:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new PerfilImpactoService(perfilImpactoRepository, clock);
    }

    private PerfilImpacto existente(String id, TipoPropietario tipo) {
        PerfilImpacto perfil = new PerfilImpacto(id, tipo);
        when(perfilImpactoRepository.findByPropietarioIdAndTipoPropietario(id, tipo)).thenReturn(Optional.of(perfil));
        when(perfilImpactoRepository.save(any(PerfilImpacto.class))).thenAnswer(inv -> inv.getArgument(0));
        return perfil;
    }

    private boolean tiene(PerfilImpacto perfil, CodigoInsignia codigo) {
        return perfil.getInsignias().stream().map(Insignia::getCodigo).anyMatch(c -> c == codigo);
    }

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

    @Test
    void laPrimeraRecogidaIniciaLaRachaYDaElPrimerRescate() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);

        service.registrarRecogida("1");

        assertThat(perfil.getRachaActualSemanas()).isEqualTo(1);
        assertThat(perfil.getMejorRachaSemanas()).isEqualTo(1);
        assertThat(perfil.getUltimaSemanaActiva()).isEqualTo(SEMANA_ACTUAL);
        assertThat(tiene(perfil, CodigoInsignia.PRIMER_RESCATE)).isTrue();
        assertThat(tiene(perfil, CodigoInsignia.RACHA_3_SEMANAS)).isFalse();
    }

    @Test
    void variasRecogidasEnLaMismaSemanaCuentanUnaSolaVezParaLaRacha() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);
        perfil.setRachaActualSemanas(2);
        perfil.setMejorRachaSemanas(2);
        perfil.setUltimaSemanaActiva(SEMANA_ACTUAL);

        service.registrarRecogida("1");

        assertThat(perfil.getRachaActualSemanas()).isEqualTo(2);
        assertThat(perfil.getReservasRecogidas()).isEqualTo(1);
    }

    @Test
    void tresSemanasSeguidasOtorganLaInsigniaDeRacha() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);
        perfil.setRachaActualSemanas(2);
        perfil.setMejorRachaSemanas(2);
        perfil.setUltimaSemanaActiva(SEMANA_ANTERIOR);

        service.registrarRecogida("1");

        assertThat(perfil.getRachaActualSemanas()).isEqualTo(3);
        assertThat(perfil.getMejorRachaSemanas()).isEqualTo(3);
        assertThat(tiene(perfil, CodigoInsignia.RACHA_3_SEMANAS)).isTrue();
    }

    @Test
    void saltarseUnaSemanaReiniciaLaRachaPeroConservaLaMejor() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);
        perfil.setRachaActualSemanas(4);
        perfil.setMejorRachaSemanas(4);
        perfil.setUltimaSemanaActiva(HACE_TRES_SEMANAS);

        service.registrarRecogida("1");

        assertThat(perfil.getRachaActualSemanas()).isEqualTo(1);
        assertThat(perfil.getMejorRachaSemanas()).isEqualTo(4);
    }

    @Test
    void diezRecogidasDanGuardianYVeinticincoHeroeDelPlaneta() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(9);

        service.registrarRecogida("1");
        assertThat(tiene(perfil, CodigoInsignia.GUARDIAN_DEL_BARRIO)).isTrue();
        assertThat(tiene(perfil, CodigoInsignia.HEROE_DEL_PLANETA)).isFalse();

        perfil.setReservasRecogidas(24);
        service.registrarRecogida("1");
        assertThat(tiene(perfil, CodigoInsignia.HEROE_DEL_PLANETA)).isTrue();
    }

    @Test
    void unaInsigniaYaOtorgadaNoSeDuplica() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);

        service.registrarRecogida("1");
        service.registrarRecogida("1");

        assertThat(perfil.getInsignias().stream().filter(i -> i.getCodigo() == CodigoInsignia.PRIMER_RESCATE))
                .hasSize(1);
    }

    @Test
    void elResumenMuestraLaRachaSoloSiSigueVigente() {
        PerfilImpacto viva = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        viva.setRachaActualSemanas(3);
        viva.setMejorRachaSemanas(3);
        viva.setUltimaSemanaActiva(SEMANA_ANTERIOR);
        when(perfilImpactoRepository.findByPropietarioIdAndTipoPropietario("1", TipoPropietario.CLIENTE))
                .thenReturn(Optional.of(viva));

        PerfilImpactoResumen resumenVivo = service.consultarResumen("1", TipoPropietario.CLIENTE);
        assertThat(resumenVivo.rachaActualSemanas()).isEqualTo(3);

        PerfilImpacto vencida = new PerfilImpacto("2", TipoPropietario.CLIENTE);
        vencida.setRachaActualSemanas(3);
        vencida.setMejorRachaSemanas(3);
        vencida.setUltimaSemanaActiva(HACE_TRES_SEMANAS);
        when(perfilImpactoRepository.findByPropietarioIdAndTipoPropietario("2", TipoPropietario.CLIENTE))
                .thenReturn(Optional.of(vencida));

        PerfilImpactoResumen resumenVencido = service.consultarResumen("2", TipoPropietario.CLIENTE);
        assertThat(resumenVencido.rachaActualSemanas()).isZero();
        assertThat(resumenVencido.mejorRachaSemanas()).isEqualTo(3);
    }

    @Test
    void laVentaDeUnNegocioSumaRecogidaRachaYPrimeraVenta() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);

        service.registrarVentaNegocio("1");

        assertThat(perfil.getReservasRecogidas()).isEqualTo(1);
        assertThat(perfil.getRachaActualSemanas()).isEqualTo(1);
        assertThat(tiene(perfil, CodigoInsignia.PRIMERA_VENTA)).isTrue();
        assertThat(tiene(perfil, CodigoInsignia.NEGOCIO_CONSTANTE)).isFalse();
    }

    @Test
    void unNegocioConTresSemanasSeguidasGanaNegocioConstante() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);
        perfil.setRachaActualSemanas(2);
        perfil.setMejorRachaSemanas(2);
        perfil.setUltimaSemanaActiva(SEMANA_ANTERIOR);

        service.registrarVentaNegocio("1");

        assertThat(tiene(perfil, CodigoInsignia.NEGOCIO_CONSTANTE)).isTrue();
    }

    @Test
    void laNoRecogidaDelNegocioSeCuenta() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);

        service.registrarNoRecogidaNegocio("1");

        assertThat(perfil.getReservasNoRecogidas()).isEqualTo(1);
    }

    @Test
    void laPrimeraDonacionDaLaInsignia() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);

        service.registrarDonacion("1");

        assertThat(perfil.getPaquetesDonados()).isEqualTo(1);
        assertThat(tiene(perfil, CodigoInsignia.PRIMERA_DONACION)).isTrue();
        assertThat(tiene(perfil, CodigoInsignia.CERO_DESPERDICIO)).isFalse();
    }

    @Test
    void ceroDesperdicioRequiereDiezPaquetesYOchentaPorCientoDonados() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);
        perfil.setPaquetesDonados(7);
        perfil.setPaquetesPerdidos(2);

        service.registrarDonacion("1");

        assertThat(tiene(perfil, CodigoInsignia.CERO_DESPERDICIO)).isTrue();
    }

    @Test
    void conMuchasPerdidasNoHayCeroDesperdicio() {
        PerfilImpacto perfil = existente("1", TipoPropietario.NEGOCIO);
        perfil.setPaquetesDonados(5);
        perfil.setPaquetesPerdidos(5);

        service.registrarDonacion("1");
        service.registrarPerdida("1");

        assertThat(perfil.getPaquetesPerdidos()).isEqualTo(6);
        assertThat(tiene(perfil, CodigoInsignia.CERO_DESPERDICIO)).isFalse();
    }

    @Test
    void laNoRecogidaDelClienteSeCuenta() {
        PerfilImpacto perfil = existente("1", TipoPropietario.CLIENTE);

        service.registrarNoRecogida("1");

        assertThat(perfil.getReservasNoRecogidas()).isEqualTo(1);
    }
}