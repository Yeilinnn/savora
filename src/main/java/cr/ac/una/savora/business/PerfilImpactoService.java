package cr.ac.una.savora.business;

import cr.ac.una.savora.business.dto.PerfilImpactoResumen;
import cr.ac.una.savora.data.CodigoInsignia;
import cr.ac.una.savora.data.Insignia;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PerfilImpactoService {

    private static final int LIMITE_RESERVAS_BASE = 3;
    private static final int LIMITE_RESERVAS_CONFIABLE = 5;
    private static final int UMBRAL_RECOGIDAS_GUARDIAN = 10;
    private static final int UMBRAL_RECOGIDAS_HEROE = 25;
    private static final double RATIO_MINIMO_CONFIABLE = 0.9;

    private static final int MINIMO_PAQUETES_CERO_DESPERDICIO = 10;
    private static final double RATIO_MINIMO_CERO_DESPERDICIO = 0.8;

    // Un negocio sube en el catálogo si dona su excedente en vez de perderlo.
    private static final int MINIMO_DONADOS_DESTACADO = 1;
    private static final double RATIO_MINIMO_DESTACADO = 0.8;

    private static final int SEMANAS_PARA_INSIGNIA_DE_RACHA = 3;
    private static final ZoneId ZONA_COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final PerfilImpactoRepository perfilImpactoRepository;
    private final Clock clock;

    public PerfilImpactoService(PerfilImpactoRepository perfilImpactoRepository, Clock clock) {
        this.perfilImpactoRepository = perfilImpactoRepository;
        this.clock = clock;
    }

    public PerfilImpacto obtenerOCrear(String propietarioId, TipoPropietario tipoPropietario) {
        return perfilImpactoRepository
                .findByPropietarioIdAndTipoPropietario(propietarioId, tipoPropietario)
                .orElseGet(() -> perfilImpactoRepository.save(new PerfilImpacto(propietarioId, tipoPropietario)));
    }

    public PerfilImpactoResumen consultarResumen(String propietarioId, TipoPropietario tipoPropietario) {
        PerfilImpacto perfil = obtenerOCrear(propietarioId, tipoPropietario);
        return new PerfilImpactoResumen(
                perfil.getPropietarioId(),
                perfil.getTipoPropietario().name(),
                perfil.getKgRescatados().doubleValue(),
                perfil.getReservasRecogidas(),
                perfil.getReservasNoRecogidas(),
                perfil.getPaquetesDonados(),
                perfil.getPaquetesPerdidos(),
                perfil.getInsignias().stream().map(i -> i.getCodigo().name()).toList(),
                calcularLimiteReservas(perfil),
                rachaVigente(perfil),
                perfil.getMejorRachaSemanas());
    }

    public int calcularLimiteReservas(PerfilImpacto perfil) {
        int totalReservas = perfil.getReservasRecogidas() + perfil.getReservasNoRecogidas();
        if (totalReservas == 0) {
            return LIMITE_RESERVAS_BASE;
        }
        double ratio = (double) perfil.getReservasRecogidas() / totalReservas;
        boolean esConfiable = perfil.getReservasRecogidas() >= UMBRAL_RECOGIDAS_GUARDIAN
                && ratio >= RATIO_MINIMO_CONFIABLE;
        return esConfiable ? LIMITE_RESERVAS_CONFIABLE : LIMITE_RESERVAS_BASE;
    }

    // ---- Clientes ----

    public PerfilImpacto registrarRecogida(String clienteId) {
        PerfilImpacto perfil = obtenerOCrear(clienteId, TipoPropietario.CLIENTE);
        perfil.setReservasRecogidas(perfil.getReservasRecogidas() + 1);
        actualizarRacha(perfil);
        otorgarSiCorresponde(perfil, CodigoInsignia.PRIMER_RESCATE, perfil.getReservasRecogidas() >= 1);
        otorgarSiCorresponde(
                perfil, CodigoInsignia.GUARDIAN_DEL_BARRIO, perfil.getReservasRecogidas() >= UMBRAL_RECOGIDAS_GUARDIAN);
        otorgarSiCorresponde(
                perfil, CodigoInsignia.HEROE_DEL_PLANETA, perfil.getReservasRecogidas() >= UMBRAL_RECOGIDAS_HEROE);
        otorgarSiCorresponde(
                perfil,
                CodigoInsignia.RACHA_3_SEMANAS,
                perfil.getMejorRachaSemanas() >= SEMANAS_PARA_INSIGNIA_DE_RACHA);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarNoRecogida(String clienteId) {
        PerfilImpacto perfil = obtenerOCrear(clienteId, TipoPropietario.CLIENTE);
        perfil.setReservasNoRecogidas(perfil.getReservasNoRecogidas() + 1);
        return perfilImpactoRepository.save(perfil);
    }

    // ---- Negocios ----

    /** Un paquete del negocio fue recogido por un cliente: cuenta como venta y como actividad de la semana. */
    public PerfilImpacto registrarVentaNegocio(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setReservasRecogidas(perfil.getReservasRecogidas() + 1);
        actualizarRacha(perfil);
        otorgarSiCorresponde(perfil, CodigoInsignia.PRIMERA_VENTA, perfil.getReservasRecogidas() >= 1);
        otorgarSiCorresponde(
                perfil,
                CodigoInsignia.NEGOCIO_CONSTANTE,
                perfil.getMejorRachaSemanas() >= SEMANAS_PARA_INSIGNIA_DE_RACHA);
        return perfilImpactoRepository.save(perfil);
    }

    /** Un cliente no recogió un paquete reservado del negocio. */
    public PerfilImpacto registrarNoRecogidaNegocio(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setReservasNoRecogidas(perfil.getReservasNoRecogidas() + 1);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarDonacion(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setPaquetesDonados(perfil.getPaquetesDonados() + 1);
        otorgarSiCorresponde(perfil, CodigoInsignia.PRIMERA_DONACION, perfil.getPaquetesDonados() >= 1);
        evaluarCeroDesperdicio(perfil);
        return perfilImpactoRepository.save(perfil);
    }

    public PerfilImpacto registrarPerdida(String negocioId) {
        PerfilImpacto perfil = obtenerOCrear(negocioId, TipoPropietario.NEGOCIO);
        perfil.setPaquetesPerdidos(perfil.getPaquetesPerdidos() + 1);
        return perfilImpactoRepository.save(perfil);
    }

    // ---- Kilogramos rescatados y posicionamiento ----

    /**
     * Suma kilogramos de comida rescatada al perfil. Convención del dominio: cada unidad de un
     * paquete equivale a 1 kg (la misma que usa el cierre para las donaciones).
     */
    public PerfilImpacto registrarKilogramos(String propietarioId, TipoPropietario tipo, int kilogramos) {
        PerfilImpacto perfil = obtenerOCrear(propietarioId, tipo);
        perfil.setKgRescatados(perfil.getKgRescatados().add(BigDecimal.valueOf(kilogramos)));
        return perfilImpactoRepository.save(perfil);
    }

    /**
     * Negocios con buen historial de donación: al menos una donación y un 80 % o más de sus
     * paquetes sobrantes donados (no perdidos). El catálogo los muestra primero.
     */
    public Set<Long> negociosDestacados(Collection<Long> negocioIds) {
        if (negocioIds.isEmpty()) {
            return Set.of();
        }
        Collection<String> ids = negocioIds.stream().map(String::valueOf).toList();
        return perfilImpactoRepository.findByTipoPropietarioAndPropietarioIdIn(TipoPropietario.NEGOCIO, ids).stream()
                .filter(PerfilImpactoService::esDestacado)
                .map(p -> Long.valueOf(p.getPropietarioId()))
                .collect(Collectors.toSet());
    }

    static boolean esDestacado(PerfilImpacto perfil) {
        int total = perfil.getPaquetesDonados() + perfil.getPaquetesPerdidos();
        return perfil.getPaquetesDonados() >= MINIMO_DONADOS_DESTACADO
                && (double) perfil.getPaquetesDonados() / total >= RATIO_MINIMO_DESTACADO;
    }

    // ---- Rachas semanales ----

    /**
     * Una racha son semanas (lunes a domingo, hora de Costa Rica) consecutivas con al menos una
     * recogida. Varias recogidas en la misma semana cuentan una sola vez; si se salta una semana
     * completa, la racha vuelve a empezar en 1.
     */
    private void actualizarRacha(PerfilImpacto perfil) {
        LocalDate semana = lunesDeLaSemanaActual();
        LocalDate ultima = perfil.getUltimaSemanaActiva() == null ? null : LocalDate.parse(perfil.getUltimaSemanaActiva());
        if (semana.equals(ultima)) {
            return;
        }
        int racha = (ultima != null && ultima.plusWeeks(1).equals(semana)) ? perfil.getRachaActualSemanas() + 1 : 1;
        perfil.setRachaActualSemanas(racha);
        perfil.setMejorRachaSemanas(Math.max(perfil.getMejorRachaSemanas(), racha));
        perfil.setUltimaSemanaActiva(semana.toString());
    }

    /** La racha guardada solo sigue "viva" si hubo actividad esta semana o la anterior. */
    private int rachaVigente(PerfilImpacto perfil) {
        if (perfil.getUltimaSemanaActiva() == null) {
            return 0;
        }
        LocalDate ultima = LocalDate.parse(perfil.getUltimaSemanaActiva());
        LocalDate semana = lunesDeLaSemanaActual();
        boolean viva = ultima.equals(semana) || ultima.plusWeeks(1).equals(semana);
        return viva ? perfil.getRachaActualSemanas() : 0;
    }

    private LocalDate lunesDeLaSemanaActual() {
        return LocalDate.now(clock.withZone(ZONA_COSTA_RICA)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void evaluarCeroDesperdicio(PerfilImpacto perfil) {
        int totalPaquetes = perfil.getPaquetesDonados() + perfil.getPaquetesPerdidos();
        if (totalPaquetes < MINIMO_PAQUETES_CERO_DESPERDICIO) {
            return;
        }
        double ratio = (double) perfil.getPaquetesDonados() / totalPaquetes;
        otorgarSiCorresponde(perfil, CodigoInsignia.CERO_DESPERDICIO, ratio >= RATIO_MINIMO_CERO_DESPERDICIO);
    }

    private void otorgarSiCorresponde(PerfilImpacto perfil, CodigoInsignia codigo, boolean cumpleCondicion) {
        boolean yaLaTiene = perfil.getInsignias().stream().anyMatch(i -> i.getCodigo() == codigo);
        if (cumpleCondicion && !yaLaTiene) {
            perfil.getInsignias().add(new Insignia(codigo, Instant.now(clock)));
        }
    }
}