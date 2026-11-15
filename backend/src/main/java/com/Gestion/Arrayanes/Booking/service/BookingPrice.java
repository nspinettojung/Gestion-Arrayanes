package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookingPrice {

    private static final BigDecimal COST_PER_EXTRA_PERSON = new BigDecimal("5000");

    public static BigDecimal calculatePrice(BigDecimal apartmentPrice, Long amountPeople, Long nights) {
        if (apartmentPrice == null || amountPeople == null || nights == null) {
            throw new BadRequestException("Los parámetros de precio no pueden ser nulos.");
        }

        if (apartmentPrice.signum() < 0) {
            throw new BadRequestException("El precio del apartamento no puede ser negativo.");
        }

        if (amountPeople <= 0 || nights <= 0) {
            throw new BadRequestException("La cantidad de personas y noches debe ser mayor a cero.");
        }

        long extraPeopleCount = Math.max(0, amountPeople - 1);
        BigDecimal extraPeople = BigDecimal.valueOf(extraPeopleCount);
        BigDecimal amountNights = BigDecimal.valueOf(nights);

        BigDecimal adjustedBase = apartmentPrice.add(extraPeople.multiply(COST_PER_EXTRA_PERSON));

        return adjustedBase.multiply(amountNights);
    }
}
