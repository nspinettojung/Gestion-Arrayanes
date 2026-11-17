package com.Gestion.Arrayanes.Clients.mapper;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientMapperTest {

    @Mock
    private BookingRepository bookingRepository;

    private ClientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClientMapper(bookingRepository);
    }

    @Test
    void toEntityShouldMapAllFields() {
        ClientRequestDTO dto = new ClientRequestDTO("Ana", "Perez", "12345678", "555");

        Client client = mapper.toEntity(dto);

        assertEquals("Ana", client.getClientName());
        assertEquals("Perez", client.getClientLastName());
        assertEquals("12345678", client.getClientDni());
        assertEquals("555", client.getClientPhone());
    }

    @Test
    void toEntityShouldNormalizeDni() {
        ClientRequestDTO dto = new ClientRequestDTO("Ana", "Perez", " ab-123 ", "555");

        Client client = mapper.toEntity(dto);

        assertEquals("AB-123", client.getClientDni());
    }

    @Test
    void toResponseDTOShouldMapAllFields() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");

        ClientResponseDTO dto = mapper.toResponseDTO(client);

        assertEquals(client.getId(), dto.getId());
        assertEquals("Ana", dto.getClientName());
        assertEquals("Perez", dto.getClientLastName());
        assertEquals("12345678", dto.getClientDni());
        assertEquals("555", dto.getClientPhone());
        assertNull(dto.getLastCheckIn());
    }

    @Test
    void toResponseDTOShouldIncludeLastCheckIn() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");
        Booking lastBooking = new Booking();
        lastBooking.setArrivalDate(LocalDate.of(2026, 10, 5));

        when(bookingRepository.findTopByClientOrderByArrivalDateDesc(client))
                .thenReturn(Optional.of(lastBooking));

        ClientResponseDTO dto = mapper.toResponseDTO(client);

        assertEquals(LocalDate.of(2026, 10, 5), dto.getLastCheckIn());
    }

    @Test
    void updateEntityShouldOverwriteFields() {
        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555");
        ClientRequestDTO dto = new ClientRequestDTO("Maria", "Gomez", "87654321", "444");

        mapper.updateEntity(client, dto);

        assertEquals("Maria", client.getClientName());
        assertEquals("Gomez", client.getClientLastName());
        assertEquals("87654321", client.getClientDni());
        assertEquals("444", client.getClientPhone());
    }
}
