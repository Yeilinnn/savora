package cr.ac.una.savora.business.excepcion;

public class UsuarioDuplicadoException extends RuntimeException {
    public UsuarioDuplicadoException(String detalle) {
        super(detalle);
    }
}