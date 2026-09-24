package cr.ac.una.savora.business.excepcion;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String tipoRecurso, Object id) {
        super(tipoRecurso + " no encontrado: " + id);
    }
}