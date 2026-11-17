package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingBaseRequestDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
public class BookingValidation {

    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingValidation(BookingRepository bookingRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    public Stay validate(Booking booking, BookingBaseRequestDTO dto, Apartment apartment) {
        validateArrivalDate(booking.getArrivalDate());

        Stay stay = Stay.from(booking.getArrivalDate(), dto.getDepartureDate(), dto.getNights());

        validateStayDates(stay);
        validateCapacity(booking, apartment);
        validateOverlaps(booking, stay, apartment);

        return stay;
    }

    private void validateArrivalDate(LocalDate arrivalDate) {
        if (arrivalDate == null) {
            throw new BadRequestException("La fecha de llegada es obligatoria.");
        }

        if (arrivalDate.isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("La fecha de llegada no puede ser anterior a hoy.");
        }
    }

    private void validateStayDates(Stay stay) {
        if (stay.departureDate().isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("La fecha de salida no puede ser anterior a hoy.");
        }

        if (stay.departureDate().isEqual(stay.arrivalDate())) {
            throw new BadRequestException("La fecha de salida no puede ser igual a la de llegada.");
        }

        if (stay.departureDate().isBefore(stay.arrivalDate())) {
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

    private void validateOverlaps(Booking booking, Stay stay, Apartment apartment) {
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                apartment.getId(),
                booking.getId(),
                stay.arrivalDate(),
                stay.departureDate()
        );

        if (!overlaps.isEmpty()) {
            throw new GlobalCustomException("Las fechas se superponen con otra reserva existente.");
        }
    }
}
