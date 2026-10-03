package cr.ac.una.savora.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI savoraOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Savora API")
                        .description("API REST v1 — Lab 5 (endpoints de Nazareth; JWT pendiente Yeilin)")
                        .version("1.0"));
    }
}
