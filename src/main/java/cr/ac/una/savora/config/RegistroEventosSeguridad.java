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

    public void registroCliente(String usuario) {
        log.info("evento=registro_cliente usuario={}", sanitizar(usuario));
    }

    public void negocioRegistrado(String usuario) {
        log.info("evento=registro_negocio usuario={}", sanitizar(usuario));
    }

    public void adminAccion(String administrador, String accion, String objetivo) {
        log.info(
                "evento=admin_accion admin={} accion={} objetivo={}",
                sanitizar(administrador),
                sanitizar(accion),
                sanitizar(objetivo));
    }

    public void restablecimientoSolicitado(String usuario) {
        log.info("evento=restablecimiento_solicitado usuario={}", sanitizar(usuario));
    }

    public void contrasenaCambiada(String usuario, String via) {
        log.info("evento=contrasena_cambiada usuario={} via={}", sanitizar(usuario), via);
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