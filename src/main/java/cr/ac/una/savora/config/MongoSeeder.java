package cr.ac.una.savora.config;

import cr.ac.una.savora.data.CodigoInsignia;
import cr.ac.una.savora.data.Insignia;
import cr.ac.una.savora.data.PerfilImpacto;
import cr.ac.una.savora.data.PerfilImpactoRepository;
import cr.ac.una.savora.data.TipoPropietario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class MongoSeeder implements CommandLineRunner {

    private final PerfilImpactoRepository perfilImpactoRepository;

    public MongoSeeder(PerfilImpactoRepository perfilImpactoRepository) {
        this.perfilImpactoRepository = perfilImpactoRepository;
    }

    @Override
    public void run(String... args) {
        if (perfilImpactoRepository.count() > 0) {
            return;
        }

        PerfilImpacto ana = new PerfilImpacto("1", TipoPropietario.CLIENTE);
        ana.setKgRescatados(new BigDecimal("6.5"));
        ana.setReservasRecogidas(1);
        ana.getInsignias().add(new Insignia(CodigoInsignia.PRIMER_RESCATE, Instant.now().minus(6, ChronoUnit.DAYS)));

        PerfilImpacto luis = new PerfilImpacto("2", TipoPropietario.CLIENTE);
        luis.setKgRescatados(new BigDecimal("3.0"));
        luis.setReservasRecogidas(1);
        luis.getInsignias().add(new Insignia(CodigoInsignia.PRIMER_RESCATE, Instant.now().minus(3, ChronoUnit.DAYS)));

        PerfilImpacto marta = new PerfilImpacto("3", TipoPropietario.CLIENTE);
        marta.setReservasNoRecogidas(1);

        PerfilImpacto panaderia = new PerfilImpacto("1", TipoPropietario.NEGOCIO);
        panaderia.setKgRescatados(new BigDecimal("4.0"));
        panaderia.setPaquetesDonados(1);

        PerfilImpacto soda = new PerfilImpacto("2", TipoPropietario.NEGOCIO);
        soda.setPaquetesPerdidos(1);

        perfilImpactoRepository.saveAll(List.of(ana, luis, marta, panaderia, soda));
    }
}