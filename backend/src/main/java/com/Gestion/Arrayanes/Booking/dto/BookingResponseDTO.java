package com.Gestion.Arrayanes.Booking.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.UUID;

@Setter
@Getter
public class BookingResponseDTO {

    private UUID id;
    private LocalDate arrivalDate;
    private Long nights;
    private LocalDate departureDate;
    private UUID clientId;
    private UUID apartmentId;
    private Long amountPeople;
    private BigDecimal totalPrice;
    private String transactionCode;



    public BookingResponseDTO() {
    }

    public BookingResponseDTO(UUID id, LocalDate arrivalDate, Long nights, LocalDate departureDate, UUID clientId, UUID apartmentId, Long amountPeople, BigDecimal totalPrice) {
        this.id = id;
        this.arrivalDate = arrivalDate;
        this.nights = nights;
        this.departureDate = departureDate;
        this.clientId = clientId;
        this.apartmentId = apartmentId;
        this.amountPeople = amountPeople;
        this.totalPrice = totalPrice;
    }

}
