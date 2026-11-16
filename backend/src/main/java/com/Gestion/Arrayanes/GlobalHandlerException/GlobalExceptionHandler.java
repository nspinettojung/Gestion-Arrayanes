package com.Gestion.Arrayanes.GlobalHandlerException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI PROBLEM_TYPE = URI.create("about:blank");

    /**
     * Error de validación de un campo. Se serializa como
     * {@code {"field": "...", "message": "..."}} y viaja en la propiedad
     * {@code errors} del ProblemDetail (extensión RFC 9457).
     */
    public record FieldViolation(String field, String message) {
    }

    @ExceptionHandler(GlobalCustomException.class)
    public ProblemDetail handlerCustomException(GlobalCustomException ex) {
        return buildProblem(ex.getStatus(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return buildProblem(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos.", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldViolation> errors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return buildProblem(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos.", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("No se pudo leer el cuerpo de la solicitud: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido o está mal formado.", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST,
                "Falta el parámetro requerido: " + ex.getParameterName(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un formato inválido.", null);
    }

    @ExceptionHandler(DateTimeException.class)
    public ProblemDetail handleDateTimeException(DateTimeException ex) {
        return buildProblem(HttpStatus.BAD_REQUEST,
                "La fecha o los componentes de fecha indicados no son válidos.", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex) {
        ProblemDetail problem = buildProblem(HttpStatus.CONFLICT,
                "La reserva fue modificada por otro usuario. Recargá los datos y volvé a intentar.", null);
        log.warn("[{}] Conflicto de concurrencia: {}", requestIdOf(problem), ex.getMessage());
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail problem = buildProblem(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes.", null);
        log.warn("[{}] Violación de integridad de datos: {}",
                requestIdOf(problem), ex.getMostSpecificCause().getMessage());
        return problem;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        return buildProblem(HttpStatus.NOT_FOUND, "El recurso solicitado no existe.", null);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente nuevamente.", null);
        log.error("[{}] Error no controlado", requestIdOf(problem), ex);
        return problem;
    }

    private String requestIdOf(ProblemDetail problem) {
        return String.valueOf(problem.getProperties().get("requestId"));
    }

    private ProblemDetail buildProblem(HttpStatus status, String detail, List<FieldViolation> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(PROBLEM_TYPE);
        problem.setProperty("requestId", UUID.randomUUID().toString());
        problem.setProperty("timestamp", LocalDateTime.now());

        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }

        return problem;
    }
}
