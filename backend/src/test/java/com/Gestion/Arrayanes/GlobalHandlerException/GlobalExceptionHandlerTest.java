package com.Gestion.Arrayanes.GlobalHandlerException;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void customExceptionShouldUseItsOwnStatus() {
        ResponseEntity<ErrorResponseDTO> notFound =
                handler.handlerCustomException(new ResourceNotFoundException("No existe"));

        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertBody(notFound, 404, "Not Found", "No existe");

        ResponseEntity<ErrorResponseDTO> badRequest =
                handler.handlerCustomException(new BadRequestException("Dato inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertBody(badRequest, 400, "Bad Request", "Dato inválido");
    }

    @Test
    void customExceptionShouldDefaultToConflict() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handlerCustomException(new GlobalCustomException("Se superpone"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertBody(response, 409, "Conflict", "Se superpone");
    }

    @Test
    void validationExceptionShouldReturnBadRequestWithFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new ClientRequestDTO(), "request");
        binding.rejectValue("clientName", "NotBlank", "no puede estar vacío");

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertBody(response, 400, "Bad Request", "clientName: no puede estar vacío");
    }

    @Test
    void missingParameterShouldReturnBadRequest() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("date", "LocalDate");

        ResponseEntity<ErrorResponseDTO> response = handler.handleMissingParameter(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("date"));
    }

    @Test
    void optimisticLockShouldReturnConflict() {
        ResponseEntity<ErrorResponseDTO> response = handler.handleOptimisticLock(
                new ObjectOptimisticLockingFailureException(Booking.class, UUID.randomUUID()));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertBody(response, 409, "Conflict",
                "La reserva fue modificada por otro usuario. Recargá los datos y volvé a intentar.");
    }

    @Test
    void dataIntegrityViolationShouldReturnConflict() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicado"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertBody(response, 409, "Conflict", "La operación entra en conflicto con datos existentes.");
    }

    @Test
    void unexpectedExceptionShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleUnexpected(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertBody(response, 500, "Internal Server Error",
                "Ocurrió un error inesperado. Intente nuevamente.");
    }

    private void assertBody(ResponseEntity<ErrorResponseDTO> response,
                            int status,
                            String error,
                            String message) {
        ErrorResponseDTO body = response.getBody();

        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertEquals(status, body.status());
        assertEquals(error, body.error());
        assertEquals(message, body.message());
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
    }
}
