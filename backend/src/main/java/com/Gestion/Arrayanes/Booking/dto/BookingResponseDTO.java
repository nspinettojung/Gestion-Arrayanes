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
    private String clientName;
    private UUID apartmentId;
    private String apartmentName;
    private Long amountPeople;
    private BigDecimal totalPrice;
    private String transactionCode;
    private long version;

    public BookingResponseDTO() {
    }

}
