package com.Gestion.Arrayanes.Booking.mapper;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingUpdateRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingMapperTest {

    private final BookingMapper mapper = new BookingMapper();

    @Test
    void toEntityShouldMapClientDataButNotPrice() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");
        Apartment apartment = new Apartment(UUID.randomUUID(), "Arrayanes 1", 4L, new BigDecimal("10000"));
        BookingWithClientIdRequestDTO dto = new BookingWithClientIdRequestDTO();
        dto.setArrivalDate(LocalDate.of(2026, 12, 1));
        dto.setNights(3L);
        dto.setDepartureDate(LocalDate.of(2026, 12, 4));
        dto.setClientId(client.getId());
        dto.setApartmentId(apartment.getId());
        dto.setAmountPeople(2L);

        Booking booking = mapper.toEntity(dto, client, apartment);

        assertEquals(dto.getArrivalDate(), booking.getArrivalDate());
        assertEquals(dto.getNights(), booking.getNights());
        assertEquals(dto.getDepartureDate(), booking.getDepartureDate());
        assertEquals(client, booking.getClientId());
        assertEquals(apartment, booking.getApartmentId());
        assertEquals(dto.getAmountPeople(), booking.getAmountPeople());
        assertNull(booking.getTotalPrice());
    }

    @Test
    void toResponseDTOShouldMapIdentifiers() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");
        Apartment apartment = new Apartment(UUID.randomUUID(), "Arrayanes 1", 4L, new BigDecimal("10000"));
        Booking booking = new Booking(UUID.randomUUID(), LocalDate.of(2026, 12, 1), 3L,
                LocalDate.of(2026, 12, 4), client, apartment, 2L, new BigDecimal("45000"));
        booking.setTransactionCode("RS-2026-0005");
        booking.setVersion(4L);

        BookingResponseDTO dto = mapper.toResponseDTO(booking);

        assertEquals(booking.getId(), dto.getId());
        assertEquals(client.getId(), dto.getClientId());
        assertEquals(apartment.getId(), dto.getApartmentId());
        assertEquals(booking.getTotalPrice(), dto.getTotalPrice());
        assertEquals("RS-2026-0005", dto.getTransactionCode());
        assertEquals(4L, dto.getVersion());
    }

    @Test
    void bookingUpdateShouldOverwriteFieldsAndKeepServerPrice() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");
        Apartment apartment = new Apartment(UUID.randomUUID(), "Arrayanes 1", 4L, new BigDecimal("10000"));
        Booking booking = new Booking();
        booking.setTotalPrice(new BigDecimal("45000"));
        BookingUpdateRequestDTO dto = new BookingUpdateRequestDTO();
        dto.setArrivalDate(LocalDate.of(2026, 12, 10));
        dto.setNights(2L);
        dto.setDepartureDate(LocalDate.of(2026, 12, 12));
        dto.setClientId(client.getId());
        dto.setApartmentId(apartment.getId());
        dto.setAmountPeople(3L);

        mapper.bookingUpdate(booking, dto, client, apartment);

        assertEquals(dto.getArrivalDate(), booking.getArrivalDate());
        assertEquals(dto.getNights(), booking.getNights());
        assertEquals(dto.getDepartureDate(), booking.getDepartureDate());
        assertEquals(client, booking.getClientId());
        assertEquals(apartment, booking.getApartmentId());
        assertEquals(dto.getAmountPeople(), booking.getAmountPeople());
        assertEquals(new BigDecimal("45000"), booking.getTotalPrice());
    }
}
