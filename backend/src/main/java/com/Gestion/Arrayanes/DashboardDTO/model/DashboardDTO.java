package com.Gestion.Arrayanes.DashboardDTO.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class DashboardDTO {

    private UUID id;
    private UUID clientId;
    private String clientName;
    private String clientLastName;
    private UUID apartmentId;
    private String apartmentName;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private Long nights;
    private Long amountPeople;
    private BigDecimal totalPrice;

    public DashboardDTO(UUID id,UUID clientId ,String clientName, String clientLastName,UUID apartmentId ,String apartmentName, LocalDate arrivalDate, LocalDate departureDate, Long nights, Long amountPeople, BigDecimal totalPrice ) {

        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.apartmentId = apartmentId;
        this.apartmentName = apartmentName;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.nights = nights;
        this.amountPeople = amountPeople;
        this.totalPrice = totalPrice;

    }

}
