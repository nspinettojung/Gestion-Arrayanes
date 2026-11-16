package com.Gestion.Arrayanes.Booking.controller;

import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingUpdateRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithNewClientRequestDTO;
import com.Gestion.Arrayanes.Booking.service.IBookingService;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import com.Gestion.Arrayanes.support.FixedClockTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(FixedClockTestConfiguration.class)
class BookingControllerTest {

    private static final UUID BOOKING_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID APARTMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IBookingService bookingService;

    @Test
    void createBookingShouldReturnCreatedWithLocation() throws Exception {
        when(bookingService.saveBooking(any(BookingWithClientIdRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/bookings/createbooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/bookings/createbooking/" + BOOKING_ID)))
                .andExpect(jsonPath("$.id").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.totalPrice").value(45000))
                .andExpect(jsonPath("$.transactionCode").value("RS-2026-0007"));
    }

    @Test
    void createBookingShouldRejectInvalidBody() throws Exception {
        mockMvc.perform(post("/api/bookings/createbooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nights\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("arrivalDate")));
    }

    @Test
    void createBookingShouldRejectMissingClientId() throws Exception {
        mockMvc.perform(post("/api/bookings/createbooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "arrivalDate": "2026-12-01",
                                  "nights": 3,
                                  "apartmentId": "%s",
                                  "amountPeople": 2
                                }
                                """.formatted(APARTMENT_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("clientId")));
    }

    @Test
    void createBookingWithClientShouldReturnCreated() throws Exception {
        when(bookingService.createBookingWithClient(any(BookingWithNewClientRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/bookings/createWithClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingWithClientJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(BOOKING_ID.toString()));
    }

    @Test
    void createBookingShouldMapConflictTo409() throws Exception {
        when(bookingService.saveBooking(any(BookingWithClientIdRequestDTO.class)))
                .thenThrow(new GlobalCustomException("Las fechas se superponen con otra reserva existente."));

        mockMvc.perform(post("/api/bookings/createbooking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("superponen")));
    }

    @Test
    void deleteBookingShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/bookings/delete/{id}", BOOKING_ID))
                .andExpect(status().isNoContent());

        verify(bookingService).deleteBooking(BOOKING_ID);
    }

    @Test
    void deleteBookingListShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"" + BOOKING_ID + "\"]"))
                .andExpect(status().isNoContent());

        verify(bookingService).deleteBookings(List.of(BOOKING_ID));
    }

    @Test
    void getBookingListShouldReturnBookings() throws Exception {
        when(bookingService.getListBooking()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/bookings/bookinglist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$[0].clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$[0].transactionCode").value("RS-2026-0007"))
                .andExpect(jsonPath("$[0].version").value(3));
    }

    @Test
    void updateBookingShouldReturnUpdatedBooking() throws Exception {
        when(bookingService.updateBooking(eq(BOOKING_ID), any(BookingUpdateRequestDTO.class))).thenReturn(response());

        mockMvc.perform(put("/api/bookings/editbooking/{id}", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingUpdateJson(3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.version").value(3));
    }

    @Test
    void updateBookingShouldReturnConflictWhenVersionIsStale() throws Exception {
        when(bookingService.updateBooking(eq(BOOKING_ID), any(BookingUpdateRequestDTO.class)))
                .thenThrow(new OptimisticLockingFailureException("conflicto"));

        mockMvc.perform(put("/api/bookings/editbooking/{id}", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingUpdateJson(1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("modificada por otro usuario")));
    }

    @Test
    void updateBookingShouldRejectNegativeVersion() throws Exception {
        mockMvc.perform(put("/api/bookings/editbooking/{id}", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingUpdateJson(-1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("version")));
    }

    @Test
    void updateBookingShouldRejectMissingVersion() throws Exception {
        mockMvc.perform(put("/api/bookings/editbooking/{id}", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "arrivalDate": "2026-12-01",
                                  "nights": 3,
                                  "clientId": "%s",
                                  "apartmentId": "%s",
                                  "amountPeople": 2
                                }
                                """.formatted(CLIENT_ID, APARTMENT_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("version")));
    }

    @Test
    void updateBookingShouldReturnNotFoundWhenBookingDoesNotExist() throws Exception {
        when(bookingService.updateBooking(eq(BOOKING_ID), any(BookingUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Reserva no encontrada."));

        mockMvc.perform(put("/api/bookings/editbooking/{id}", BOOKING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingUpdateJson(3)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void deleteBookingShouldReturnNotFoundWhenBookingDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Reserva no encontrada."))
                .when(bookingService).deleteBooking(BOOKING_ID);

        mockMvc.perform(delete("/api/bookings/delete/{id}", BOOKING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentDateShouldUseInjectedClock() throws Exception {
        mockMvc.perform(get("/api/bookings/currentdate"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"2026-11-14\""));
    }

    @Test
    void generateTransactionCodeShouldReturnCode() throws Exception {
        when(bookingService.transactionCodeGenerator()).thenReturn("RS-2026-0001");

        mockMvc.perform(get("/api/bookings/generateTransactionCode"))
                .andExpect(status().isOk())
                .andExpect(content().string("RS-2026-0001"));
    }

    @Test
    void corsShouldAllowAngularDevelopmentOrigin() throws Exception {
        when(bookingService.getListBooking()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/bookings/bookinglist")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void corsShouldRejectUnknownOrigin() throws Exception {
        mockMvc.perform(get("/api/bookings/bookinglist")
                        .header("Origin", "http://evil.example"))
                .andExpect(status().isForbidden());
    }

    private BookingResponseDTO response() {
        BookingResponseDTO response = new BookingResponseDTO(
                BOOKING_ID,
                LocalDate.of(2026, 12, 1),
                3L,
                LocalDate.of(2026, 12, 4),
                CLIENT_ID,
                APARTMENT_ID,
                2L,
                new BigDecimal("45000.00")
        );
        response.setTransactionCode("RS-2026-0007");
        response.setVersion(3L);
        return response;
    }

    private String bookingJson() {
        return """
                {
                  "arrivalDate": "2026-12-01",
                  "nights": 3,
                  "clientId": "%s",
                  "apartmentId": "%s",
                  "amountPeople": 2
                }
                """.formatted(CLIENT_ID, APARTMENT_ID);
    }

    private String bookingUpdateJson(long version) {
        return """
                {
                  "arrivalDate": "2026-12-01",
                  "nights": 3,
                  "clientId": "%s",
                  "apartmentId": "%s",
                  "amountPeople": 2,
                  "version": %d
                }
                """.formatted(CLIENT_ID, APARTMENT_ID, version);
    }

    private String bookingWithClientJson() {
        return """
                {
                  "arrivalDate": "2026-12-01",
                  "nights": 3,
                  "apartmentId": "%s",
                  "amountPeople": 2,
                  "client": {
                    "clientName": "Ana",
                    "clientLastName": "Perez",
                    "clientDni": "12345678",
                    "clientPhone": "555-1234"
                  }
                }
                """.formatted(APARTMENT_ID);
    }
}
