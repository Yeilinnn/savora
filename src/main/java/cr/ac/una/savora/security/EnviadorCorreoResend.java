package cr.ac.una.savora.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envía correos con la API HTTP de Resend (los servicios gratuitos de Render bloquean SMTP).
 * Si no hay API key configurada (SAVORA_RESEND_API_KEY), no envía nada: deja el contenido en el
 * log del servidor, para poder probar el flujo sin proveedor de correo.
 */
@Component
public class EnviadorCorreoResend implements EnviadorCorreo {

    private static final Logger log = LoggerFactory.getLogger(EnviadorCorreoResend.class);
    private static final String URL_RESEND = "https://api.resend.com/emails";
    private static final String REMITENTE_POR_DEFECTO = "Savora <onboarding@resend.dev>";

    private final String apiKey;
    private final String remitente;
    private final RestClient restClient;

    public EnviadorCorreoResend(
            @Value("${savora.resend.api-key:}") String apiKey, @Value("${savora.mail.from:}") String remitente) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.remitente = remitente == null || remitente.isBlank() ? REMITENTE_POR_DEFECTO : remitente.trim();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(fabrica).build();
    }

    @Override
    public void enviar(String para, String asunto, String texto) {
        if (apiKey.isEmpty()) {
            log.warn("evento=correo_simulado para={} asunto={} cuerpo={}", limpiar(para), asunto, texto);
            return;
        }
        try {
            restClient.post()
                    .uri(URL_RESEND)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.USER_AGENT, "savora/1.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", remitente, "to", List.of(para), "subject", asunto, "text", texto))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("evento=correo_fallido para={} causa={}", limpiar(para), ex.getMessage());
            throw new IllegalStateException("No se pudo enviar el correo", ex);
        }
    }

    private static String limpiar(String valor) {
        return valor == null ? "desconocido" : valor.replaceAll("[\\r\\n]", "");
    }
}