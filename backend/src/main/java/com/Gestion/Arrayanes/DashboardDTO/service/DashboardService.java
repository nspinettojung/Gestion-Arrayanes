package com.Gestion.Arrayanes.DashboardDTO.service;

import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.DashboardDTO.model.CalendarDayDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.ClientApartmentDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardStatsDTO;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int MAX_PAGE_SIZE = 1000;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "arrivalDate", "departureDate", "nights", "amountPeople", "totalPrice", "transactionCode");

    private final BookingRepository bookingRepository;
    private final ApartmentRepository apartmentRepository;
    private final Clock clock;


    public DashboardService(BookingRepository bookingRepository, ApartmentRepository apartmentRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.apartmentRepository = apartmentRepository;
        this.clock = clock;
    }

    public List<DashboardDTO> getDashboard() {
        List<Booking> bookings = bookingRepository.findAll();

        return bookings.stream()
                .map(b -> new DashboardDTO(
                        b.getId(),
                        b.getTransactionCode(),
                        b.getClientId().getId(),
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getId(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate(),
                        b.getNights(),
                        b.getAmountPeople(),
                        b.getTotalPrice(),
                        b.getVersion()
                ))
                .toList();
    }

    public Page<DashboardDTO> getDashboard(String lastName, LocalDate start, LocalDate end,
                                           UUID apartmentId, int page, int size,
                                           String sortBy, String sortDir, boolean onlyActive) {

        validatePagination(page, size, sortBy, sortDir);

        Specification<Booking> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (lastName != null) {
            spec = spec.and(BookingSpecifications.hasClientLastName(lastName));
        }
        if (start != null && end != null) {
            spec = spec.and(BookingSpecifications.betweenDates(start, end));
        }
        if (apartmentId != null) {
            spec = spec.and(BookingSpecifications.apartmentEquals(apartmentId));
        }
        if (onlyActive) {
            spec = spec.and(BookingSpecifications.departureOnOrAfter(LocalDate.now(clock)));
        }


        String sortField = (sortBy == null || sortBy.isEmpty()) ? "arrivalDate" : sortBy;
        Sort sort = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ?
                Sort.by(sortField).descending() :
                Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return bookingRepository.findAll(spec, pageable)
                .map(b -> new DashboardDTO(
                        b.getId(),
                        b.getTransactionCode(),
                        b.getClientId().getId(),
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getId(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate(),
                        b.getNights(),
                        b.getAmountPeople(),
                        b.getTotalPrice(),
                        b.getVersion()
                ));
    }


    public DashboardStatsDTO getStats() {
        LocalDate today = LocalDate.now(clock);
        long totalBookings = bookingRepository.count();
        long totalApartments = apartmentRepository.count();
        long occupied = bookingRepository.countOccupiedBookingsOnDate(today);
        long todayArrivals = bookingRepository.countBookingsByArrivalDate(today);
        long yesterdayArrivals = bookingRepository.countBookingsByArrivalDate(today.minusDays(1));

        return new DashboardStatsDTO(
                totalBookings,
                totalApartments,
                calculateOccupation(totalApartments, occupied),
                calculateGrowth(todayArrivals, yesterdayArrivals),
                todayArrivals
        );
    }

    private double calculateOccupation(long totalApartments, long occupied) {
        if (totalApartments == 0) {
            return 0.0;
        }

        return (double) occupied / totalApartments * 100;
    }

    private double calculateGrowth(long todayCount, long yesterdayCount) {
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }

        return ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100;
    }

    public long getArrivalsCountInMonth(LocalDate date) {
        LocalDate[] range = getMonthRange(date);
        return bookingRepository.countBookingsByArrivalDateBetween(range[0], range[1]);
    }

    public long getDeparturesCountInMonth(LocalDate date) {
        LocalDate[] range = getMonthRange(date);
        return bookingRepository.countBookingsByDepartureDateBetween(range[0], range[1]);
    }

    /**
     * Valida los parámetros de paginación y ordenamiento antes de construir el
     * Pageable. Sin whitelist, un {@code sortBy} inexistente rompe en Hibernate
     * y un {@code page}/{@code size} absurdo termina en 400/500.
     */
    private void validatePagination(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new BadRequestException("La página no puede ser negativa.");
        }

        if (size < 1) {
            throw new BadRequestException("El tamaño de página debe ser mayor a cero.");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("El tamaño de página no puede superar " + MAX_PAGE_SIZE + ".");
        }

        if (sortBy != null && !sortBy.isEmpty() && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("El campo de ordenamiento '" + sortBy + "' no es válido.");
        }

        if (sortDir != null && !sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new BadRequestException("El orden debe ser 'asc' o 'desc'.");
        }
    }

    private LocalDate[] getMonthRange(LocalDate date) {
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return new LocalDate[]{start, end};
    }

    public List<ClientApartmentDTO> getArrivalsListInMonth(LocalDate date) {
        LocalDate[] range = getMonthRange(date);
        List<Booking> bookings = bookingRepository.findArrivalsInMonth(range[0], range[1]);

        return bookings.stream()
                .map(b -> new ClientApartmentDTO(
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate()

                ))
                .toList();
    }

    public List<ClientApartmentDTO> getDeparturesListInMonth(LocalDate date) {
        LocalDate[] range = getMonthRange(date);
        List<Booking> bookings = bookingRepository.findDeparturesInMonth(range[0], range[1]);

        return bookings.stream()
                .map(b -> new ClientApartmentDTO(
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate()
                ))
                .toList();
    }

    public List<ClientApartmentDTO> getArrivalsByDate(LocalDate date) {
        List<Booking> bookings = bookingRepository.findArrivalsByDate(date);
        return bookings.stream()
                .map(b -> new ClientApartmentDTO(
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate()
                ))
                .toList();
    }

    public List<ClientApartmentDTO> getDeparturesByDate(LocalDate date) {
        List<Booking> bookings = bookingRepository.findDeparturesByDate(date);
        return bookings.stream()
                .map(b -> new ClientApartmentDTO(
                        b.getClientId().getClientName(),
                        b.getClientId().getClientLastName(),
                        b.getApartmentId().getApartmentName(),
                        b.getArrivalDate(),
                        b.getDepartureDate()
                ))
                .toList();
    }

    public List<CalendarDayDTO> getCalendar(LocalDate start, LocalDate end) {
        List<Booking> bookings = bookingRepository.findBookingsInRange(start, end);

        List<CalendarDayDTO> calendar = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            final LocalDate day = current; // variable final para usar en el lambda

            boolean hasBooking = bookings.stream()
                    .anyMatch(b -> !b.getArrivalDate().isAfter(day) && b.getDepartureDate().isAfter(day));

            calendar.add(new CalendarDayDTO(day, hasBooking));
            current = current.plusDays(1);
        }

        return calendar;
    }

    public long getArrivalsCountByDate(LocalDate date) {
        return bookingRepository.countBookingsByArrivalDate(date);
    }

    public long getDeparturesCountByDate(LocalDate date) {
        return bookingRepository.countDeparturesByDate(date);
    }

}
