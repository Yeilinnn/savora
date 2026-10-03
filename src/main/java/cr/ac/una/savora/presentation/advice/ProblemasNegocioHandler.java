package cr.ac.una.savora.presentation.advice;

import cr.ac.una.savora.business.excepcion.CantidadNoDisponibleException;
import cr.ac.una.savora.business.excepcion.CierreAntesDeHoraLimiteException;
import cr.ac.una.savora.business.excepcion.HoraLimiteSuperadaException;
import cr.ac.una.savora.business.excepcion.LimiteReservasActivasException;
import cr.ac.una.savora.business.excepcion.RecursoNoEncontradoException;
import cr.ac.una.savora.business.excepcion.TransicionEstadoInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce excepciones de negocio y validación a Problem Details (RFC 9457).
 */
@RestControllerAdvice
public class ProblemasNegocioHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado");
        return problema;
    }

    @ExceptionHandler({
            HoraLimiteSuperadaException.class,
            LimiteReservasActivasException.class,
            CantidadNoDisponibleException.class,
            TransicionEstadoInvalidaException.class,
            CierreAntesDeHoraLimiteException.class
    })
    public ProblemDetail reglaDeNegocio(RuntimeException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problema.setTitle("Regla de negocio incumplida");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacionFormato(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos de entrada inválidos");
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle);
        problema.setTitle("Validación de formato");
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflictoIntegridad(DataIntegrityViolationException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Conflicto con datos existentes (p. ej. recurso duplicado)");
        problema.setTitle("Conflicto");
        return problema;
    }
}
