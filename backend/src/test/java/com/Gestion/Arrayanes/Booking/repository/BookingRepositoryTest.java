package com.Gestion.Arrayanes.Booking.repository;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.repository.ClientRepository;
import com.Gestion.Arrayanes.DashboardDTO.service.BookingSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import com.Gestion.Arrayanes.support.SqlStatementCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@TestPropertySource(properties =
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "com.Gestion.Arrayanes.support.SqlStatementCollector")
class BookingRepositoryTest {

    private static final LocalDate NOV_10 = LocalDate.of(2026, 11, 10);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private EntityManager entityManager;

    private Client client;
    private Apartment apartment;
    private Booking booking;

    @BeforeEach
    void setUp() {
        client = clientRepository.save(new Client(null, "Ana", "Perez", "12345678", "555-1234"));
        apartment = apartmentRepository.save(
                new Apartment(null, "Arrayanes 1", 4L, new BigDecimal("10000.00")));

        booking = bookingRepository.save(booking(client, apartment, NOV_10, NOV_10.plusDays(2)));
    }

    @Test
    void shouldCountArrivalsAndDeparturesOnExactDate() {
        assertEquals(1L, bookingRepository.countBookingsByArrivalDate(NOV_10));
        assertEquals(0L, bookingRepository.countBookingsByArrivalDate(NOV_10.plusDays(1)));
        assertEquals(1L, bookingRepository.countDeparturesByDate(NOV_10.plusDays(2)));
    }

    @Test
    void shouldCountOccupiedBookingsOnDateUsingHalfOpenRange() {
        assertEquals(1L, bookingRepository.countOccupiedBookingsOnDate(NOV_10));
        assertEquals(1L, bookingRepository.countOccupiedBookingsOnDate(NOV_10.plusDays(1)));
        // El día de salida ya está libre
        assertEquals(0L, bookingRepository.countOccupiedBookingsOnDate(NOV_10.plusDays(2)));
    }

