package com.Gestion.Arrayanes.Booking.dto;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Alta de reserva que reutiliza el cliente si el DNI ya existe o lo crea si no. */
@Setter
@Getter
public class BookingWithNewClientRequestDTO extends BookingBaseRequestDTO {

    @Valid
    @NotNull(message = "Debe indicar los datos del cliente.")
    private ClientRequestDTO client;
}
