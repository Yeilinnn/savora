package cr.ac.una.savora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegistroEventosSeguridad {

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    public void loginFallido(String usuario) {
        log.warn("evento=login_fallido usuario={}", sanitizar(usuario));
    }

    public void accesoDenegado(String contexto) {
        log.warn("evento=acceso_denegado contexto={}", contexto);
    }

    public void conflictoConcurrencia(String entidad) {
        log.warn("evento=conflicto_concurrencia entidad={}", entidad != null ? entidad : "desconocida");
    }

    private String sanitizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "desconocido";
        }
        return valor.replaceAll("[\\r\\n]", "");
    }
}
