package com.Gestion.Arrayanes.Booking.dto;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class BookingRequestDTO {

    @NotNull(message = "La fecha de llegada es obligatoria.")
    private LocalDate arrivalDate;
    @Positive(message = "La cantidad de noches debe ser mayor a cero.")
    private Long nights;
    private LocalDate departureDate;
    private UUID clientId;
    @NotNull(message = "El apartamento es obligatorio.")
    private UUID apartmentId;
    @NotNull(message = "La cantidad de personas es obligatoria.")
    @Positive(message = "La cantidad de personas debe ser mayor a cero.")
    private Long amountPeople;
    private BigDecimal totalPrice;

    /**
     * Versión de la reserva que el cliente leyó. Obligatoria al editar para
     * detectar cambios concurrentes (respuesta 409).
     */
    @PositiveOrZero(message = "La versión de la reserva no puede ser negativa.")
    private Long version;

    @Valid
    private ClientRequestDTO client;

    public BookingRequestDTO() {
    }

    public BookingRequestDTO(LocalDate arrivalDate, Long nights, LocalDate departureDate, UUID clientId, UUID apartmentId, Long amountPeople, BigDecimal totalPrice) {
        this.arrivalDate = arrivalDate;
        this.nights = nights;
        this.departureDate = departureDate;
        this.clientId = clientId;
        this.apartmentId = apartmentId;
        this.amountPeople = amountPeople;
        this.totalPrice = totalPrice;
    }
}
