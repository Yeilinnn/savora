package cr.ac.una.savora.config;

import cr.ac.una.savora.data.CategoriaRepository;
import cr.ac.una.savora.data.NegocioRepository;
import cr.ac.una.savora.data.PaqueteSorpresa;
import cr.ac.una.savora.data.PaqueteSorpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Paquetes de demostración con hora límite RELATIVA al momento actual (la semilla de Flyway tiene
 * fechas fijas que ya vencieron, y por eso no se podía reservar sin crear datos a mano).
 *
 * Corre en dev siempre, y en prod solo si SAVORA_SEED_DEMO=true. Si ya hay 3 o más paquetes
 * disponibles con hora límite futura, no crea nada. No corre en las pruebas.
 */
@Component
@Profile({"dev", "prod"})
public class DatosDemoSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatosDemoSeeder.class);
    private static final int MINIMO_VIGENTES = 3;

    private final PaqueteSorpresaRepository paqueteSorpresaRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final Environment environment;
    private final Clock clock;
    private final boolean demoActivada;

    public DatosDemoSeeder(
            PaqueteSorpresaRepository paqueteSorpresaRepository,
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            Environment environment,
            Clock clock,
            @Value("${SAVORA_SEED_DEMO:false}") boolean demoActivada) {
        this.paqueteSorpresaRepository = paqueteSorpresaRepository;
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.environment = environment;
        this.clock = clock;
        this.demoActivada = demoActivada;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sembrar() {
        if (!environment.acceptsProfiles(Profiles.of("dev")) && !demoActivada) {
            return;
        }
        OffsetDateTime ahora = OffsetDateTime.now(clock);
        long vigentes = paqueteSorpresaRepository.findAll().stream()
                .filter(p -> "disponible".equals(p.getEstado()) && p.getHoraLimiteRecogida().isAfter(ahora))
                .count();
        if (vigentes >= MINIMO_VIGENTES) {
            return;
        }

        List<Demo> demos = List.of(
                new Demo(1L, "Panadería", "Bolsa de pan y repostería del día", 5000, 2500, 5),
                new Demo(2L, "Comida preparada", "Almuerzo casero sorpresa", 4500, 2000, 8),
                new Demo(3L, "Frutas y verduras", "Caja de frutas y verduras de temporada", 6000, 2800, 26),
                new Demo(1L, "Panadería", "Pastelitos y empanadas del cierre", 4000, 1800, 30));

        int creados = 0;
        for (Demo demo : demos) {
            var negocio = negocioRepository.findById(demo.negocioId());
            var categoria = categoriaRepository.findByNombre(demo.categoria());
            if (negocio.isEmpty() || categoria.isEmpty()) {
                continue;
            }
            paqueteSorpresaRepository.save(new PaqueteSorpresa(
                    negocio.get(),
                    categoria.get(),
                    demo.descripcion(),
                    1,
                    BigDecimal.valueOf(demo.precioOriginal()),
                    BigDecimal.valueOf(demo.precioConDescuento()),
                    ahora.plusHours(demo.horasHastaElLimite()),
                    "disponible"));
            creados++;
        }
        log.info("Datos de demostración: {} paquete(s) con hora límite relativa creados", creados);
    }

    private record Demo(
            Long negocioId,
            String categoria,
            String descripcion,
            int precioOriginal,
            int precioConDescuento,
            int horasHastaElLimite) {
    }
}