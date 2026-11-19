package cr.ac.una.savora.business.excepcion;

/** El pago simulado no fue aprobado (tarjeta inválida, vencida o rechazada). */
public class PagoRechazadoException extends RuntimeException {
    public PagoRechazadoException(String detalle) {
        super(detalle);
    }
}