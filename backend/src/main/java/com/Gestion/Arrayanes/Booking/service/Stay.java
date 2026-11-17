package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Rango de estadía resuelto a partir de los datos de entrada, sin tocar la entidad. */
public record Stay(LocalDate arrivalDate, LocalDate departureDate, long nights) {

    public static Stay from(LocalDate arrivalDate, LocalDate departureDate, Long nights) {
        if (arrivalDate == null) {
            throw new BadRequestException("La fecha de llegada es obligatoria.");
        }

        if (departureDate != null) {
            return new Stay(arrivalDate, departureDate, ChronoUnit.DAYS.between(arrivalDate, departureDate));
        }

        if (nights == null) {
            throw new BadRequestException("Debe indicar la cantidad de noches o la fecha de salida.");
        }

        if (nights <= 0) {
            throw new BadRequestException("La cantidad de noches debe ser mayor a cero.");
        }

        return new Stay(arrivalDate, arrivalDate.plusDays(nights), nights);
    }

    public void applyTo(Booking booking) {
        booking.setArrivalDate(arrivalDate);
        booking.setDepartureDate(departureDate);
        booking.setNights(nights);
    }
}
