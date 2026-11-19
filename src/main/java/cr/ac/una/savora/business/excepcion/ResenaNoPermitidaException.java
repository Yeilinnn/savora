package cr.ac.una.savora.business.excepcion;

public class ResenaNoPermitidaException extends RuntimeException {
    public ResenaNoPermitidaException(Long reservaId, String estado) {
        super("Solo se puede calificar un paquete recogido; la reserva " + reservaId + " está en estado " + estado);
    }
}