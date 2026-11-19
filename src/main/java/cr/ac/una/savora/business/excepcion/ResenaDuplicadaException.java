package cr.ac.una.savora.business.excepcion;

public class ResenaDuplicadaException extends RuntimeException {
    public ResenaDuplicadaException(Long reservaId) {
        super("La reserva " + reservaId + " ya tiene una reseña");
    }
}