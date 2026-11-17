package com.Gestion.Arrayanes.Clients.repository;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.support.SqlStatementCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties =
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "com.Gestion.Arrayanes.support.SqlStatementCollector")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @BeforeEach
    void setUp() {
        clientRepository.save(new Client(null, "Ana", "Perez", "12345678", "555-1234"));
    }

    @Test
    void shouldDetectExistingDni() {
        assertTrue(clientRepository.existsByClientDni("12345678"));
        assertFalse(clientRepository.existsByClientDni("00000000"));
    }

    @Test
    void shouldFindClientByDni() {
        Optional<Client> found = clientRepository.findByClientDni("12345678");

        assertTrue(found.isPresent());
        assertEquals("Ana", found.get().getClientName());
    }

    @Test
    void shouldReturnEmptyWhenDniDoesNotExist() {
        assertTrue(clientRepository.findByClientDni("00000000").isEmpty());
    }

    @Test
    void lastCheckInsShouldBeGroupedAndIgnoreCancelledBookings() {
        Client ana = clientRepository.findByClientDni("12345678").orElseThrow();
        Client luis = clientRepository.save(new Client(null, "Luis", "Gomez", "87654321", "555-9999"));
        Apartment apartment = apartmentRepository.save(
                new Apartment(null, "Arrayanes 1", 4L, new BigDecimal("10000.00")));

        bookingRepository.save(booking(ana, apartment, LocalDate.of(2026, 10, 1)));
        bookingRepository.save(booking(ana, apartment, LocalDate.of(2026, 11, 1)));
        Booking cancelled = booking(ana, apartment, LocalDate.of(2026, 12, 1));
        cancelled.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(cancelled);

        SqlStatementCollector.reset();
        List<ClientLastCheckIn> rows = clientRepository.findLastCheckIns(BookingStatus.CANCELLED);

        Map<UUID, LocalDate> byClient = new HashMap<>();
        rows.forEach(row -> byClient.put(row.getClientId(), row.getLastCheckIn()));

        assertEquals(2, rows.size());
        assertEquals(LocalDate.of(2026, 11, 1), byClient.get(ana.getId()));
        assertNull(byClient.get(luis.getId()));
        assertEquals(1, SqlStatementCollector.selectCount());
    }

    private Booking booking(Client client, Apartment apartment, LocalDate arrivalDate) {
        return new Booking(null, arrivalDate, 2L, arrivalDate.plusDays(2),
                client, apartment, 2L, new BigDecimal("30000.00"));
    }
}
