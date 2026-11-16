package com.Gestion.Arrayanes.Booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Datos comunes a las altas y la edición de reservas.
 *
 * <p>El precio total, el código de transacción y la versión los administra el
 * servidor: nunca se aceptan como datos de entrada de la reserva.</p>
 */
@Setter
@Getter
public abstract class BookingBaseRequestDTO {

    @NotNull(message = "La fecha de llegada es obligatoria.")
    private LocalDate arrivalDate;

    @Positive(message = "La cantidad de noches debe ser mayor a cero.")
    private Long nights;

    private LocalDate departureDate;

    @NotNull(message = "El apartamento es obligatorio.")
    private UUID apartmentId;

    @NotNull(message = "La cantidad de personas es obligatoria.")
    @Positive(message = "La cantidad de personas debe ser mayor a cero.")
    private Long amountPeople;
}
