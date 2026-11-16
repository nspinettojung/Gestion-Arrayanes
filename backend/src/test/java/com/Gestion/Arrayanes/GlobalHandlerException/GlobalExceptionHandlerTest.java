package com.Gestion.Arrayanes.GlobalHandlerException;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void customExceptionShouldUseItsOwnStatus() {
        ProblemDetail notFound =
                handler.handlerCustomException(new ResourceNotFoundException("No existe"));

        assertProblem(notFound, 404, "Not Found", "No existe");

        ProblemDetail badRequest =
                handler.handlerCustomException(new BadRequestException("Dato inválido"));

        assertProblem(badRequest, 400, "Bad Request", "Dato inválido");
    }

    @Test
    void customExceptionShouldDefaultToConflict() {
        ProblemDetail problem =
                handler.handlerCustomException(new GlobalCustomException("Se superpone"));

        assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(problem.getStatus()));
        assertProblem(problem, 409, "Conflict", "Se superpone");
    }

    @Test
    void validationExceptionShouldReturnFieldErrorsAsList() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new ClientRequestDTO(), "request");
        binding.rejectValue("clientName", "NotBlank", "no puede estar vacío");

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, 0), binding);

        ProblemDetail problem = handler.handleValidationException(ex);

        assertProblem(problem, 400, "Bad Request", "La solicitud contiene datos inválidos.");

        @SuppressWarnings("unchecked")
        var errors = (java.util.List<GlobalExceptionHandler.FieldViolation>) problem.getProperties().get("errors");

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("clientName", errors.get(0).field());
        assertEquals("no puede estar vacío", errors.get(0).message());
    }

    @Test
    void constraintViolationShouldReturnFieldErrorsAsList() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("client.dni");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("no puede estar vacío");

        ProblemDetail problem = handler.handleConstraintViolation(
                new jakarta.validation.ConstraintViolationException(java.util.Set.of(violation)));

        assertProblem(problem, 400, "Bad Request", "La solicitud contiene datos inválidos.");

        @SuppressWarnings("unchecked")
        var errors = (java.util.List<GlobalExceptionHandler.FieldViolation>) problem.getProperties().get("errors");

        assertNotNull(errors);
        assertEquals("client.dni", errors.get(0).field());
        assertEquals("no puede estar vacío", errors.get(0).message());
    }

    @Test
    void missingParameterShouldReturnBadRequest() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("date", "LocalDate");

        ProblemDetail problem = handler.handleMissingParameter(ex);

        assertProblem(problem, 400, "Bad Request", "Falta el parámetro requerido: date");
    }

    @Test
    void optimisticLockShouldReturnConflict() {
        ProblemDetail problem = handler.handleOptimisticLock(
                new ObjectOptimisticLockingFailureException(Booking.class, UUID.randomUUID()));

        assertProblem(problem, 409, "Conflict",
                "La reserva fue modificada por otro usuario. Recargá los datos y volvé a intentar.");
    }

    @Test
    void dataIntegrityViolationShouldReturnConflict() {
        ProblemDetail problem =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicado"));

        assertProblem(problem, 409, "Conflict", "La operación entra en conflicto con datos existentes.");
    }

    @Test
    void missingResourceShouldReturnNotFound() {
        NoResourceFoundException ex =
                new NoResourceFoundException(HttpMethod.GET, "api/no-existe", "/api/no-existe");

        ProblemDetail problem = handler.handleNoResourceFound(ex);

        assertProblem(problem, 404, "Not Found", "El recurso solicitado no existe.");
    }

    @Test
    void unexpectedExceptionShouldReturnInternalServerError() {
        ProblemDetail problem =
                handler.handleUnexpected(new IllegalStateException("boom"));

        assertProblem(problem, 500, "Internal Server Error",
                "Ocurrió un error inesperado. Intente nuevamente.");
    }

    @Test
    void problemShouldIncludeTypeRequestIdAndTimestamp() {
        ProblemDetail problem = handler.handlerCustomException(new ResourceNotFoundException("No existe"));

        assertEquals(URI.create("about:blank"), problem.getType());
        assertNotNull(problem.getProperties().get("requestId"));
        assertNotNull(problem.getProperties().get("timestamp"));
        assertNull(problem.getProperties().get("errors"));
    }

    private void assertProblem(ProblemDetail problem, int status, String title, String detail) {
        assertNotNull(problem);
        assertEquals(status, problem.getStatus());
        assertEquals(title, problem.getTitle());
        assertEquals(detail, problem.getDetail());
        assertTrue(problem.getDetail() != null && !problem.getDetail().isBlank());
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
