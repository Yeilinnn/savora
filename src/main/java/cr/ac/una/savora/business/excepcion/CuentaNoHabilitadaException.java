package cr.ac.una.savora.business.excepcion;

/** Cuenta desactivada, o negocio pendiente de aprobación / suspendido. */
public class CuentaNoHabilitadaException extends RuntimeException {
    public CuentaNoHabilitadaException(String detalle) {
        super(detalle);
    }
}