    @Test
    void shouldCountArrivalsAndDeparturesInInclusiveRange() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        assertEquals(1L, bookingRepository.countBookingsByArrivalDateBetween(start, end));
        assertEquals(1L, bookingRepository.countBookingsByDepartureDateBetween(start, end));
        assertEquals(0L, bookingRepository.countBookingsByArrivalDateBetween(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));
    }

    @Test
    void findBookingsInRangeShouldExcludeDepartureDayAndIncludeOverlappingBooking() {
        List<Booking> fromDeparture = bookingRepository.findBookingsInRange(
                NOV_10.plusDays(2), NOV_10.plusDays(5));
        assertTrue(fromDeparture.isEmpty(), "La reserva que sale al inicio del rango no debe aparecer");

        List<Booking> coveringArrival = bookingRepository.findBookingsInRange(
                NOV_10.minusDays(1), NOV_10.plusDays(1));
        assertEquals(1, coveringArrival.size());
    }

    @Test
    void findOverlappingBookingsShouldDetectRealOverlap() {
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                apartment.getId(), null, NOV_10.plusDays(1), NOV_10.plusDays(4));

        assertEquals(1, overlaps.size());
    }

    @Test
    void findOverlappingBookingsShouldIgnoreAdjacentStays() {
        List<Booking> afterDeparture = bookingRepository.findOverlappingBookings(
                apartment.getId(), null, NOV_10.plusDays(2), NOV_10.plusDays(4));
        assertTrue(afterDeparture.isEmpty());

        List<Booking> beforeArrival = bookingRepository.findOverlappingBookings(
                apartment.getId(), null, NOV_10.minusDays(2), NOV_10);
        assertTrue(beforeArrival.isEmpty());
    }

    @Test
    void findOverlappingBookingsShouldExcludeTheBookingBeingEdited() {
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                apartment.getId(), booking.getId(), NOV_10, NOV_10.plusDays(2));

        assertTrue(overlaps.isEmpty());
    }

    @Test
    void findOverlappingBookingsShouldIgnoreOtherApartments() {
        Apartment other = apartmentRepository.save(
                new Apartment(null, "Arrayanes 2", 2L, new BigDecimal("9000.00")));

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                other.getId(), null, NOV_10, NOV_10.plusDays(2));

        assertTrue(overlaps.isEmpty());
    }

    @Test
    void shouldFindArrivalsAndDeparturesInMonth() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        assertEquals(1, bookingRepository.findArrivalsInMonth(start, end).size());
        assertEquals(1, bookingRepository.findDeparturesInMonth(start, end).size());
    }

    @Test
    void shouldFindArrivalsAndDeparturesByDate() {
        assertEquals(1, bookingRepository.findArrivalsByDate(NOV_10).size());
        assertEquals(0, bookingRepository.findArrivalsByDate(NOV_10.plusDays(1)).size());
        assertEquals(1, bookingRepository.findDeparturesByDate(NOV_10.plusDays(2)).size());
    }

    @Test
    void shouldOrderBookingsByArrivalDate() {
        bookingRepository.save(booking(client, apartment, NOV_10.minusDays(5), NOV_10.minusDays(4)));

        List<Booking> ordered = bookingRepository.findAllByOrderByArrivalDateAsc();

        assertEquals(2, ordered.size());
        assertTrue(ordered.get(0).getArrivalDate().isBefore(ordered.get(1).getArrivalDate()));
    }

    @Test
    void shouldFindLastBookingOfClient() {
        Booking older = bookingRepository.save(
                booking(client, apartment, NOV_10.minusDays(10), NOV_10.minusDays(9)));

        Optional<Booking> last = bookingRepository
                .findTopByClientAndStatusNotOrderByArrivalDateDesc(client, BookingStatus.CANCELLED);

        assertTrue(last.isPresent());
        assertEquals(booking.getId(), last.get().getId());
        assertFalse(last.get().getId().equals(older.getId()));
    }

    @Test
    void shouldPersistTransactionCode() {
        booking.setTransactionCode("RS-2026-0001");
        bookingRepository.saveAndFlush(booking);

        List<String> codes = bookingRepository.findAllTransactionCodes();

        assertTrue(codes.contains("RS-2026-0001"));
        assertFalse(codes.contains(null));
    }

    @Test
    void shouldStartAtVersionZeroAndIncrementOnUpdate() {
        bookingRepository.flush();
        Booking persisted = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(0L, persisted.getVersion());

        persisted.setAmountPeople(3L);
        Booking updated = bookingRepository.saveAndFlush(persisted);

        assertEquals(1L, updated.getVersion());
    }

    @Test
    void specificationShouldFilterByClientLastName() {
        List<Booking> matches = bookingRepository.findAll(BookingSpecifications.hasClientLastName("per"));
        assertEquals(1, matches.size());

        List<Booking> noMatches = bookingRepository.findAll(BookingSpecifications.hasClientLastName("gomez"));
        assertTrue(noMatches.isEmpty());
    }

    @Test
    void specificationShouldFilterByDateRangeAndApartment() {
        assertEquals(1, bookingRepository.findAll(
                BookingSpecifications.betweenDates(NOV_10, NOV_10.plusDays(1))).size());
        assertTrue(bookingRepository.findAll(
                BookingSpecifications.betweenDates(NOV_10.plusDays(3), NOV_10.plusDays(4))).isEmpty());

        assertEquals(1, bookingRepository.findAll(
                BookingSpecifications.apartmentEquals(apartment.getId())).size());
        assertTrue(bookingRepository.findAll(
                BookingSpecifications.apartmentEquals(UUID.randomUUID())).isEmpty());
    }

    @Test
    void operationalQueriesShouldIgnoreCancelledAndArchivedBookings() {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        Booking archived = bookingRepository.save(
                booking(client, apartment, NOV_10.plusDays(5), NOV_10.plusDays(7)));
        archived.setStatus(BookingStatus.ARCHIVED);
        bookingRepository.saveAndFlush(archived);

        assertEquals(0L, bookingRepository.countBookingsByArrivalDate(NOV_10));
        assertEquals(0L, bookingRepository.countOccupiedBookingsOnDate(NOV_10));
        // Las canceladas no bloquean el borrado; las archivadas siguen siendo historial.
        assertEquals(1L, bookingRepository.countByApartmentIdAndStatusNot(
                apartment.getId(), BookingStatus.CANCELLED));
        assertTrue(bookingRepository.findOverlappingBookings(
                apartment.getId(), null, NOV_10, NOV_10.plusDays(1)).isEmpty());
        assertTrue(bookingRepository.findBookingsInRange(NOV_10, NOV_10.plusDays(1)).isEmpty());
    }

    @Test
    void archiveExpiredBookingsShouldOnlyUpdateFinishedActiveBookings() {
        Booking finished = bookingRepository.save(
                booking(client, apartment, NOV_10.minusDays(9), NOV_10.minusDays(8)));

        int updated = bookingRepository.archiveExpiredBookings(
                NOV_10.plusDays(1), BookingStatus.ACTIVE, BookingStatus.ARCHIVED);

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, updated);
        assertEquals(BookingStatus.ARCHIVED,
                bookingRepository.findById(finished.getId()).orElseThrow().getStatus());
        assertEquals(BookingStatus.ACTIVE,
                bookingRepository.findById(booking.getId()).orElseThrow().getStatus());
    }

    @Test
    void dashboardListShouldFetchRelationsInOneQuery() {
        bookingRepository.save(booking(client, apartment, NOV_10.plusDays(3), NOV_10.plusDays(5)));
        entityManager.flush();
        entityManager.clear();
        SqlStatementCollector.reset();

        List<Booking> bookings = bookingRepository.findAllByStatusNot(BookingStatus.CANCELLED);

        assertEquals(2, bookings.size());
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertTrue(util.isLoaded(bookings.get(0), "client"));
        assertTrue(util.isLoaded(bookings.get(0), "apartment"));
        assertEquals(1, SqlStatementCollector.selectCount());
    }

    @Test
    void filteredDashboardShouldFetchRelationsWithoutNPlusOne() {
        bookingRepository.save(booking(client, apartment, NOV_10.plusDays(3), NOV_10.plusDays(5)));
        entityManager.flush();
        entityManager.clear();
        SqlStatementCollector.reset();

        // PageSize=1 fuerza el count: Spring Data lo omite si la página no se llena.
        Page<Booking> page = bookingRepository.findAll(
                BookingSpecifications.statusEquals(BookingStatus.ACTIVE), PageRequest.of(0, 1));

        assertEquals(2, page.getTotalElements());
        PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertTrue(util.isLoaded(page.getContent().get(0), "client"));
        assertTrue(util.isLoaded(page.getContent().get(0), "apartment"));
        // Una de count y una de datos; nunca una por reserva.
        assertEquals(2, SqlStatementCollector.selectCount());
    }

    private Booking booking(Client client, Apartment apartment, LocalDate arrival, LocalDate departure) {
        return new Booking(
                null,
                arrival,
                departure.toEpochDay() - arrival.toEpochDay(),
                departure,
                client,
                apartment,
                2L,
                new BigDecimal("30000.00")
        );
    }
}
