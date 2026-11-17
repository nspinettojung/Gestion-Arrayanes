package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingValidationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 14);

    @Mock
    private BookingRepository bookingRepository;

    private BookingValidation validation;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        validation = new BookingValidation(bookingRepository, fixedClock);

        apartment = new Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setApartmentName("Arrayanes 1");
        apartment.setApartmentMaxNumber(4L);
        apartment.setPrice(new BigDecimal("10000.00"));
    }

    @Test
    void shouldRejectNullArrivalDate() {
        Booking booking = booking(null, 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 1L), apartment));
    }

    @Test
    void shouldRejectArrivalDateInThePast() {
        Booking booking = booking(TODAY.minusDays(1), 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 1L), apartment));
    }

    @Test
    void shouldAcceptArrivalDateToday() {
        Booking booking = booking(TODAY, 2L);
        stubNoOverlaps();

        Stay stay = validation.validate(booking, dto(null, 1L), apartment);

        assertEquals(TODAY.plusDays(1), stay.departureDate());
    }

    @Test
    void shouldRejectWhenNeitherNightsNorDepartureAreProvided() {
        Booking booking = booking(TODAY, 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, null), apartment));
    }

    @Test
    void shouldRejectNonPositiveNights() {
        Booking booking = booking(TODAY, 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 0L), apartment));
    }

    @Test
    void shouldRejectDepartureDateEqualToArrivalDate() {
        Booking booking = booking(TODAY, 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(TODAY, null), apartment));
    }

    @Test
    void shouldRejectDepartureDateBeforeArrivalDate() {
        Booking booking = booking(TODAY.plusDays(5), 2L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(TODAY.plusDays(3), null), apartment));
    }

    @Test
    void shouldCalculateDepartureDateFromNightsWithoutMutatingBooking() {
        Booking booking = booking(TODAY, 2L);
        stubNoOverlaps();

        Stay stay = validation.validate(booking, dto(null, 3L), apartment);

        assertEquals(new Stay(TODAY, TODAY.plusDays(3), 3L), stay);
        assertNull(booking.getDepartureDate());
        assertNull(booking.getNights());
    }

    @Test
    void shouldCalculateNightsFromDepartureDateWithoutMutatingBooking() {
        Booking booking = booking(TODAY, 2L);
        stubNoOverlaps();

        Stay stay = validation.validate(booking, dto(TODAY.plusDays(2), null), apartment);

        assertEquals(new Stay(TODAY, TODAY.plusDays(2), 2L), stay);
        assertNull(booking.getDepartureDate());
        assertNull(booking.getNights());
    }

    @Test
    void shouldRejectMorePeopleThanApartmentCapacity() {
        Booking booking = booking(TODAY, 5L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 1L), apartment));
    }

    @Test
    void shouldRejectNullAmountOfPeople() {
        Booking booking = booking(TODAY, null);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 1L), apartment));
    }

    @Test
    void shouldRejectZeroAmountOfPeople() {
        Booking booking = booking(TODAY, 0L);

        assertThrows(BadRequestException.class,
                () -> validation.validate(booking, dto(null, 1L), apartment));
    }

    @Test
    void shouldRejectOverlappingBooking() {
        Booking booking = booking(TODAY, 2L);
        when(bookingRepository.findOverlappingBookings(
                apartment.getId(), null, TODAY, TODAY.plusDays(2)))
                .thenReturn(List.of(new Booking()));

        assertThrows(GlobalCustomException.class,
                () -> validation.validate(booking, dto(TODAY.plusDays(2), null), apartment));
    }

    @Test
    void shouldAcceptWhenThereAreNoOverlaps() {
        Booking booking = booking(TODAY, 2L);
        stubNoOverlaps();

        validation.validate(booking, dto(TODAY.plusDays(2), null), apartment);

        verify(bookingRepository).findOverlappingBookings(
                apartment.getId(), null, TODAY, TODAY.plusDays(2));
    }

    private Booking booking(LocalDate arrivalDate, Long amountPeople) {
        Booking booking = new Booking();
        booking.setArrivalDate(arrivalDate);
        booking.setAmountPeople(amountPeople);
        return booking;
    }

    private BookingWithClientIdRequestDTO dto(LocalDate departureDate, Long nights) {
        BookingWithClientIdRequestDTO dto = new BookingWithClientIdRequestDTO();
        dto.setDepartureDate(departureDate);
        dto.setNights(nights);
        return dto;
    }

    private void stubNoOverlaps() {
        when(bookingRepository.findOverlappingBookings(
                org.mockito.ArgumentMatchers.eq(apartment.getId()),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of());
    }
}
