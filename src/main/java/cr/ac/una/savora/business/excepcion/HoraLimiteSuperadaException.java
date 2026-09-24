package cr.ac.una.savora.business.excepcion;

public class HoraLimiteSuperadaException extends RuntimeException {
    public HoraLimiteSuperadaException(Long paqueteId) {
        super("Ya pasó la hora límite de recogida del paquete " + paqueteId);
    }
}