package cr.ac.una.savora.business.excepcion;

/** Solicitud de cuenta que no se puede cumplir (enlace vencido, contraseña actual incorrecta, etc.). */
public class SolicitudCuentaInvalidaException extends RuntimeException {
    public SolicitudCuentaInvalidaException(String detalle) {
        super(detalle);
    }
}