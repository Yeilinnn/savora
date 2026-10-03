package cr.ac.una.savora.business.excepcion;

public class CierreAntesDeHoraLimiteException extends RuntimeException {
    public CierreAntesDeHoraLimiteException(Long paqueteId) {
        super("Aún no llegó la hora límite de recogida del paquete " + paqueteId);
    }
}
