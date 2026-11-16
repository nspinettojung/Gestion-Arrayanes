package com.Gestion.Arrayanes.Booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Alta de reserva para un cliente que ya existe. */
@Setter
@Getter
public class BookingWithClientIdRequestDTO extends BookingBaseRequestDTO {

    @NotNull(message = "El cliente es obligatorio.")
    private UUID clientId;
}
