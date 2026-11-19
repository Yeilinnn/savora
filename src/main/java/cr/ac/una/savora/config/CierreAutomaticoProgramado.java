package cr.ac.una.savora.config;

import cr.ac.una.savora.business.CierreDePaquetesService;
import cr.ac.una.savora.business.dto.CierreResumen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cierre automático (Proceso 4): cada cierto tiempo busca los paquetes disponibles cuya hora
 * límite ya pasó y los dona a una organización comunitaria o los marca como perdidos. Así el
 * negocio no tiene que cerrarlos a mano, aunque sigue pudiendo hacerlo.
 *
 * Ajustable con savora.cierre.espera-inicial-ms (30 s) y savora.cierre.intervalo-ms (5 min).
 */
@Component
@Profile({"dev", "prod"})
public class CierreAutomaticoProgramado {

    private static final Logger log = LoggerFactory.getLogger(CierreAutomaticoProgramado.class);

    private final CierreDePaquetesService cierreDePaquetesService;

    public CierreAutomaticoProgramado(CierreDePaquetesService cierreDePaquetesService) {
        this.cierreDePaquetesService = cierreDePaquetesService;
    }

    @Scheduled(
            initialDelayString = "${savora.cierre.espera-inicial-ms:30000}",
            fixedDelayString = "${savora.cierre.intervalo-ms:300000}")
    public void cerrarPaquetesVencidos() {
        try {
            List<CierreResumen> cierres = cierreDePaquetesService.cerrarPaquetesVencidosDisponibles();
            if (!cierres.isEmpty()) {
                log.info("Cierre automático: {} paquete(s) cerrado(s)", cierres.size());
            }
        } catch (RuntimeException e) {
            log.error("Falló el cierre automático de paquetes vencidos", e);
        }
    }
}