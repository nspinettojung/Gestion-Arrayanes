package com.Gestion.Arrayanes.DashboardDTO.service;

import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.DashboardDTO.model.CalendarDayDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.ClientApartmentDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardStatsDTO;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 14);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ApartmentRepository apartmentRepository;

    private DashboardService dashboardService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        dashboardService = new DashboardService(bookingRepository, apartmentRepository, fixedClock);

        Client client = new Client(UUID.randomUUID(), "Ana", "Perez", "12345678", "555-1234");

        com.Gestion.Arrayanes.Apartment.model.Apartment apartment =
                new com.Gestion.Arrayanes.Apartment.model.Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setApartmentName("Arrayanes 1");
        apartment.setApartmentMaxNumber(4L);
        apartment.setPrice(new BigDecimal("10000.00"));

        booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setTransactionCode("RS-2026-0001");
        booking.setClient(client);
        booking.setApartment(apartment);
        booking.setArrivalDate(LocalDate.of(2026, 11, 10));
        booking.setDepartureDate(LocalDate.of(2026, 11, 12));
        booking.setNights(2L);
        booking.setAmountPeople(2L);
        booking.setTotalPrice(new BigDecimal("30000.00"));
        booking.setVersion(4L);
    }

    @Test
    void getStatsShouldAggregateDashboardCounters() {
        when(bookingRepository.count()).thenReturn(7L);
        when(apartmentRepository.count()).thenReturn(4L);
        when(bookingRepository.countOccupiedBookingsOnDate(TODAY)).thenReturn(2L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY)).thenReturn(3L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY.minusDays(1))).thenReturn(2L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(7L, stats.totalBookings());
        assertEquals(4L, stats.totalApartments());
        assertEquals(50.0, stats.occupancy());
        assertEquals(50.0, stats.growth());
        assertEquals(3L, stats.todayBookings());
    }

    @Test
    void getStatsShouldReturnZeroWithoutData() {
        when(bookingRepository.count()).thenReturn(0L);
        when(apartmentRepository.count()).thenReturn(0L);
        when(bookingRepository.countOccupiedBookingsOnDate(TODAY)).thenReturn(0L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY)).thenReturn(0L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY.minusDays(1))).thenReturn(0L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(0L, stats.totalBookings());
        assertEquals(0L, stats.totalApartments());
        assertEquals(0.0, stats.occupancy());
        assertEquals(0.0, stats.growth());
        assertEquals(0L, stats.todayBookings());
    }

    @Test
    void getStatsShouldReturnOneHundredGrowthWhenYesterdayWasZero() {
        when(bookingRepository.count()).thenReturn(1L);
        when(apartmentRepository.count()).thenReturn(2L);
        when(bookingRepository.countOccupiedBookingsOnDate(TODAY)).thenReturn(1L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY)).thenReturn(2L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY.minusDays(1))).thenReturn(0L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(50.0, stats.occupancy());
        assertEquals(100.0, stats.growth());
    }

    @Test
    void getStatsShouldCalculateNegativeGrowth() {
        when(bookingRepository.count()).thenReturn(2L);
        when(apartmentRepository.count()).thenReturn(2L);
        when(bookingRepository.countOccupiedBookingsOnDate(TODAY)).thenReturn(1L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY)).thenReturn(1L);
        when(bookingRepository.countBookingsByArrivalDate(TODAY.minusDays(1))).thenReturn(2L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(-50.0, stats.growth());
    }

    @Test
    void monthlyCountsShouldUseInclusiveMonthRange() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);

        when(bookingRepository.countBookingsByArrivalDateBetween(start, end)).thenReturn(5L);
        when(bookingRepository.countBookingsByDepartureDateBetween(start, end)).thenReturn(6L);

        assertEquals(5L, dashboardService.getArrivalsCountInMonth(LocalDate.of(2026, 11, 20)));
        assertEquals(6L, dashboardService.getDeparturesCountInMonth(LocalDate.of(2026, 11, 20)));
    }

    @Test
    void getArrivalsListInMonthShouldMapClientAndApartmentData() {
        when(bookingRepository.findArrivalsInMonth(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(booking));

        List<ClientApartmentDTO> result =
                dashboardService.getArrivalsListInMonth(LocalDate.of(2026, 11, 5));

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getClientName());
        assertEquals("Perez", result.get(0).getClientLastName());
        assertEquals("Arrayanes 1", result.get(0).getApartmentName());
    }

    @Test
    void getDeparturesListInMonthShouldMapClientAndApartmentData() {
        when(bookingRepository.findDeparturesInMonth(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(booking));

        List<ClientApartmentDTO> result =
                dashboardService.getDeparturesListInMonth(LocalDate.of(2026, 11, 5));

        assertEquals(1, result.size());
        assertEquals("Arrayanes 1", result.get(0).getApartmentName());
    }

    @Test
    void getArrivalsAndDeparturesByDateShouldMapBookings() {
        when(bookingRepository.findArrivalsByDate(TODAY)).thenReturn(List.of(booking));
        when(bookingRepository.findDeparturesByDate(TODAY)).thenReturn(List.of(booking));

        assertEquals(1, dashboardService.getArrivalsByDate(TODAY).size());
        assertEquals(1, dashboardService.getDeparturesByDate(TODAY).size());
    }

    @Test
    void getCalendarShouldMarkArrivalAndIntermediateDaysButNotDeparture() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 12);

        when(bookingRepository.findBookingsInRange(start, end))
                .thenReturn(List.of(booking)); // 2026-11-10 -> 2026-11-12

        List<CalendarDayDTO> calendar = dashboardService.getCalendar(start, end);

        assertEquals(3, calendar.size());
        assertTrue(calendar.get(0).isHasBooking()); // 10
        assertTrue(calendar.get(1).isHasBooking()); // 11
        assertFalse(calendar.get(2).isHasBooking()); // 12 (día de salida)
    }

    @Test
    void getCalendarShouldReturnEveryDayWithoutBookings() {
        when(bookingRepository.findBookingsInRange(TODAY, TODAY.plusDays(1)))
                .thenReturn(List.of());

        List<CalendarDayDTO> calendar = dashboardService.getCalendar(TODAY, TODAY.plusDays(1));

        assertEquals(2, calendar.size());
        assertFalse(calendar.get(0).isHasBooking());
        assertFalse(calendar.get(1).isHasBooking());
    }

    @Test
    void getArrivalsAndDeparturesCountByDateShouldDelegateToRepository() {
        when(bookingRepository.countBookingsByArrivalDate(TODAY)).thenReturn(2L);
        when(bookingRepository.countDeparturesByDate(TODAY)).thenReturn(3L);

        assertEquals(2L, dashboardService.getArrivalsCountByDate(TODAY));
        assertEquals(3L, dashboardService.getDeparturesCountByDate(TODAY));
    }

    @Test
    void getDashboardShouldMapAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<DashboardDTO> result = dashboardService.getDashboard();

        assertEquals(1, result.size());
        DashboardDTO dto = result.get(0);
        assertEquals(booking.getId(), dto.getId());
        assertEquals("RS-2026-0001", dto.getTransactionCode());
        assertEquals("Ana", dto.getClientName());
        assertEquals("Arrayanes 1", dto.getApartmentName());
        assertEquals(booking.getTotalPrice(), dto.getTotalPrice());
        assertEquals(4L, dto.getVersion());
    }

    @Test
    void filteredDashboardShouldApplySortDirectionAndPagination() {
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        Page<DashboardDTO> result = dashboardService.getDashboard(
                null, null, null, null, 1, 5, "arrivalDate", "desc", false);

        assertEquals(1, result.getTotalElements());
        assertEquals("RS-2026-0001", result.getContent().get(0).getTransactionCode());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookingRepository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("arrivalDate").getDirection());
    }

    @Test
    void filteredDashboardShouldDefaultToAscendingArrivalDate() {
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        dashboardService.getDashboard(null, null, null, null, 0, 10, null, null, false);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookingRepository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("arrivalDate").getDirection());
    }

    @Test
    void filteredDashboardShouldRejectNegativePage() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard(null, null, null, null, -1, 10, "arrivalDate", "asc", false));
    }

    @Test
    void filteredDashboardShouldRejectSizeBelowOne() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard(null, null, null, null, 0, 0, "arrivalDate", "asc", false));
    }

    @Test
    void filteredDashboardShouldRejectSizeAboveMaximum() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard(null, null, null, null, 0, 1001, "arrivalDate", "asc", false));
    }

    @Test
    void filteredDashboardShouldAcceptMaximumSize() {
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        Page<DashboardDTO> result = dashboardService.getDashboard(
                null, null, null, null, 0, 1000, "arrivalDate", "asc", false);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void filteredDashboardShouldRejectUnknownSortField() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard(null, null, null, null, 0, 10, "secretField", "asc", false));
    }

    @Test
    void filteredDashboardShouldRejectInvalidSortDirection() {
        assertThrows(BadRequestException.class,
                () -> dashboardService.getDashboard(null, null, null, null, 0, 10, "arrivalDate", "sideways", false));
    }

    @Test
    @SuppressWarnings("unchecked")
    void filteredDashboardShouldFilterOutDepartedBookingsWhenOnlyActive() {
        when(bookingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        dashboardService.getDashboard(null, null, null, null, 0, 10, null, null, true);

        ArgumentCaptor<Specification<Booking>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(bookingRepository).findAll(captor.capture(), any(Pageable.class));

        Root<Booking> root = mock(Root.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<LocalDate> departurePath = mock(Path.class);
        when(root.<LocalDate>get("departureDate")).thenReturn(departurePath);

        captor.getValue().toPredicate(root, null, criteriaBuilder);

        verify(criteriaBuilder).greaterThanOrEqualTo(departurePath, TODAY);
    }
}
