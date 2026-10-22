package cr.ac.una.savora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${savora.cors.allowed-origins:}") String allowedOrigins) {
        CorsConfiguration configuracion = new CorsConfiguration();
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .forEach(configuracion::addAllowedOrigin);
        }
        configuracion.addAllowedHeader("*");
        configuracion.addAllowedMethod("*");
        configuracion.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource origen = new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/api/**", configuracion);
        origen.registerCorsConfiguration("/auth/**", configuracion);
        return origen;
    }
}
