package cr.ac.una.savora.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa las tareas programadas solo en dev y prod. En las pruebas (perfil "test") no corren,
 * para que no cierren paquetes por su cuenta en medio de un test.
 */
@Configuration
@EnableScheduling
@Profile({"dev", "prod"})
public class ProgramacionConfig {
}