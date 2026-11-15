package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingRequestDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BookingValidation {

    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingValidation(BookingRepository bookingRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    public void validate(Booking booking, BookingRequestDTO dto, Apartment apartment) {
        validateArrivalDate(booking);

        determineDepartureDate(booking, dto);

        validateDepartureDate(booking);

        validateArrivalBeforeDeparture(booking);

        validateCapacity(booking, apartment);

        validateOverlaps(booking, apartment);
    }

    private void validateArrivalDate(Booking booking) {
        if (booking.getArrivalDate() == null) {
            throw new BadRequestException("La fecha de llegada es obligatoria.");
        }

        if (booking.getArrivalDate().isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("La fecha de llegada no puede ser anterior a hoy.");
        }
    }

    private void determineDepartureDate(Booking booking, BookingRequestDTO dto) {
        if (dto.getDepartureDate() != null) {
            booking.setDepartureDate(dto.getDepartureDate());
            long nights = ChronoUnit.DAYS.between(booking.getArrivalDate(), dto.getDepartureDate());
            booking.setNights(nights);
            return;
        }

        if (dto.getNights() == null) {
            throw new BadRequestException("Debe indicar la cantidad de noches o la fecha de salida.");
        }

        if (dto.getNights() <= 0) {
            throw new BadRequestException("La cantidad de noches debe ser mayor a cero.");
        }

        booking.setDepartureDate(booking.getArrivalDate().plusDays(dto.getNights()));
        booking.setNights(dto.getNights());
    }

    private void validateDepartureDate(Booking booking) {
        if (booking.getDepartureDate().isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("La fecha de salida no puede ser anterior a hoy.");
        }

        if (booking.getDepartureDate().isEqual(booking.getArrivalDate())) {
            throw new BadRequestException("La fecha de salida no puede ser igual a la de llegada.");
        }
    }

    private void validateArrivalBeforeDeparture(Booking booking) {
        if (booking.getDepartureDate().isBefore(booking.getArrivalDate())) {
            throw new BadRequestException("La fecha de salida debe ser posterior a la llegada.");
        }
    }

    private void validateCapacity(Booking booking, Apartment apartment) {
        if (booking.getAmountPeople() == null || booking.getAmountPeople() <= 0) {
            throw new BadRequestException("La cantidad de personas debe ser mayor a cero.");
        }

        if (apartment.getApartmentMaxNumber() != null
                && booking.getAmountPeople() > apartment.getApartmentMaxNumber()) {
            throw new BadRequestException(
                    "El apartamento admite hasta " + apartment.getApartmentMaxNumber() + " personas.");
        }
    }

    private void validateOverlaps(Booking booking, Apartment apartment) {
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                apartment.getId(),
                booking.getId(),
                booking.getArrivalDate(),
                booking.getDepartureDate()
        );

        if (!overlaps.isEmpty()) {
            throw new GlobalCustomException("Las fechas se superponen con otra reserva existente.");
        }
    }
}
