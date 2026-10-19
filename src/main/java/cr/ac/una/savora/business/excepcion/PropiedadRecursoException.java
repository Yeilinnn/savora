package cr.ac.una.savora.business.excepcion;

public class PropiedadRecursoException extends RuntimeException {
    public PropiedadRecursoException(String tipoRecurso, Long id) {
        super("El recurso " + tipoRecurso + " " + id + " no pertenece al usuario autenticado");
    }
}
