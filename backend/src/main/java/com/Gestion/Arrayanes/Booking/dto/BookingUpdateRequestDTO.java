package com.Gestion.Arrayanes.Booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Edición de una reserva existente.
 *
 * <p>La versión es la que el cliente leyó antes de editar y permite detectar
 * cambios concurrentes (respuesta 409).</p>
 */
@Setter
@Getter
public class BookingUpdateRequestDTO extends BookingBaseRequestDTO {

    @NotNull(message = "El cliente es obligatorio.")
    private UUID clientId;

    @NotNull(message = "La versión de la reserva es obligatoria para editarla.")
    @PositiveOrZero(message = "La versión de la reserva no puede ser negativa.")
    private Long version;
}
