package cr.ac.una.savora.business.excepcion;

public class ReservaNoPendienteException extends RuntimeException {
    public ReservaNoPendienteException(Long reservaId, String estadoActual) {
        super("La reserva " + reservaId + " ya fue procesada (estado: " + estadoActual + ")");
    }
}