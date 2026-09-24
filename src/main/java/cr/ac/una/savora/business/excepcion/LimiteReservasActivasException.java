package cr.ac.una.savora.business.excepcion;

public class LimiteReservasActivasException extends RuntimeException {
    public LimiteReservasActivasException(Long clienteId, int limite) {
        super("El cliente " + clienteId + " ya alcanzó su límite de " + limite + " reservas activas");
    }
}