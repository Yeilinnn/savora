package cr.ac.una.savora.business.excepcion;

public class CantidadNoDisponibleException extends RuntimeException {
    public CantidadNoDisponibleException(Long paqueteId) {
        super("El paquete " + paqueteId + " no tiene unidades disponibles");
    }
}
